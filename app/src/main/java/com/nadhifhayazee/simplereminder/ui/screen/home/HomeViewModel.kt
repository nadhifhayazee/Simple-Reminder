package com.nadhifhayazee.simplereminder.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.notification.NotificationScheduler
import com.nadhifhayazee.simplereminder.domain.usecase.AddReminderUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.DeleteReminderUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.GetRemindersUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.UpdateReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val addReminderUseCase: AddReminderUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        handleIntent(HomeIntent.LoadReminders)
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadReminders -> loadReminders()
            is HomeIntent.AddReminder -> addReminder(intent.name)
            is HomeIntent.UpdateReminder -> updateReminder(intent.reminder)
            is HomeIntent.SearchQueryChanged -> updateSearchQuery(intent.query)
            is HomeIntent.StatusFilterChanged -> updateStatusFilter(intent.status)
            is HomeIntent.DateFilterChanged -> updateDateFilter(intent.startDate, intent.endDate)
            is HomeIntent.ClearFilters -> clearFilters()
        }
    }

    private fun loadReminders() {
        getRemindersUseCase()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { reminders ->
                _state.update {
                    it.copy(
                        reminders = reminders,
                        filteredReminders = applyFilters(reminders, it),
                        isLoading = false
                    )
                }
            }
            .catch { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effect.emit(HomeEffect.ShowError(e.message ?: "Unknown error"))
            }
            .launchIn(viewModelScope)
    }

    private fun addReminder(name: String) {
        viewModelScope.launch {
            try {
                val deadline = System.currentTimeMillis() + 60 * 60 * 1000 // 1 hour from now
                val reminder = Reminder(
                    name = name,
                    deadline = deadline,
                    status = ReminderStatus.TODO
                )
                val id = addReminderUseCase(reminder)
                notificationScheduler.scheduleNotification(reminder.copy(id = id.toInt()))
            } catch (e: Exception) {
                _effect.emit(HomeEffect.ShowError(e.message ?: "Failed to add reminder"))
            }
        }
    }

    private fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                if (reminder.status == ReminderStatus.DONE) {
                    deleteReminderUseCase(reminder)
                    notificationScheduler.cancelNotification(reminder)
                } else {
                    updateReminderUseCase(reminder)
                }
            } catch (e: Exception) {
                _effect.emit(HomeEffect.ShowError(e.message ?: "Failed to update reminder"))
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.update {
            it.copy(
                searchQuery = query,
                filteredReminders = applyFilters(it.reminders, it.copy(searchQuery = query))
            )
        }
    }

    private fun updateStatusFilter(status: ReminderStatus?) {
        val newStatus = if (_state.value.statusFilter == status) null else status
        _state.update {
            it.copy(
                statusFilter = newStatus,
                filteredReminders = applyFilters(it.reminders, it.copy(statusFilter = newStatus))
            )
        }
    }

    private fun updateDateFilter(startDate: Long?, endDate: Long?) {
        _state.update {
            it.copy(
                dateFilterStartDate = startDate,
                dateFilterEndDate = endDate,
                filteredReminders = applyFilters(
                    it.reminders,
                    it.copy(dateFilterStartDate = startDate, dateFilterEndDate = endDate)
                )
            )
        }
    }

    private fun clearFilters() {
        _state.update {
            it.copy(
                searchQuery = "",
                statusFilter = null,
                dateFilterStartDate = null,
                dateFilterEndDate = null,
                filteredReminders = it.reminders
            )
        }
    }

    private fun applyFilters(reminders: List<Reminder>, state: HomeState): List<Reminder> {
        var result = reminders

        // Filter by search query
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            result = result.filter { it.name.lowercase().contains(query) }
        }

        // Filter by status
        if (state.statusFilter != null) {
            result = result.filter { it.status == state.statusFilter }
        }

        // Filter by date range
        state.dateFilterStartDate?.let { start ->
            result = result.filter { it.deadline >= start }
        }
        state.dateFilterEndDate?.let { end ->
            // End date should be inclusive of the whole day
            val endOfDay = end + 24 * 60 * 60 * 1000 - 1
            result = result.filter { it.deadline <= endOfDay }
        }

        return result
    }
}
