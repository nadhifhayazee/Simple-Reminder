package com.nadhifhayazee.simplereminder.data.mapper

import com.nadhifhayazee.simplereminder.data.local.entity.ReminderEntity
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderMapperTest {

    @Test
    fun `ReminderEntity toDomain correctly maps all fields`() {
        val entity = ReminderEntity(
            id = 1,
            name = "Test Reminder",
            deadline = 123456789L,
            status = "TODO",
            createdAt = 987654321L
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.deadline, domain.deadline)
        assertEquals(ReminderStatus.TODO, domain.status)
        assertEquals(entity.createdAt, domain.createdAt)
    }

    @Test
    fun `Reminder toEntity correctly maps all fields`() {
        val domain = Reminder(
            id = 1,
            name = "Test Reminder",
            deadline = 123456789L,
            status = ReminderStatus.IN_PROGRESS,
            createdAt = 987654321L
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.deadline, entity.deadline)
        assertEquals(domain.status.name, entity.status)
        assertEquals(domain.createdAt, entity.createdAt)
    }
}
