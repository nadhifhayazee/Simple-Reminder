package com.nadhifhayazee.simplereminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nadhifhayazee.simplereminder.data.local.dao.ReminderDao
import com.nadhifhayazee.simplereminder.data.local.entity.ReminderEntity

@Database(entities = [ReminderEntity::class], version = 2)
abstract class ReminderDatabase : RoomDatabase() {
    abstract val reminderDao: ReminderDao
}
