package com.joako2004.notesapp.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.joako2004.notesapp.R

class NotesWidgetFactory(
    private val context: Context,
) : RemoteViewsService.RemoteViewsFactory {

    private var pendingTaskTitles: List<String> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        pendingTaskTitles = SharedDbReader(context).readPendingTaskTitles()
    }

    override fun onDestroy() {
        pendingTaskTitles = emptyList()
    }

    override fun getCount(): Int = pendingTaskTitles.size

    override fun getViewAt(position: Int): RemoteViews? {
        val taskTitle = pendingTaskTitles.getOrNull(position) ?: return null
        val views = RemoteViews(context.packageName, R.layout.widget_task_row)
        views.setTextViewText(R.id.widget_task_text, taskTitle)
        views.setContentDescription(
            R.id.widget_task_check,
            context.getString(R.string.widget_mark_done, taskTitle),
        )
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false
}
