package com.nadhifhayazee.simplereminder.ui.screen.edit

import app.cash.turbine.test
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.model.RepeatInterval
import com.nadhifhayazee.simplereminder.domain.usecase.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class EditViewModelTest {

    private val getReminderByIdUseCase: GetReminderByIdUseCase = mockk()
    private val updateReminderUseCase: UpdateReminderUseCase = mockk()
    private val deleteReminderUseCase: DeleteReminderUseCase = mockk()
    private val getFutureDeadlineUseCase = GetFutureDeadlineUseCase()

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
    fun `UpdateRepeatDays monthly with past day should rollover to next month`() = runTest {
        // Given
        val now = Calendar.getInstance()
        val pastDay = if (now.get(Calendar.DAY_OF_MONTH) > 1) now.get(Calendar.DAY_OF_MONTH) - 1 else 28
        
        val reminder = Reminder(
            id = 1,
            name = "Monthly Task",
            deadline = now.timeInMillis,
            repeatInterval = RepeatInterval.MONTHLY
        )
        coEvery { getReminderByIdUseCase(1) } returns reminder

        val viewModel = EditViewModel(
            getReminderByIdUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            getFutureDeadlineUseCase
        )
        viewModel.handleIntent(EditIntent.LoadReminder(1))

        // When
        viewModel.handleIntent(EditIntent.UpdateRepeatDays(listOf(pastDay)))

        // Then
        viewModel.state.test {
            val state = awaitItem()
            val deadlineCal = Calendar.getInstance().apply { timeInMillis = state.reminder!!.deadline }
            
            assertTrue(state.reminder!!.deadline > now.timeInMillis)
            assertEquals(pastDay, deadlineCal.get(Calendar.DAY_OF_MONTH))
        }
    }

    @Test
    fun `UpdateDeadline with past time for daily reminder should rollover to tomorrow`() = runTest {
        // Given
        val now = Calendar.getInstance()
        val pastTime = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.MINUTE, -30)
        }
        
        val reminder = Reminder(
            id = 1,
            name = "Daily Task",
            deadline = now.timeInMillis,
            repeatInterval = RepeatInterval.DAILY
        )
        coEvery { getReminderByIdUseCase(1) } returns reminder

        val viewModel = EditViewModel(
            getReminderByIdUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            getFutureDeadlineUseCase
        )
        viewModel.handleIntent(EditIntent.LoadReminder(1))

        // When
        viewModel.handleIntent(EditIntent.UpdateDeadline(pastTime.timeInMillis))

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.reminder!!.deadline > now.timeInMillis)
            
            val deadlineCal = Calendar.getInstance().apply { timeInMillis = state.reminder!!.deadline }
            val expectedCal = (pastTime.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            
            assertEquals(expectedCal.get(Calendar.DAY_OF_YEAR), deadlineCal.get(Calendar.DAY_OF_YEAR))
        }
    }

    @Test
    fun `SaveReminder with DONE status should delete reminder`() = runTest {
        // Given
        val reminder = Reminder(
            id = 1,
            name = "Task to complete",
            deadline = System.currentTimeMillis() + 100000,
            status = ReminderStatus.DONE
        )
        coEvery { getReminderByIdUseCase(1) } returns reminder
        coEvery { deleteReminderUseCase(any()) } returns Unit

        val viewModel = EditViewModel(
            getReminderByIdUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            getFutureDeadlineUseCase
        )
        viewModel.handleIntent(EditIntent.LoadReminder(1))

        // When
        viewModel.handleIntent(EditIntent.SaveReminder)

        // Then
        coVerify { deleteReminderUseCase(match { it.id == 1 }) }
    }
}