package com.nadhifhayazee.simplereminder.domain.usecase

import com.nadhifhayazee.simplereminder.data.widget.WidgetUpdater
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
import javax.inject.Inject

class DeleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(reminder: Reminder) {
        repository.deleteReminder(reminder)
        widgetUpdater.updateWidget()
    }
}
