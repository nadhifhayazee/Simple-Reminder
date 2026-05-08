package com.nadhifhayazee.simplereminder.ui.screen.home

import com.nadhifhayazee.simplereminder.domain.model.Reminder

sealed class HomeIntent {
    data object LoadReminders : HomeIntent()
    data class AddReminder(val name: String) : HomeIntent()
    data class UpdateReminder(val reminder: Reminder) : HomeIntent()
}

data class GroupedReminders(
    val today: List<Reminder> = emptyList(),
    val daily: List<Reminder> = emptyList(),
    val weekly: List<Reminder> = emptyList(),
    val monthly: List<Reminder> = emptyList(),
    val upcoming: Map<String, List<Reminder>> = emptyMap()
)

data class HomeState(
    val groupedReminders: GroupedReminders = GroupedReminders(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class HomeEffect {
    data class ShowError(val message: String) : HomeEffect()
}
