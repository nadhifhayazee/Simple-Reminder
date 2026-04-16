package com.nadhifhayazee.simplereminder.data.repository

import com.nadhifhayazee.simplereminder.data.local.dao.ReminderDao
import com.nadhifhayazee.simplereminder.data.mapper.toDomain
import com.nadhifhayazee.simplereminder.data.mapper.toEntity
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao
) : ReminderRepository {
    override fun getReminders(): Flow<List<Reminder>> {
        return dao.getReminders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReminderById(id: Int): Reminder? {
        return dao.getReminderById(id)?.toDomain()
    }

    override suspend fun addReminder(reminder: Reminder): Long {
        return dao.insertReminder(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: Reminder) {
        dao.updateReminder(reminder.toEntity())
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        dao.deleteReminder(reminder.toEntity())
    }
}
