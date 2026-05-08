package com.nadhifhayazee.simplereminder.data.mapper

import com.nadhifhayazee.simplereminder.data.local.entity.ReminderEntity
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.model.RepeatInterval

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        name = name,
        deadline = deadline,
        status = ReminderStatus.valueOf(status),
        repeatInterval = RepeatInterval.valueOf(repeatInterval),
        repeatDays = repeatDays?.split(",")?.filter { it.isNotEmpty() }?.map { it.toInt() },
        createdAt = createdAt
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        name = name,
        deadline = deadline,
        status = status.name,
        repeatInterval = repeatInterval.name,
        repeatDays = repeatDays?.joinToString(","),
        createdAt = createdAt
    )
}
