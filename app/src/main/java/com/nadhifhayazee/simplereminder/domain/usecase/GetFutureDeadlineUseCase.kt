package com.nadhifhayazee.simplereminder.domain.usecase

import com.nadhifhayazee.simplereminder.domain.model.RepeatInterval
import java.util.Calendar
import javax.inject.Inject

class GetFutureDeadlineUseCase @Inject constructor() {
    operator fun invoke(deadline: Long, interval: RepeatInterval, days: List<Int>?): Long {
        if (deadline >= System.currentTimeMillis() || interval == RepeatInterval.NONE) {
            return deadline
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = deadline
        }

        while (calendar.timeInMillis < System.currentTimeMillis()) {
            when (interval) {
                RepeatInterval.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                RepeatInterval.WEEKLY -> {
                    if (days.isNullOrEmpty()) {
                        calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    } else {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        while (!days.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
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