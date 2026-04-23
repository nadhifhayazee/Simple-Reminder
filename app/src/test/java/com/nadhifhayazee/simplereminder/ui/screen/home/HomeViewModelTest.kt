package com.nadhifhayazee.simplereminder.ui.screen.home

import app.cash.turbine.test
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.notification.NotificationScheduler
import com.nadhifhayazee.simplereminder.domain.usecase.AddReminderUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.DeleteReminderUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.GetRemindersUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.UpdateReminderUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val getRemindersUseCase: GetRemindersUseCase = mockk()
    private val addReminderUseCase: AddReminderUseCase = mockk()
    private val updateReminderUseCase: UpdateReminderUseCase = mockk()
    private val deleteReminderUseCase: DeleteReminderUseCase = mockk()
    private val notificationScheduler: NotificationScheduler = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should load reminders`() = runTest {
        // Given
        val reminders = listOf(
            Reminder(id = 1, name = "Task 1", deadline = 100L, status = ReminderStatus.TODO)
        )
        every { getRemindersUseCase() } returns flowOf(reminders)

        // When
        val viewModel = HomeViewModel(
            getRemindersUseCase,
            addReminderUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            notificationScheduler
        )

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(reminders, state.reminders)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `AddReminder intent should call addReminderUseCase`() = runTest {
        // Given
        val reminders = emptyList<Reminder>()
        every { getRemindersUseCase() } returns flowOf(reminders)
        coEvery { addReminderUseCase(any()) } returns 1L
        coEvery { notificationScheduler.scheduleNotification(any()) } returns Unit

        val viewModel = HomeViewModel(
            getRemindersUseCase,
            addReminderUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            notificationScheduler
        )

        // When
        viewModel.handleIntent(HomeIntent.AddReminder("New Task"))

        // Then
        coEvery { addReminderUseCase(match { it.name == "New Task" }) }
    }
}
