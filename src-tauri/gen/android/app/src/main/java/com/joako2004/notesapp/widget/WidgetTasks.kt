package com.joako2004.notesapp.widget

import com.joako2004.notesapp.R

data class WidgetTask(val id: Int, val labelResId: Int)

object WidgetTasks {
    val testTasks: List<WidgetTask> = listOf(
        WidgetTask(id = 1, labelResId = R.string.widget_task_water),
        WidgetTask(id = 2, labelResId = R.string.widget_task_read),
        WidgetTask(id = 3, labelResId = R.string.widget_task_stretch),
        WidgetTask(id = 4, labelResId = R.string.widget_task_email),
        WidgetTask(id = 5, labelResId = R.string.widget_task_walk),
        WidgetTask(id = 6, labelResId = R.string.widget_task_meditate),
    )
}
