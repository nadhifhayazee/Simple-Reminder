package com.nadhifhayazee.simplereminder.ui.screen.home

import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.usecase.GroupedReminders

sealed class HomeIntent {
    data object LoadReminders : HomeIntent()
    data class AddReminder(val name: String) : HomeIntent()
    data class UpdateReminder(val reminder: Reminder) : HomeIntent()
}

data class HomeState(
    val groupedReminders: GroupedReminders = GroupedReminders(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class HomeEffect {
    data class ShowError(val message: String) : HomeEffect()
}
