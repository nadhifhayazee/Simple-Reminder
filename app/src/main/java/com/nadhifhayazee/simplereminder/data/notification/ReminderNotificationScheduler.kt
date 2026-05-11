package com.nadhifhayazee.simplereminder.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderDefaults
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.notification.NotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class ReminderNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleNotification(reminder: Reminder) {
        // Always cancel existing notification for this reminder before scheduling a new one
        cancelNotification(reminder)

        if (reminder.status == ReminderStatus.DONE) {
            Timber.d("Not scheduling for ${reminder.name}: status is DONE")
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

        val triggerAt = reminder.deadline - ReminderDefaults.NOTIFICATION_LEAD_TIME_MS
        val now = System.currentTimeMillis()

        if (reminder.deadline <= now) {
            Timber.d("Not scheduling for ${reminder.name}: deadline is in the past")
            return
        }

        val finalTriggerAt = if (triggerAt <= now) {
            Timber.d("Trigger time for ${reminder.name} was in the past, scheduling for immediate trigger")
            now + ReminderDefaults.IMMEDIATE_TRIGGER_DELAY_MS
        } else {
            triggerAt
        }

        Timber.d("Scheduling notification for ${reminder.name} at $finalTriggerAt (deadline: ${reminder.deadline})")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    finalTriggerAt,
                    pendingIntent
                )
            } else {
                Timber.w("Cannot schedule exact alarms, falling back to setAndAllowWhileIdle")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    finalTriggerAt,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                finalTriggerAt,
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
