package com.studentmanagement.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val displayName: String
)

enum class DailyTag(val code: String, val displayName: String) {
    HAS_SCORE("HAS_SCORE", "Có điểm"),
    LOW_SCORE("LOW_SCORE", "Điểm kém"),
    STUDIED("STUDIED", "Học"),
    MAKEUP("MAKEUP", "Bù bài"),
    ABSENT("ABSENT", "Nghỉ học"),
    NO_HOMEWORK("NO_HOMEWORK", "Chưa làm bài tập"),
    FULL_HOMEWORK("FULL_HOMEWORK", "Đầy đủ bài tập")
}
