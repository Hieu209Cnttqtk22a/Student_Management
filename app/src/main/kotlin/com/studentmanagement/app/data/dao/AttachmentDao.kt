package com.studentmanagement.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.studentmanagement.app.data.entity.AttachmentEntity

@Dao
interface AttachmentDao {
    @Insert
    suspend fun insert(attachmentEntity: AttachmentEntity): Long

    @Delete
    suspend fun delete(attachmentEntity: AttachmentEntity)

    @Query("SELECT * FROM attachments WHERE dailyRecordId = :dailyRecordId ORDER BY createdAt ASC")
    suspend fun getAttachmentsByDailyRecord(dailyRecordId: Long): List<AttachmentEntity>

    @Query("DELETE FROM attachments WHERE dailyRecordId = :dailyRecordId")
    suspend fun deleteAttachmentsByDailyRecord(dailyRecordId: Long)
}
