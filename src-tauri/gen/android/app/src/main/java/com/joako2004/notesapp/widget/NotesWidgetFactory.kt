package com.joako2004.notesapp.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.joako2004.notesapp.R

class NotesWidgetFactory(
    private val context: Context,
) : RemoteViewsService.RemoteViewsFactory {

    private var pendingTasks: List<WidgetTask> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        val completedTaskIds = WidgetTaskStore(context).completedTaskIds()
        pendingTasks = WidgetTasks.testTasks.filterNot { it.id in completedTaskIds }
    }

    override fun onDestroy() {
        pendingTasks = emptyList()
    }

    override fun getCount(): Int = pendingTasks.size

    override fun getViewAt(position: Int): RemoteViews? {
        val task = pendingTasks.getOrNull(position) ?: return null
        val taskLabel = context.getString(task.labelResId)
        val views = RemoteViews(context.packageName, R.layout.widget_task_row)
        views.setTextViewText(R.id.widget_task_text, taskLabel)
        views.setContentDescription(
            R.id.widget_task_check,
            context.getString(R.string.widget_mark_done, taskLabel),
        )
        views.setOnClickFillInIntent(
            R.id.widget_task_check,
            Intent().putExtra(NotesWidgetProvider.EXTRA_TASK_ID, task.id),
        )
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        pendingTasks.getOrNull(position)?.id?.toLong() ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
