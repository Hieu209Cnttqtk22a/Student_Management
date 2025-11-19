package com.studentmanagement.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studentmanagement.app.data.repository.ClassRepository
import com.studentmanagement.app.service.ReminderService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Worker to reschedule reminders after app updates or device reboots.
 * Requirement 12.3: Use WorkManager for reliability across reboots
 */
@HiltWorker
class ReminderRescheduleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val classRepository: ClassRepository,
    private val reminderService: ReminderService
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val TAG = "ReminderRescheduleWorker"
        const val WORK_NAME = "reminder_reschedule_work"
    }
    
    /**
     * Reschedule all reminders for classes that have reminders enabled.
     * Requirement 12.3: Reschedule reminders after app update
     */
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting reminder rescheduling...")
            
            // Get all classes with reminders enabled
            val allClasses = classRepository.getAllClasses().first()
            val classesWithReminders = allClasses.filter { it.reminderEnabled }
            
            Log.d(TAG, "Found ${classesWithReminders.size} classes with reminders enabled")
            
            // Reschedule reminders for each class
            var successCount = 0
            var failureCount = 0
            
            classesWithReminders.forEach { classEntity ->
                try {
                    reminderService.scheduleRemindersForClass(classEntity)
                    successCount++
                    Log.d(TAG, "Successfully rescheduled reminders for class ${classEntity.id}")
                } catch (e: Exception) {
                    failureCount++
                    Log.e(TAG, "Failed to reschedule reminders for class ${classEntity.id}", e)
                }
            }
            
            Log.d(TAG, "Reminder rescheduling completed: $successCount succeeded, $failureCount failed")
            
            // Return success even if some failed, as long as we tried all
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Reminder rescheduling failed", e)
            // Retry on failure
            Result.retry()
        }
    }
}
