package com.nadhifhayazee.simplereminder.ui.screen.home

import app.cash.turbine.test
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.usecase.*
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
    private val getGroupedRemindersUseCase: GetGroupedRemindersUseCase = mockk()

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
    fun `initial state should load reminders and group them`() = runTest {
        // Given
        val reminders = listOf(
            Reminder(id = 1, name = "Task 1", deadline = 100L, status = ReminderStatus.TODO)
        )
        val grouped = GroupedReminders(today = reminders)
        
        every { getRemindersUseCase() } returns flowOf(reminders)
        every { getGroupedRemindersUseCase(reminders) } returns grouped

        // When
        val viewModel = HomeViewModel(
            getRemindersUseCase,
            addReminderUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            getGroupedRemindersUseCase
        )

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(grouped, state.groupedReminders)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `AddReminder intent should call addReminderUseCase`() = runTest {
        // Given
        val reminders = emptyList<Reminder>()
        every { getRemindersUseCase() } returns flowOf(reminders)
        every { getGroupedRemindersUseCase(any()) } returns GroupedReminders()
        coEvery { addReminderUseCase(any()) } returns 1L

        val viewModel = HomeViewModel(
            getRemindersUseCase,
            addReminderUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            getGroupedRemindersUseCase
        )

        // When
        viewModel.handleIntent(HomeIntent.AddReminder("New Task"))

        // Then
        coEvery { addReminderUseCase(match { it.name == "New Task" }) }
    }
}