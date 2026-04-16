package com.nadhifhayazee.simplereminder.data.mapper

import com.nadhifhayazee.simplereminder.data.local.entity.ReminderEntity
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        name = name,
        deadline = deadline,
        status = ReminderStatus.valueOf(status)
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        name = name,
        deadline = deadline,
        status = status.name
    )
}
