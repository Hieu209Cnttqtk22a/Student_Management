package com.studentmanagement.app.data.repository

import com.studentmanagement.app.data.dao.ReminderDao
import com.studentmanagement.app.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for managing reminder data operations.
 * Requirements: 7.1, 10.4
 */
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {
    /**
     * Insert a new reminder into the database.
     * Requirement: 7.1
     * 
     * @param reminderEntity The reminder to insert
     * @return The ID of the inserted reminder
     */
    suspend fun insertReminder(reminderEntity: ReminderEntity): Long {
        return reminderDao.insert(reminderEntity)
    }

    /**
     * Update an existing reminder.
     * Requirement: 7.1
     * 
     * @param reminderEntity The reminder to update
     */
    suspend fun updateReminder(reminderEntity: ReminderEntity) {
        reminderDao.update(reminderEntity)
    }

    /**
     * Delete a reminder from the database.
     * Requirement: 7.1
     * 
     * @param reminderEntity The reminder to delete
     */
    suspend fun deleteReminder(reminderEntity: ReminderEntity) {
        reminderDao.delete(reminderEntity)
    }

    /**
     * Get a reminder by its ID.
     * Requirement: 7.1
     * 
     * @param id The reminder ID
     * @return The reminder entity or null if not found
     */
    suspend fun getReminderById(id: Long): ReminderEntity? {
        return reminderDao.getById(id)
    }

    /**
     * Get all reminders for a specific class.
     * Requirement: 7.1
     * 
     * @param classId The class ID
     * @return List of reminders for the class
     */
    suspend fun getRemindersForClass(classId: Long): List<ReminderEntity> {
        return reminderDao.getRemindersForClass(classId)
    }

    /**
     * Get pending (not yet delivered) reminders for a specific class.
     * Requirement: 7.1, 10.4
     * 
     * @param classId The class ID
     * @return List of pending reminders for the class
     */
    suspend fun getPendingRemindersForClass(classId: Long): List<ReminderEntity> {
        return reminderDao.getPendingRemindersForClass(classId)
    }

    /**
     * Mark a reminder as delivered.
     * Requirement: 10.4
     * 
     * @param reminderId The reminder ID to mark as delivered
     */
    suspend fun markReminderAsDelivered(reminderId: Long) {
        reminderDao.markReminderAsDelivered(reminderId)
    }

    /**
     * Delete all reminders for a specific class.
     * Requirement: 7.1
     * 
     * @param classId The class ID
     */
    suspend fun deleteRemindersForClass(classId: Long) {
        reminderDao.deleteRemindersForClass(classId)
    }

    /**
     * Get all pending reminders across all classes as a Flow.
     * Requirement: 7.1, 10.4
     * 
     * @return Flow of pending reminders ordered by scheduled time
     */
    fun getAllPendingReminders(): Flow<List<ReminderEntity>> {
        return reminderDao.getAllPendingReminders()
    }
}
