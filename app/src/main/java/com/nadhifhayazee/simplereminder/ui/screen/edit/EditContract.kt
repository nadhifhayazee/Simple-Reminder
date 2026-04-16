package com.nadhifhayazee.simplereminder.ui.screen.edit

import com.nadhifhayazee.simplereminder.domain.model.Reminder

sealed class EditIntent {
    data class LoadReminder(val id: Int) : EditIntent()
    data class UpdateName(val name: String) : EditIntent()
    data class UpdateDeadline(val deadline: Long) : EditIntent()
    data object SaveReminder : EditIntent()
}

data class EditState(
    val reminder: Reminder? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
