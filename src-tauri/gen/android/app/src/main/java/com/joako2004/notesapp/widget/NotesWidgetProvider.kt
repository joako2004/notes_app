package com.joako2004.notesapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.joako2004.notesapp.R

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
                val taskId = intent.getIntExtra(EXTRA_TASK_ID, NO_TASK_ID)
                if (taskId != NO_TASK_ID) {
                    WidgetTaskStore(context).markCompleted(taskId)
                    refreshWidgets(context)
                }
            }

            ACTION_RESET_TASKS -> {
                WidgetTaskStore(context).reset()
                refreshWidgets(context)
            }

            else -> super.onReceive(context, intent)
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
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_task_list)
    }

    companion object {
        const val ACTION_TOGGLE_TASK = "com.joako2004.notesapp.widget.ACTION_TOGGLE_TASK"
        const val ACTION_RESET_TASKS = "com.joako2004.notesapp.widget.ACTION_RESET_TASKS"
        const val EXTRA_TASK_ID = "com.joako2004.notesapp.widget.EXTRA_TASK_ID"

        private const val NO_TASK_ID = -1
        private const val REQUEST_TOGGLE_TASK = 1
        private const val REQUEST_RESET_TASKS = 2
    }
}
