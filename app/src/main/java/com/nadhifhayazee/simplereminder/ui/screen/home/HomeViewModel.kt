package com.nadhifhayazee.simplereminder.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.usecase.AddReminderUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.DeleteReminderUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.GetGroupedRemindersUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.GetRemindersUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.GroupedReminders
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
    private val getGroupedRemindersUseCase: GetGroupedRemindersUseCase
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
        }
    }

    private fun loadReminders() {
        getRemindersUseCase()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { reminders ->
                val grouped = getGroupedRemindersUseCase(reminders)
                _state.update { 
                    it.copy(
                        groupedReminders = GroupedReminders(
                            today = grouped.today,
                            daily = grouped.daily,
                            weekly = grouped.weekly,
                            monthly = grouped.monthly,
                            upcoming = grouped.upcoming
                        ), 
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
                addReminderUseCase(reminder)
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
                } else {
                    updateReminderUseCase(reminder)
                }
            } catch (e: Exception) {
                _effect.emit(HomeEffect.ShowError(e.message ?: "Failed to update reminder"))
            }
        }
    }
}