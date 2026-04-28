package com.nadhifhayazee.simplereminder.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.notification.NotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

import android.util.Log

class ReminderNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleNotification(reminder: Reminder) {
        // Always cancel existing notification for this reminder before scheduling a new one
        cancelNotification(reminder)

        if (reminder.status == ReminderStatus.DONE) {
            Log.d("NotificationScheduler", "Not scheduling for ${reminder.name}: status is DONE")
            return
        }

        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_name", reminder.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 30 minutes before deadline
        val triggerAt = reminder.deadline - 30 * 60 * 1000

        if (triggerAt <= System.currentTimeMillis()) {
            Log.d("NotificationScheduler", "Not scheduling for ${reminder.name}: trigger time $triggerAt is in the past (now is ${System.currentTimeMillis()})")
            return
        }

        Log.d("NotificationScheduler", "Scheduling notification for ${reminder.name} at $triggerAt")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                Log.w("NotificationScheduler", "Cannot schedule exact alarms, falling back to setAndAllowWhileIdle")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    override fun cancelNotification(reminder: Reminder) {
        val intent = Intent(context, ReminderNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
