package com.nadhifhayazee.simplereminder.domain.usecase

import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRemindersUseCaseTest {

    private val repository: ReminderRepository = mockk()
    private val getRemindersUseCase = GetRemindersUseCase(repository)

    @Test
    fun `invoke should return reminders from repository`() = runTest {
        // Given
        val reminders = listOf(
            Reminder(id = 1, name = "Task 1", deadline = 100L, status = ReminderStatus.TODO),
            Reminder(id = 2, name = "Task 2", deadline = 200L, status = ReminderStatus.DONE)
        )
        every { repository.getReminders() } returns flowOf(reminders)

        // When
        val result = getRemindersUseCase()

        // Then
        result.collect { collectedReminders ->
            assertEquals(reminders, collectedReminders)
        }
    }
}
