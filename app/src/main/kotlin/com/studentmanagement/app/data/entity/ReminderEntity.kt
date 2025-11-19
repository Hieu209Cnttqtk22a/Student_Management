package com.studentmanagement.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId"])]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val scheduledTime: Long, // Timestamp when reminder should trigger (milliseconds)
    val leadTimeMinutes: Int, // How many minutes before class
    val isDelivered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
