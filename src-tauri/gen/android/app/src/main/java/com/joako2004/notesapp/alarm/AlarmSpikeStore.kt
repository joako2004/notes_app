package com.joako2004.notesapp.alarm

import android.content.Context

class AlarmSpikeStore(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun scheduledAtMs(): Long? =
        preferences.getLong(KEY_SCHEDULED_AT_MS, NO_SCHEDULED_AT)
            .takeUnless { it == NO_SCHEDULED_AT }

    fun mode(): String =
        preferences.getString(KEY_MODE, MODE_CLOCK) ?: MODE_CLOCK

    fun saveSchedule(scheduledAtMs: Long, mode: String) {
        preferences.edit()
            .putLong(KEY_SCHEDULED_AT_MS, scheduledAtMs)
            .putString(KEY_MODE, mode)
            .commit()
    }

    fun clearSchedule() {
        preferences.edit()
            .remove(KEY_SCHEDULED_AT_MS)
            .remove(KEY_MODE)
            .commit()
    }

    fun clearScheduledAt() {
        preferences.edit()
            .remove(KEY_SCHEDULED_AT_MS)
            .commit()
    }

    fun appendLog(line: String) {
        val currentLines = preferences.getString(KEY_FIRED_LOG, "")
            .orEmpty()
            .lines()
            .filter { it.isNotBlank() }
        val updatedLog = (currentLines + line).takeLast(MAX_LOG_LINES)
        preferences.edit()
            .putString(KEY_FIRED_LOG, updatedLog.joinToString("\n"))
            .commit()
    }

    companion object {
        const val PREFERENCES_NAME = "alarm_spike"
        const val MODE_CLOCK = "clock"
        const val MODE_EXACT = "exact"

        private const val KEY_SCHEDULED_AT_MS = "scheduled_at_ms"
        private const val KEY_MODE = "mode"
        private const val KEY_FIRED_LOG = "fired_log"
        private const val NO_SCHEDULED_AT = -1L
        private const val MAX_LOG_LINES = 20
    }
}
