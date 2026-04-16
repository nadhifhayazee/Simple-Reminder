package com.nadhifhayazee.simplereminder.ui.screen.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nadhifhayazee.simplereminder.domain.notification.NotificationScheduler
import com.nadhifhayazee.simplereminder.domain.usecase.GetReminderByIdUseCase
import com.nadhifhayazee.simplereminder.domain.usecase.UpdateReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val getReminderByIdUseCase: GetReminderByIdUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state: StateFlow<EditState> = _state.asStateFlow()

    fun handleIntent(intent: EditIntent) {
        when (intent) {
            is EditIntent.LoadReminder -> loadReminder(intent.id)
            is EditIntent.UpdateName -> updateName(intent.name)
            is EditIntent.UpdateDeadline -> updateDeadline(intent.deadline)
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
        _state.update { it.copy(reminder = it.reminder?.copy(deadline = deadline)) }
    }

    private fun saveReminder() {
        val reminder = _state.value.reminder ?: return
        viewModelScope.launch {
            try {
                updateReminderUseCase(reminder)
                // Reschedule notification with new deadline
                notificationScheduler.scheduleNotification(reminder)
                _state.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}
