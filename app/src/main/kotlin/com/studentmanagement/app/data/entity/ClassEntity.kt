package com.studentmanagement.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subject: String? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val scheduleDaysOfWeek: String = "", // JSON serialized list
    val startTimeMinutes: Int? = null,
    val durationMinutes: Int? = null,
    val repeatInterval: Int = 1, // Số lần lặp (1, 2, 3...)
    val repeatUnit: String = "WEEK" // WEEK, MONTH, YEAR
)
