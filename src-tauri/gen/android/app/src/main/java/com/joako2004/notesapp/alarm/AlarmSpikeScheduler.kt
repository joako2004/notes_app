package com.joako2004.notesapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.joako2004.notesapp.MainActivity

object AlarmSpikeScheduler {

    const val ACTION_FIRE = "com.joako2004.notesapp.alarm.SPIKE_FIRE"

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }
        val alarmManager = alarmManager(context) ?: return false
        return alarmManager.canScheduleExactAlarms()
    }

    fun schedule(context: Context, triggerAtMs: Long, mode: String): Boolean {
        val alarmManager = alarmManager(context) ?: return false
        val firePendingIntent = firePendingIntent(context)
        when (mode) {
            AlarmSpikeStore.MODE_CLOCK ->
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMs, showAlarmPendingIntent(context)),
                    firePendingIntent,
                )

            AlarmSpikeStore.MODE_EXACT ->
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMs,
                    firePendingIntent,
                )

            else -> return false
        }
        return true
    }

    fun cancel(context: Context) {
        val alarmManager = alarmManager(context) ?: return
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_FIRE,
            fireIntent(context),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun alarmManager(context: Context): AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private fun fireIntent(context: Context): Intent =
        Intent(context, AlarmFireReceiver::class.java).setAction(ACTION_FIRE)

    private fun firePendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_FIRE,
            fireIntent(context),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun showAlarmPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            REQUEST_SHOW_ALARM,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private const val REQUEST_FIRE = 1
    private const val REQUEST_SHOW_ALARM = 2
}
