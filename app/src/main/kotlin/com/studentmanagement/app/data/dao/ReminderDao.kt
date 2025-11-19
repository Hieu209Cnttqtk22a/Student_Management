package com.studentmanagement.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.studentmanagement.app.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminderEntity: ReminderEntity): Long

    @Update
    suspend fun update(reminderEntity: ReminderEntity)

    @Delete
    suspend fun delete(reminderEntity: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE classId = :classId")
    suspend fun getRemindersForClass(classId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE classId = :classId AND isDelivered = 0")
    suspend fun getPendingRemindersForClass(classId: Long): List<ReminderEntity>

    @Query("UPDATE reminders SET isDelivered = 1 WHERE id = :reminderId")
    suspend fun markReminderAsDelivered(reminderId: Long)

    @Query("DELETE FROM reminders WHERE classId = :classId")
    suspend fun deleteRemindersForClass(classId: Long)

    @Query("SELECT * FROM reminders WHERE isDelivered = 0 ORDER BY scheduledTime ASC")
    fun getAllPendingReminders(): Flow<List<ReminderEntity>>
}
