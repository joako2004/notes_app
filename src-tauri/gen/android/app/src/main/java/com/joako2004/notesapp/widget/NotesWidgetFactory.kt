package com.joako2004.notesapp.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.joako2004.notesapp.R

class NotesWidgetFactory(
    private val context: Context,
) : RemoteViewsService.RemoteViewsFactory {

    private var pendingTasks: List<PendingTask> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        pendingTasks = SharedDbReader(context).readPendingTasks()
    }

    override fun onDestroy() {
        pendingTasks = emptyList()
    }

    override fun getCount(): Int = pendingTasks.size

    override fun getViewAt(position: Int): RemoteViews? {
        val task = pendingTasks.getOrNull(position) ?: return null
        val views = RemoteViews(context.packageName, R.layout.widget_task_row)
        views.setTextViewText(R.id.widget_task_text, task.title)
        views.setContentDescription(
            R.id.widget_task_check,
            context.getString(R.string.widget_mark_done, task.title),
        )
        views.setOnClickFillInIntent(
            R.id.widget_task_check,
            Intent().putExtra(NotesWidgetProvider.EXTRA_TASK_ID, task.id),
        )
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false
}
