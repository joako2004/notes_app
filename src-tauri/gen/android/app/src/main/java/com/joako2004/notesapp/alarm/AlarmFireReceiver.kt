package com.joako2004.notesapp.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.joako2004.notesapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmFireReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmSpikeScheduler.ACTION_FIRE) {
            Log.w(TAG, "acción inesperada: ${intent.action}")
            return
        }

        val store = AlarmSpikeStore(context)
        val scheduledAtMs = store.scheduledAtMs()
        if (scheduledAtMs == null) {
            Log.w(TAG, "disparo sin programación guardada")
            return
        }

        val firedAtMs = System.currentTimeMillis()
        val mode = store.mode()
        store.appendLog(
            "modo=$mode; programada=${formatLocalIso(scheduledAtMs)}; " +
                "disparada=${formatLocalIso(firedAtMs)}; atraso_ms=${firedAtMs - scheduledAtMs}",
        )
        postNotification(context, store)
        store.clearScheduledAt()
        Log.i(TAG, "alarma disparada (modo=$mode)")
    }

    private fun postNotification(context: Context, store: AlarmSpikeStore) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            store.appendLog("sin permiso de notificaciones")
            Log.w(TAG, "sin permiso de notificaciones")
            return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (notificationManager == null) {
            store.appendLog("sin servicio de notificaciones")
            Log.w(TAG, "sin servicio de notificaciones")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context, notificationManager)
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            Notification.Builder(context)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_HIGH)
        }

        val notification = builder
            .setSmallIcon(R.drawable.alarm_notification_icon)
            .setContentTitle(context.getString(R.string.alarm_notification_title))
            .setContentText(context.getString(R.string.alarm_notification_text))
            .setCategory(Notification.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.alarm_channel_description)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun formatLocalIso(timeMs: Long): String =
        SimpleDateFormat(ISO_LOCAL_FORMAT, Locale.US).format(Date(timeMs))

    companion object {
        private const val TAG = "AlarmSpike"
        private const val CHANNEL_ID = "alarm_spike"
        private const val NOTIFICATION_ID = 1
        private const val ISO_LOCAL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    }
}
