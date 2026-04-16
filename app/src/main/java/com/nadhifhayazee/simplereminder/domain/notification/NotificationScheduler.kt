package com.nadhifhayazee.simplereminder.domain.notification

import com.nadhifhayazee.simplereminder.domain.model.Reminder

interface NotificationScheduler {
    fun scheduleNotification(reminder: Reminder)
    fun cancelNotification(reminder: Reminder)
}
