package com.nadhifhayazee.simplereminder.domain.usecase

import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.notification.NotificationScheduler
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
import com.nadhifhayazee.simplereminder.domain.widget.WidgetUpdater
import javax.inject.Inject

class AddReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val widgetUpdater: WidgetUpdater,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(reminder: Reminder): Long {
        val id = repository.addReminder(reminder)
        val savedReminder = reminder.copy(id = id.toInt())
        notificationScheduler.scheduleNotification(savedReminder)
        widgetUpdater.updateWidget()
        return id
    }
}
