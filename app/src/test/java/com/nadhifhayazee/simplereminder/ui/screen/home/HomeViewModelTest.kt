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
import org.junit.Assert.assertTrue
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

    private val testReminders = listOf(
        Reminder(id = 1, name = "Buy groceries", deadline = 1714176000000L, status = ReminderStatus.TODO), // 2024-04-27 00:00:00
        Reminder(id = 2, name = "Meeting prep", deadline = 1714262400000L, status = ReminderStatus.IN_PROGRESS), // 2024-04-28 00:00:00
        Reminder(id = 3, name = "Submit report", deadline = 1714348800000L, status = ReminderStatus.TODO), // 2024-04-29 00:00:00
        Reminder(id = 4, name = "Code review", deadline = 1714435200000L, status = ReminderStatus.DONE) // 2024-04-30 00:00:00
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        every { getRemindersUseCase() } returns flowOf(testReminders)
        return HomeViewModel(
            getRemindersUseCase,
            addReminderUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            notificationScheduler
        )
    }

    @Test
    fun `initial state should load reminders`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(testReminders, state.reminders)
            assertEquals(testReminders, state.filteredReminders)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `AddReminder intent should call addReminderUseCase`() = runTest {
        every { getRemindersUseCase() } returns flowOf(emptyList<Reminder>())
        coEvery { addReminderUseCase(any()) } returns 1L
        coEvery { notificationScheduler.scheduleNotification(any()) } returns Unit

        val viewModel = HomeViewModel(
            getRemindersUseCase,
            addReminderUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            notificationScheduler
        )

        viewModel.handleIntent(HomeIntent.AddReminder("New Task"))

        coEvery { addReminderUseCase(match { it.name == "New Task" }) }
    }

    @Test
    fun `SearchQueryChanged should filter reminders by name`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.SearchQueryChanged("buy"))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.filteredReminders.size)
            assertEquals("Buy groceries", state.filteredReminders[0].name)
            assertEquals("buy", state.searchQuery)
        }
    }

    @Test
    fun `SearchQueryChanged should be case insensitive`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.SearchQueryChanged("MEETING"))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.filteredReminders.size)
            assertEquals("Meeting prep", state.filteredReminders[0].name)
        }
    }

    @Test
    fun `SearchQueryChanged with empty query should show all reminders`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.SearchQueryChanged("buy"))
        viewModel.handleIntent(HomeIntent.SearchQueryChanged(""))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(testReminders.size, state.filteredReminders.size)
        }
    }

    @Test
    fun `StatusFilterChanged should filter reminders by status`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.StatusFilterChanged(ReminderStatus.TODO))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(2, state.filteredReminders.size)
            assertTrue(state.filteredReminders.all { it.status == ReminderStatus.TODO })
            assertEquals(ReminderStatus.TODO, state.statusFilter)
        }
    }

    @Test
    fun `StatusFilterChanged with same status should toggle off`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.StatusFilterChanged(ReminderStatus.TODO))
        // Same status again should toggle off
        viewModel.handleIntent(HomeIntent.StatusFilterChanged(ReminderStatus.TODO))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(null, state.statusFilter)
            assertEquals(testReminders.size, state.filteredReminders.size)
        }
    }

    @Test
    fun `StatusFilterChanged with different status should replace filter`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.StatusFilterChanged(ReminderStatus.TODO))
        viewModel.handleIntent(HomeIntent.StatusFilterChanged(ReminderStatus.IN_PROGRESS))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(ReminderStatus.IN_PROGRESS, state.statusFilter)
            assertEquals(1, state.filteredReminders.size)
            assertEquals(ReminderStatus.IN_PROGRESS, state.filteredReminders[0].status)
        }
    }

    @Test
    fun `DateFilterChanged should filter reminders by date range`() = runTest {
        val viewModel = createViewModel()
        val startDate = 1714262400000L // 2024-04-28
        val endDate = 1714348800000L   // 2024-04-29

        viewModel.handleIntent(HomeIntent.DateFilterChanged(startDate, endDate))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(2, state.filteredReminders.size)
            // Should include April 28 and April 29
            assertTrue(state.filteredReminders.any { it.name == "Meeting prep" })
            assertTrue(state.filteredReminders.any { it.name == "Submit report" })
            assertEquals(startDate, state.dateFilterStartDate)
            assertEquals(endDate, state.dateFilterEndDate)
        }
    }

    @Test
    fun `DateFilterChanged with only start date should filter from that date`() = runTest {
        val viewModel = createViewModel()
        val startDate = 1714348800000L // 2024-04-29

        viewModel.handleIntent(HomeIntent.DateFilterChanged(startDate, null))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(2, state.filteredReminders.size)
            assertTrue(state.filteredReminders.all { it.deadline >= startDate })
        }
    }

    @Test
    fun `combined search and status filter should work together`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.SearchQueryChanged("submit"))
        viewModel.handleIntent(HomeIntent.StatusFilterChanged(ReminderStatus.TODO))

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.filteredReminders.size)
            assertEquals("Submit report", state.filteredReminders[0].name)
            assertEquals(ReminderStatus.TODO, state.filteredReminders[0].status)
        }
    }

    @Test
    fun `ClearFilters should reset all filters`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.SearchQueryChanged("buy"))
        viewModel.handleIntent(HomeIntent.StatusFilterChanged(ReminderStatus.TODO))
        viewModel.handleIntent(HomeIntent.DateFilterChanged(1000L, 3000L))
        viewModel.handleIntent(HomeIntent.ClearFilters)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("", state.searchQuery)
            assertEquals(null, state.statusFilter)
            assertEquals(null, state.dateFilterStartDate)
            assertEquals(null, state.dateFilterEndDate)
            assertEquals(testReminders.size, state.filteredReminders.size)
        }
    }

    @Test
    fun `search with no matches should return empty filtered list`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(HomeIntent.SearchQueryChanged("nonexistent"))

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.filteredReminders.isEmpty())
        }
    }

    @Test
    fun `DateFilterChanged should be inclusive of the whole end date`() = runTest {
        // Mock reminders on the same day as the end filter
        val baseTime = 1714176000000L // 2024-04-27 00:00:00 UTC
        val sameDayReminders = listOf(
            Reminder(id = 5, name = "Start of day", deadline = baseTime, status = ReminderStatus.TODO),
            Reminder(id = 6, name = "Middle of day", deadline = baseTime + 12 * 60 * 60 * 1000, status = ReminderStatus.TODO),
            Reminder(id = 7, name = "End of day", deadline = baseTime + 23 * 60 * 60 * 1000 + 59 * 60 * 1000, status = ReminderStatus.TODO),
            Reminder(id = 8, name = "Next day", deadline = baseTime + 24 * 60 * 60 * 1000, status = ReminderStatus.TODO)
        )
        every { getRemindersUseCase() } returns flowOf(sameDayReminders)
        val viewModel = HomeViewModel(
            getRemindersUseCase,
            addReminderUseCase,
            updateReminderUseCase,
            deleteReminderUseCase,
            notificationScheduler
        )

        // Filter for exactly that day
        viewModel.handleIntent(HomeIntent.DateFilterChanged(baseTime, baseTime))

        viewModel.state.test {
            val state = awaitItem()
            // Should include first 3 reminders, but not the 4th
            assertEquals(3, state.filteredReminders.size)
            assertTrue(state.filteredReminders.any { it.name == "Start of day" })
            assertTrue(state.filteredReminders.any { it.name == "Middle of day" })
            assertTrue(state.filteredReminders.any { it.name == "End of day" })
            assertFalse(state.filteredReminders.any { it.name == "Next day" })
        }
    }
}
