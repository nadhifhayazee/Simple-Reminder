package com.nadhifhayazee.simplereminder.ui.screen.home

import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus

sealed class HomeIntent {
    data object LoadReminders : HomeIntent()
    data class AddReminder(val name: String) : HomeIntent()
    data class UpdateReminder(val reminder: Reminder) : HomeIntent()
    data class SearchQueryChanged(val query: String) : HomeIntent()
    data class StatusFilterChanged(val status: ReminderStatus?) : HomeIntent()
    data class DateFilterChanged(val startDate: Long?, val endDate: Long?) : HomeIntent()
    data object ClearFilters : HomeIntent()
}

data class HomeState(
    val reminders: List<Reminder> = emptyList(),
    val filteredReminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val statusFilter: ReminderStatus? = null,
    val dateFilterStartDate: Long? = null,
    val dateFilterEndDate: Long? = null
)

sealed class HomeEffect {
    data class ShowError(val message: String) : HomeEffect()
}
