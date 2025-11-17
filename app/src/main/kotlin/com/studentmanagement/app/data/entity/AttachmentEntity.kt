package com.studentmanagement.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = DailyRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["dailyRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dailyRecordId")]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dailyRecordId: Long,
    val uri: String,
    val createdAt: Long = System.currentTimeMillis()
)
