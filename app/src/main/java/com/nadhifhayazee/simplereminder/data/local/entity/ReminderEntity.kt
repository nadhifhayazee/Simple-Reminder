package com.nadhifhayazee.simplereminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val deadline: Long,
    val status: String,
    val repeatInterval: String = "NONE",
    val repeatDays: String? = null, // Comma-separated integers
    val createdAt: Long
)
