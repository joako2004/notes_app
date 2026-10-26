package com.joako2004.notesapp.widget

import android.content.Context

class WidgetTaskStore(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun completedTaskIds(): Set<Int> =
        preferences.getStringSet(KEY_COMPLETED_TASK_IDS, emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()

    fun markCompleted(taskId: Int) {
        val completedTaskIds = completedTaskIds() + taskId
        preferences.edit()
            .putStringSet(
                KEY_COMPLETED_TASK_IDS,
                completedTaskIds.map { it.toString() }.toSet(),
            )
            .apply()
    }

    fun reset() {
        preferences.edit().remove(KEY_COMPLETED_TASK_IDS).apply()
    }

    companion object {
        const val PREFERENCES_NAME = "widget_spike"
        private const val KEY_COMPLETED_TASK_IDS = "completed_task_ids"
    }
}
