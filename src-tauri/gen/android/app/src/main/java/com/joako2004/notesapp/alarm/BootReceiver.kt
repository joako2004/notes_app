package com.joako2004.notesapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            Log.w(TAG, "acción inesperada: ${intent.action}")
            return
        }

        val store = AlarmSpikeStore(context)
        val scheduledAtMs = store.scheduledAtMs() ?: return
        val mode = store.mode()

        if (scheduledAtMs <= System.currentTimeMillis()) {
            store.appendLog("perdida durante el apagado")
            store.clearSchedule()
            Log.i(TAG, "alarma perdida durante el apagado")
            return
        }

        if (!AlarmSpikeScheduler.canScheduleExactAlarms(context)) {
            store.appendLog("sin permiso de alarmas exactas")
            Log.w(TAG, "sin permiso de alarmas exactas")
            return
        }

        if (!AlarmSpikeScheduler.schedule(context, scheduledAtMs, mode)) {
            store.appendLog("no se pudo reprogramar la alarma")
            Log.w(TAG, "no se pudo reprogramar la alarma")
            return
        }

        store.appendLog("reprogramada tras reinicio")
        Log.i(TAG, "alarma reprogramada tras reinicio")
    }

    companion object {
        private const val TAG = "AlarmSpike"
    }
}
