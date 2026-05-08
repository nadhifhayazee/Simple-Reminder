package com.nadhifhayazee.simplereminder.domain.usecase

import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.RepeatInterval
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class GroupedReminders(
    val today: List<Reminder> = emptyList(),
    val daily: List<Reminder> = emptyList(),
    val weekly: List<Reminder> = emptyList(),
    val monthly: List<Reminder> = emptyList(),
    val upcoming: Map<String, List<Reminder>> = emptyMap()
)

class GetGroupedRemindersUseCase @Inject constructor() {
    operator fun invoke(reminders: List<Reminder>): GroupedReminders {
        val sortedReminders = reminders.sortedBy { it.deadline }
        val now = Calendar.getInstance()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayReminders = sortedReminders.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.deadline }
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }

        val futureReminders = sortedReminders.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.deadline }
            cal.timeInMillis > now.timeInMillis &&
            !(cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
              cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
        }

        val futureDaily = futureReminders.filter { it.repeatInterval == RepeatInterval.DAILY }
        val futureWeekly = futureReminders.filter { it.repeatInterval == RepeatInterval.WEEKLY }
        val futureMonthly = futureReminders.filter { it.repeatInterval == RepeatInterval.MONTHLY }
        val futureOnce = futureReminders.filter { it.repeatInterval == RepeatInterval.NONE }

        val futureOnceGrouped = futureOnce.groupBy {
            SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date(it.deadline))
        }

        return GroupedReminders(
            today = todayReminders,
            daily = futureDaily,
            weekly = futureWeekly,
            monthly = futureMonthly,
            upcoming = futureOnceGrouped
        )
    }
}