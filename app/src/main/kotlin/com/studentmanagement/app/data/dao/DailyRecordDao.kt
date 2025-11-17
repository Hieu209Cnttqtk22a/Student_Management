package com.studentmanagement.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.studentmanagement.app.data.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {
    @Insert
    suspend fun insert(dailyRecordEntity: DailyRecordEntity): Long

    @Update
    suspend fun update(dailyRecordEntity: DailyRecordEntity)

    @Delete
    suspend fun delete(dailyRecordEntity: DailyRecordEntity)

    @Query("SELECT * FROM daily_records WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getByStudentAndDate(studentId: Long, date: String): DailyRecordEntity?

    @Query("SELECT * FROM daily_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getRecordsByStudent(studentId: Long): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsByStudentAndDateRange(
        studentId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records WHERE classId = :classId AND date = :date ORDER BY studentId ASC")
    fun getRecordsByClassAndDate(classId: Long, date: String): Flow<List<DailyRecordEntity>>
}
