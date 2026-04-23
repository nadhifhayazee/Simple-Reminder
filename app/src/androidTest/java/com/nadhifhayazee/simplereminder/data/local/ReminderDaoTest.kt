package com.nadhifhayazee.simplereminder.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nadhifhayazee.simplereminder.data.local.dao.ReminderDao
import com.nadhifhayazee.simplereminder.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {

    private lateinit var database: ReminderDatabase
    private lateinit var dao: ReminderDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ReminderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.reminderDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetReminder() = runBlocking {
        val entity = ReminderEntity(
            id = 1,
            name = "Test",
            deadline = 100L,
            status = "TODO",
            createdAt = 50L
        )
        dao.insertReminder(entity)

        val result = dao.getReminderById(1)
        assertEquals(entity, result)
    }

    @Test
    fun deleteReminder() = runBlocking {
        val entity = ReminderEntity(
            id = 1,
            name = "Test",
            deadline = 100L,
            status = "TODO",
            createdAt = 50L
        )
        dao.insertReminder(entity)
        dao.deleteReminder(entity)

        val result = dao.getReminderById(1)
        assertNull(result)
    }

    @Test
    fun getRemindersReturnsFlowSortedByCreatedAt() = runBlocking {
        val entity1 = ReminderEntity(1, "Task 1", 100L, "TODO", 10L)
        val entity2 = ReminderEntity(2, "Task 2", 200L, "TODO", 20L)
        
        dao.insertReminder(entity1)
        dao.insertReminder(entity2)

        val reminders = dao.getReminders().first()
        
        assertEquals(2, reminders.size)
        assertEquals(2, reminders[0].id) // Task 2 should be first (createdAt 20 > 10)
        assertEquals(1, reminders[1].id)
    }
}
