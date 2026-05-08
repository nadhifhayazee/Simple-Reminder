package com.nadhifhayazee.simplereminder.domain.usecase

import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.notification.NotificationScheduler
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
import com.nadhifhayazee.simplereminder.domain.widget.WidgetUpdater
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteReminderUseCaseTest {

    private val repository: ReminderRepository = mockk()
    private val widgetUpdater: WidgetUpdater = mockk()
    private val notificationScheduler: NotificationScheduler = mockk()
    private val deleteReminderUseCase = DeleteReminderUseCase(repository, widgetUpdater, notificationScheduler)

    @Test
    fun `invoke should delete reminder, cancel notification and update widget`() = runTest {
        // Given
        val reminder = Reminder(id = 1, name = "Task 1", deadline = 100L, status = ReminderStatus.TODO)
        coEvery { repository.deleteReminder(reminder) } returns Unit
        every { notificationScheduler.cancelNotification(reminder) } returns Unit
        every { widgetUpdater.updateWidget() } returns Unit

        // When
        deleteReminderUseCase(reminder)

        // Then
        coVerify { repository.deleteReminder(reminder) }
        verify { notificationScheduler.cancelNotification(reminder) }
        verify { widgetUpdater.updateWidget() }
    }
}