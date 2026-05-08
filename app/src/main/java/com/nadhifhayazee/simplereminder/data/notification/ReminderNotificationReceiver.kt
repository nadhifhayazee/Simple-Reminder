package com.nadhifhayazee.simplereminder.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nadhifhayazee.simplereminder.R
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
import com.nadhifhayazee.simplereminder.domain.usecase.UpdateReminderUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ReminderRepository

    @Inject
    lateinit var updateReminderUseCase: UpdateReminderUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", 0)
        
        CoroutineScope(Dispatchers.IO).launch {
            val reminder = repository.getReminderById(reminderId) ?: return@launch
            
            showNotification(context, reminder.id, reminder.name, reminder.deadline)

            // If recurring, schedule the next occurrence
            val nextDeadline = reminder.calculateNextOccurrence()
            if (nextDeadline != null) {
                val updatedReminder = reminder.copy(deadline = nextDeadline)
                updateReminderUseCase(updatedReminder)
            }
        }
    }

    private fun showNotification(context: Context, reminderId: Int, reminderName: String, deadline: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val remainingMinutes = ((deadline - System.currentTimeMillis()) / (60 * 1000)).toInt()
        val contentText = if (remainingMinutes > 0) {
            "Your reminder \"$reminderName\" is due in $remainingMinutes minutes!"
        } else {
            "Your reminder \"$reminderName\" is due now!"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reminder Deadline")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(reminderId, notification)
    }
}
