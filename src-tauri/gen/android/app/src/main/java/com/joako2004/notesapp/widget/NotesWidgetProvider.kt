package com.joako2004.notesapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import android.widget.RemoteViews
import com.joako2004.notesapp.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NotesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TOGGLE_TASK -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                if (taskId == null || !isValidUuid(taskId)) {
                    Log.i(TAG, "toggle ignored invalid_id=$taskId pid=${Process.myPid()}")
                    return
                }
                handleWrite(context) { writer -> writer.markDone(taskId) }
            }

            ACTION_RESET_TASKS -> handleWrite(context) { writer -> writer.resetPending() }

            ACTION_REFRESH_LIST -> refreshWidgets(context)

            else -> super.onReceive(context, intent)
        }
    }

    private fun handleWrite(context: Context, write: (SharedDbWriter) -> WriteOutcome) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        try {
            EXECUTOR.execute {
                try {
                    val outcome = write(SharedDbWriter(appContext))
                    val errorMessage = outcome.error ?: "none"
                    Log.i(
                        TAG,
                        "write_result rows=${outcome.rows} retries=${outcome.retries} " +
                            "error=$errorMessage pid=${Process.myPid()}",
                    )
                } catch (error: Exception) {
                    Log.e(TAG, "write failed", error)
                } finally {
                    try {
                        refreshWidgets(appContext)
                    } catch (error: RuntimeException) {
                        Log.e(TAG, "refresh failed", error)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        } catch (error: RuntimeException) {
            Log.e(TAG, "write dispatch failed", error)
            pendingResult.finish()
        }
    }

    private fun buildRemoteViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_task_list)
        views.setRemoteAdapter(
            R.id.widget_task_list,
            Intent(context, NotesWidgetService::class.java),
        )
        views.setEmptyView(R.id.widget_task_list, R.id.widget_empty)
        views.setOnClickPendingIntent(R.id.widget_header, resetTasksPendingIntent(context))
        views.setPendingIntentTemplate(R.id.widget_task_list, toggleTaskPendingIntent(context))
        return views
    }

    private fun resetTasksPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_RESET_TASKS,
            Intent(context, NotesWidgetProvider::class.java).setAction(ACTION_RESET_TASKS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun toggleTaskPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_TOGGLE_TASK,
            Intent(context, NotesWidgetProvider::class.java).setAction(ACTION_TOGGLE_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

    private fun refreshWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, NotesWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        Log.i(TAG, "refresh widgets=${appWidgetIds.size} pid=${Process.myPid()}")
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_task_list)
    }

    private fun isValidUuid(value: String): Boolean = UUID_PATTERN.matches(value)

    companion object {
        const val ACTION_TOGGLE_TASK = "com.joako2004.notesapp.widget.ACTION_TOGGLE_TASK"
        const val ACTION_RESET_TASKS = "com.joako2004.notesapp.widget.ACTION_RESET_TASKS"
        const val ACTION_REFRESH_LIST = "com.joako2004.notesapp.widget.ACTION_REFRESH_LIST"
        const val EXTRA_TASK_ID = "com.joako2004.notesapp.widget.EXTRA_TASK_ID"

        private const val TAG = "SQLITESPIKE"
        private val UUID_PATTERN = Regex(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-" +
                "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
        )
        private val EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()
        private const val REQUEST_TOGGLE_TASK = 1
        private const val REQUEST_RESET_TASKS = 2
    }
}
