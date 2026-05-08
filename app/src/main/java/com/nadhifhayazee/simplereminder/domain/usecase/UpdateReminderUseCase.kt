package com.nadhifhayazee.simplereminder.domain.usecase

import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.notification.NotificationScheduler
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
import com.nadhifhayazee.simplereminder.domain.widget.WidgetUpdater
import javax.inject.Inject

class UpdateReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val widgetUpdater: WidgetUpdater,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(reminder: Reminder) {
        repository.updateReminder(reminder)
        notificationScheduler.scheduleNotification(reminder)
        widgetUpdater.updateWidget()
    }
}
