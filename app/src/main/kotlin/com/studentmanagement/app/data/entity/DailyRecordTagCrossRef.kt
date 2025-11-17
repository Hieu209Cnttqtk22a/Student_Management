package com.studentmanagement.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "daily_record_tag_cross_ref",
    primaryKeys = ["dailyRecordId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = DailyRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["dailyRecordId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tagId")]
)
data class DailyRecordTagCrossRef(
    val dailyRecordId: Long,
    val tagId: Long
)
