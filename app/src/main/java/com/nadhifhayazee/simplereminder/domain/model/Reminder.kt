package com.nadhifhayazee.simplereminder.domain.model

enum class ReminderStatus {
    TODO, IN_PROGRESS, DONE
}

data class Reminder(
    val id: Int = 0,
    val name: String,
    val deadline: Long, // timestamp
    val status: ReminderStatus = ReminderStatus.TODO,
    val createdAt: Long = System.currentTimeMillis()
)
