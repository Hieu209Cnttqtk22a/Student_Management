package com.studentmanagement.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.entity.ReminderEntity
import com.studentmanagement.app.data.repository.ClassRepository
import com.studentmanagement.app.data.repository.ReminderRepository
import com.studentmanagement.app.receiver.ReminderBroadcastReceiver
import com.studentmanagement.app.util.ScheduleCalculator
import com.studentmanagement.app.worker.ReminderRescheduleWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing class reminders and scheduling notifications.
 * Requirements: 7.1, 7.2, 8.5
 */
@Singleton
class ReminderService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
    private val reminderRepository: ReminderRepository,
    private val classRepository: ClassRepository,
    private val scheduleCalculator: ScheduleCalculator
) {
    
    companion object {
        private const val TAG = "ReminderService"
        private const val ACTION_CLASS_REMINDER = "com.studentmanagement.app.CLASS_REMINDER"
        private const val DAYS_AHEAD = 30 // Schedule reminders for next 30 days
    }
    
    // Requirement 12.2: Use system timezone for all time calculations
    private val systemTimeZone: TimeZone = TimeZone.getDefault()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = systemTimeZone
    }
    
    /**
     * Schedule reminders for all upcoming class sessions.
     * Requirement 7.1, 7.2: Calculate reminder times based on class schedule and lead time
     * 
     * @param classEntity The class to schedule reminders for
     */
    suspend fun scheduleRemindersForClass(classEntity: ClassEntity) {
        if (!classEntity.reminderEnabled) {
            Log.d(TAG, "Reminders not enabled for class ${classEntity.id}")
            return
        }
        
        Log.d(TAG, "Scheduling reminders for class: ${classEntity.name} (ID: ${classEntity.id})")
        
        // Cancel existing reminders first (Requirement 7.3)
        cancelRemindersForClass(classEntity.id)
        
        // Calculate upcoming class sessions
        val upcomingSessions = getUpcomingClassSessions(classEntity)
        
        if (upcomingSessions.isEmpty()) {
            Log.d(TAG, "No upcoming sessions found for class ${classEntity.id}")
            return
        }
        
        Log.d(TAG, "Found ${upcomingSessions.size} upcoming sessions for class ${classEntity.id}")
        
        // Schedule a reminder for each session
        upcomingSessions.forEach { sessionCalendar ->
            // Calculate reminder time based on lead time
            // Requirement 12.2: Clone preserves timezone information
            val reminderCalendar = sessionCalendar.clone() as Calendar
            reminderCalendar.add(Calendar.MINUTE, -classEntity.reminderLeadTimeMinutes)
            
            // Only schedule if reminder time is in the future
            // Requirement 12.2: System time comparison accounts for DST
            if (reminderCalendar.timeInMillis > System.currentTimeMillis()) {
                val reminderEntity = ReminderEntity(
                    classId = classEntity.id,
                    scheduledTime = reminderCalendar.timeInMillis,
                    leadTimeMinutes = classEntity.reminderLeadTimeMinutes,
                    isDelivered = false
                )
                
                val reminderId = reminderRepository.insertReminder(reminderEntity)
                scheduleAlarm(reminderId, reminderCalendar, classEntity)
                
                Log.d(TAG, "Scheduled reminder $reminderId for class ${classEntity.id} at ${dateFormatter.format(reminderCalendar.time)} (timezone: ${systemTimeZone.displayName})")
            }
        }
        
        Log.d(TAG, "Successfully scheduled reminders for class ${classEntity.id}")
    }
    
    /**
     * Cancel all reminders for a specific class.
     * Requirement 7.3, 7.4: Handle class deletion and schedule changes
     * 
     * @param classId The class ID to cancel reminders for
     */
    suspend fun cancelRemindersForClass(classId: Long) {
        Log.d(TAG, "Cancelling reminders for class $classId")
        
        val reminders = reminderRepository.getRemindersForClass(classId)
        reminders.forEach { reminder ->
            cancelAlarm(reminder.id)
        }
        
        reminderRepository.deleteRemindersForClass(classId)
        Log.d(TAG, "Cancelled ${reminders.size} reminders for class $classId")
    }
    
    /**
     * Reschedule reminders when lead time changes.
     * Requirement 7.5: Reschedule when reminder lead time is changed
     * 
     * @param classId The class ID to reschedule reminders for
     */
    suspend fun rescheduleReminders(classId: Long) {
        Log.d(TAG, "Rescheduling reminders for class $classId")
        
        val classEntity = classRepository.getClassById(classId)
        if (classEntity != null) {
            scheduleRemindersForClass(classEntity)
        } else {
            Log.e(TAG, "Class $classId not found, cannot reschedule reminders")
        }
    }
    
    /**
     * Schedule an alarm using AlarmManager.
     * Requirement 8.5: Create PendingIntent for each reminder
     * 
     * @param reminderId The reminder ID
     * @param reminderTime The time to trigger the reminder
     * @param classEntity The class entity
     */
    private fun scheduleAlarm(
        reminderId: Long,
        reminderTime: Calendar,
        classEntity: ClassEntity
    ) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_CLASS_REMINDER
            putExtra("reminder_id", reminderId)
            putExtra("class_id", classEntity.id)
            putExtra("class_name", classEntity.name)
            putExtra("start_time_minutes", classEntity.startTimeMinutes ?: 0)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = reminderTime.timeInMillis
        
        // Use exact alarm for precise timing (Requirement 8.5)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d(TAG, "Scheduled exact alarm for reminder $reminderId")
                } else {
                    // Fallback to inexact alarm if exact alarms not allowed
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.w(TAG, "Scheduled inexact alarm for reminder $reminderId (exact alarms not allowed)")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm for reminder $reminderId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for reminder $reminderId", e)
        }
    }
    
    /**
     * Cancel an alarm using AlarmManager.
     * 
     * @param reminderId The reminder ID to cancel
     */
    private fun cancelAlarm(reminderId: Long) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_CLASS_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        
        Log.d(TAG, "Cancelled alarm for reminder $reminderId")
    }
    
    /**
     * Get upcoming class sessions for the next DAYS_AHEAD days.
     * Requirement 7.1: Calculate all upcoming class sessions
     * Requirement 12.2: Use system timezone and handle DST changes
     * 
     * @param classEntity The class entity
     * @return List of Calendar objects representing class session times
     */
    private fun getUpcomingClassSessions(classEntity: ClassEntity): List<Calendar> {
        val sessions = mutableListOf<Calendar>()
        // Requirement 12.2: Explicitly use system timezone
        val today = Calendar.getInstance(systemTimeZone)
        
        // Calculate schedule dates using ScheduleCalculator
        val scheduleDates = try {
            scheduleCalculator.calculateScheduleDates(
                scheduleDaysOfWeek = classEntity.scheduleDaysOfWeek,
                repeatInterval = classEntity.repeatInterval,
                repeatUnit = classEntity.repeatUnit,
                startDate = today,
                monthsAhead = 1 // Calculate for next month (approximately DAYS_AHEAD)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate schedule dates for class ${classEntity.id}", e)
            return emptyList()
        }
        
        // Convert date strings to Calendar objects with class start time
        val startTimeMinutes = classEntity.startTimeMinutes ?: 0
        val hour = startTimeMinutes / 60
        val minute = startTimeMinutes % 60
        
        scheduleDates.forEach { dateString ->
            try {
                val date = dateFormatter.parse(dateString)
                if (date != null) {
                    // Requirement 12.2: Create Calendar with system timezone
                    // This ensures DST transitions are handled correctly
                    val sessionCalendar = Calendar.getInstance(systemTimeZone).apply {
                        time = date
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    // Only add if session is in the future
                    // Requirement 12.2: Compare using system time which accounts for DST
                    if (sessionCalendar.timeInMillis > System.currentTimeMillis()) {
                        sessions.add(sessionCalendar)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse date $dateString", e)
            }
        }
        
        return sessions
    }
    
    /**
     * Trigger a WorkManager task to reschedule all reminders.
     * Requirement 12.3: Use WorkManager for reliability across reboots
     * 
     * This can be called manually or automatically after app updates/reboots.
     */
    fun triggerReminderReschedule() {
        val workRequest = OneTimeWorkRequestBuilder<ReminderRescheduleWorker>()
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            ReminderRescheduleWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        Log.d(TAG, "Reminder reschedule work triggered")
    }
}
