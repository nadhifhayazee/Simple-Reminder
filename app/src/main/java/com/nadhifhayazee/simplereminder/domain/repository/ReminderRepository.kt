package com.nadhifhayazee.simplereminder.domain.repository

import com.nadhifhayazee.simplereminder.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getReminders(): Flow<List<Reminder>>
    suspend fun getReminderById(id: Int): Reminder?
    suspend fun addReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
}
