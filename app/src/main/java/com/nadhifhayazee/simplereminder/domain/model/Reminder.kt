package com.nadhifhayazee.simplereminder.domain.model

import java.util.Calendar

enum class ReminderStatus(val displayName: String) {
    TODO("Todo"),
    IN_PROGRESS("In Progress"),
    DONE("Done")
}

enum class RepeatInterval(val displayName: String) {
    NONE("Once"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

data class Reminder(
    val id: Int = 0,
    val name: String,
    val deadline: Long, // timestamp
    val status: ReminderStatus = ReminderStatus.TODO,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val repeatDays: List<Int>? = null, // for WEEKLY: 1=Sun, 2=Mon...; for MONTHLY: day of month
    val createdAt: Long = System.currentTimeMillis()
) {
    fun calculateNextOccurrence(): Long? {
        if (repeatInterval == RepeatInterval.NONE) return null

        val calendar = Calendar.getInstance().apply {
            timeInMillis = deadline
        }

        when (repeatInterval) {
            RepeatInterval.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            RepeatInterval.WEEKLY -> {
                if (repeatDays.isNullOrEmpty()) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1)
                } else {
                    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    val sortedDays = repeatDays.sorted()
                    val nextDay = sortedDays.firstOrNull { it > currentDayOfWeek } ?: sortedDays.first()
                    
                    val daysToAdd = if (nextDay > currentDayOfWeek) {
                        nextDay - currentDayOfWeek
                    } else {
                        7 - currentDayOfWeek + nextDay
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
                }
            }
            RepeatInterval.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                repeatDays?.firstOrNull()?.let { day ->
                    calendar.set(Calendar.DAY_OF_MONTH, day.coerceIn(1, 28)) // Coerce to be safe across months
                }
            }
            else -> return null
        }

        // If calculated time is still in the past (e.g., if we were very late processing), 
        // keep adding until it's in the future.
        while (calendar.timeInMillis <= System.currentTimeMillis()) {
            when (repeatInterval) {
                RepeatInterval.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                RepeatInterval.WEEKLY -> {
                    if (repeatDays.isNullOrEmpty()) {
                        calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    } else {
                        // For simplicity in the loop, just move 1 day and check if it's in repeatDays
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        while (!repeatDays.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                }
                RepeatInterval.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                else -> break
            }
        }

        return calendar.timeInMillis
    }
}
