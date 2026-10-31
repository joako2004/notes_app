package com.joako2004.notesapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmSpikeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SCHEDULE -> scheduleAlarm(context, intent)
            ACTION_CANCEL -> cancelAlarm(context)
            else -> Log.w(TAG, "acción desconocida: ${intent.action}")
        }
    }

    private fun scheduleAlarm(context: Context, intent: Intent) {
        val delaySeconds = intent.getIntExtra(EXTRA_DELAY_SECONDS, DEFAULT_DELAY_SECONDS)
        val mode = when (intent.getStringExtra(EXTRA_MODE)) {
            AlarmSpikeStore.MODE_EXACT -> AlarmSpikeStore.MODE_EXACT
            else -> AlarmSpikeStore.MODE_CLOCK
        }
        val store = AlarmSpikeStore(context)

        if (!AlarmSpikeScheduler.canScheduleExactAlarms(context)) {
            store.appendLog("sin permiso de alarmas exactas")
            Log.w(TAG, "sin permiso de alarmas exactas")
            return
        }

        val scheduledAtMs = System.currentTimeMillis() + delaySeconds * 1000L
        store.saveSchedule(scheduledAtMs, mode)

        if (!AlarmSpikeScheduler.schedule(context, scheduledAtMs, mode)) {
            store.clearSchedule()
            store.appendLog("no se pudo programar la alarma")
            Log.w(TAG, "no se pudo programar la alarma")
            return
        }

        Log.i(TAG, "alarma programada en $delaySeconds s (modo=$mode)")
    }

    private fun cancelAlarm(context: Context) {
        val store = AlarmSpikeStore(context)
        AlarmSpikeScheduler.cancel(context)
        store.clearSchedule()
        Log.i(TAG, "alarma cancelada")
    }

    companion object {
        const val ACTION_SCHEDULE = "com.joako2004.notesapp.alarm.SPIKE_SCHEDULE"
        const val ACTION_CANCEL = "com.joako2004.notesapp.alarm.SPIKE_CANCEL"
        const val EXTRA_DELAY_SECONDS = "delay_seconds"
        const val EXTRA_MODE = "mode"

        private const val TAG = "AlarmSpike"
        private const val DEFAULT_DELAY_SECONDS = 60
    }
}
