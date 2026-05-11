package com.nadhifhayazee.simplereminder.ui.screen.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.simplereminder.domain.model.ReminderDefaults
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.model.RepeatInterval
import com.nadhifhayazee.simplereminder.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val getReminderByIdUseCase: GetReminderByIdUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val getFutureDeadlineUseCase: GetFutureDeadlineUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state: StateFlow<EditState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EditEffect>()
    val effect: SharedFlow<EditEffect> = _effect.asSharedFlow()

    fun handleIntent(intent: EditIntent) {
        when (intent) {
            is EditIntent.LoadReminder -> loadReminder(intent.id)
            is EditIntent.UpdateName -> updateName(intent.name)
            is EditIntent.UpdateDeadline -> updateDeadline(intent.deadline)
            is EditIntent.UpdateRepeatInterval -> updateRepeatInterval(intent.interval)
            is EditIntent.UpdateRepeatDays -> updateRepeatDays(intent.days)
            EditIntent.SaveReminder -> saveReminder()
        }
    }

    private fun loadReminder(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val reminder = getReminderByIdUseCase(id)
            _state.update { it.copy(reminder = reminder, isLoading = false) }
        }
    }

    private fun updateName(name: String) {
        _state.update { it.copy(reminder = it.reminder?.copy(name = name)) }
    }

    private fun updateDeadline(deadline: Long) {
        val reminder = _state.value.reminder ?: return
        val finalDeadline = getFutureDeadlineUseCase(deadline, reminder.repeatInterval, reminder.repeatDays)

        if (finalDeadline < System.currentTimeMillis()) {
            viewModelScope.launch {
                _effect.emit(EditEffect.ShowError("Cannot set a reminder in the past"))
            }
            return
        }

        _state.update { it.copy(reminder = it.reminder?.copy(deadline = finalDeadline), error = null) }
    }

    private fun updateRepeatInterval(interval: RepeatInterval) {
        _state.update { 
            val reminder = it.reminder ?: return@update it
            val updatedReminder = reminder.copy(repeatInterval = interval)
            
            val finalDeadline = if (interval == RepeatInterval.NONE && updatedReminder.deadline < System.currentTimeMillis()) {
                System.currentTimeMillis() + ReminderDefaults.DEFAULT_DEADLINE_OFFSET_MS
            } else {
                getFutureDeadlineUseCase(updatedReminder.deadline, interval, updatedReminder.repeatDays)
            }
            
            it.copy(reminder = updatedReminder.copy(deadline = finalDeadline), error = null)
        }
    }

    private fun updateRepeatDays(days: List<Int>) {
        _state.update { 
            val reminder = it.reminder ?: return@update it
            val updatedReminder = reminder.copy(repeatDays = days)
            
            val baseDeadline = if (reminder.repeatInterval == RepeatInterval.MONTHLY && days.isNotEmpty()) {
                Calendar.getInstance().apply {
                    timeInMillis = reminder.deadline
                    set(Calendar.DAY_OF_MONTH, days.first().coerceIn(1, 28))
                }.timeInMillis
            } else {
                reminder.deadline
            }

            val finalDeadline = getFutureDeadlineUseCase(baseDeadline, reminder.repeatInterval, days)
            it.copy(reminder = updatedReminder.copy(deadline = finalDeadline), error = null)
        }
    }

    private fun saveReminder() {
        val reminder = _state.value.reminder ?: return

        if (reminder.deadline < System.currentTimeMillis()) {
            viewModelScope.launch {
                _effect.emit(EditEffect.ShowError("Deadline must be in the future"))
            }
            return
        }

        viewModelScope.launch {
            try {
                if (reminder.status == ReminderStatus.DONE) {
                    deleteReminderUseCase(reminder)
                } else {
                    updateReminderUseCase(reminder)
                }
                _state.update { it.copy(isSaved = true) }
                _effect.emit(EditEffect.ShowSuccess("Reminder updated"))
            } catch (e: Exception) {
                _effect.emit(EditEffect.ShowError(e.message ?: "Failed to save reminder"))
            }
        }
    }
}