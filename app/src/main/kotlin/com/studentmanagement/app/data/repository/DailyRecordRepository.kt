package com.studentmanagement.app.data.repository

import com.studentmanagement.app.data.dao.AttachmentDao
import com.studentmanagement.app.data.dao.DailyRecordDao
import com.studentmanagement.app.data.dao.DailyRecordTagDao
import com.studentmanagement.app.data.entity.AttachmentEntity
import com.studentmanagement.app.data.entity.DailyRecordEntity
import com.studentmanagement.app.data.entity.DailyRecordTagCrossRef
import com.studentmanagement.app.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DailyRecordRepository @Inject constructor(
    private val dailyRecordDao: DailyRecordDao,
    private val dailyRecordTagDao: DailyRecordTagDao,
    private val attachmentDao: AttachmentDao
) {
    suspend fun saveDailyRecord(
        dailyRecord: DailyRecordEntity,
        tags: List<TagEntity>,
        attachments: List<String>
    ): Long {
        val recordId = if (dailyRecord.id == 0L) {
            dailyRecordDao.insert(dailyRecord)
        } else {
            dailyRecordDao.update(dailyRecord)
            dailyRecord.id
        }

        // Update tags
        dailyRecordTagDao.deleteTagsByDailyRecord(recordId)
        tags.forEach { tag ->
            dailyRecordTagDao.insert(DailyRecordTagCrossRef(recordId, tag.id))
        }

        // Update attachments
        attachmentDao.deleteAttachmentsByDailyRecord(recordId)
        attachments.forEach { uri ->
            attachmentDao.insert(AttachmentEntity(dailyRecordId = recordId, uri = uri))
        }

        return recordId
    }

    suspend fun getDailyRecord(studentId: Long, date: String): DailyRecordEntity? {
        return dailyRecordDao.getByStudentAndDate(studentId, date)
    }

    suspend fun getTagsByDailyRecord(dailyRecordId: Long): List<TagEntity> {
        return dailyRecordTagDao.getTagsByDailyRecord(dailyRecordId)
    }

    suspend fun getAttachmentsByDailyRecord(dailyRecordId: Long): List<AttachmentEntity> {
        return attachmentDao.getAttachmentsByDailyRecord(dailyRecordId)
    }

    fun getRecordsByStudent(studentId: Long): Flow<List<DailyRecordEntity>> {
        return dailyRecordDao.getRecordsByStudent(studentId)
    }

    fun getRecordsByStudentAndDateRange(
        studentId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<DailyRecordEntity>> {
        return dailyRecordDao.getRecordsByStudentAndDateRange(studentId, startDate, endDate)
    }

    fun getRecordsByClassAndDate(classId: Long, date: String): Flow<List<DailyRecordEntity>> {
        return dailyRecordDao.getRecordsByClassAndDate(classId, date)
    }

    suspend fun deleteDailyRecord(dailyRecord: DailyRecordEntity) {
        dailyRecordDao.delete(dailyRecord)
    }
}
