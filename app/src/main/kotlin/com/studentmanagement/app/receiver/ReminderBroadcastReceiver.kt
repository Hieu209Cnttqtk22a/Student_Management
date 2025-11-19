package com.studentmanagement.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.studentmanagement.app.data.repository.ReminderRepository
import com.studentmanagement.app.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver for handling class reminder notifications.
 * Requirements: 8.1, 10.4
 */
@AndroidEntryPoint
class ReminderBroadcastReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var reminderRepository: ReminderRepository
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val TAG = "ReminderBroadcastReceiver"
        private const val ACTION_CLASS_REMINDER = "com.studentmanagement.app.CLASS_REMINDER"
    }
    
    /**
     * Handle reminder broadcast intent.
     * Requirement 8.1: Trigger notification display when scheduled reminder time arrives
     * Requirement 10.4: Mark reminder as delivered in database
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast with action: ${intent.action}")
        
        // Verify this is a class reminder intent
        if (intent.action != ACTION_CLASS_REMINDER) {
            Log.w(TAG, "Ignoring intent with unexpected action: ${intent.action}")
            return
        }
        
        // Extract class information from intent extras
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val classId = intent.getLongExtra("class_id", -1L)
        val className = intent.getStringExtra("class_name") ?: ""
        val startTimeMinutes = intent.getIntExtra("start_time_minutes", 0)
        
        // Validate extracted data
        if (reminderId == -1L || classId == -1L || className.isEmpty()) {
            Log.e(TAG, "Invalid reminder data: reminderId=$reminderId, classId=$classId, className=$className")
            return
        }
        
        Log.d(TAG, "Processing reminder: id=$reminderId, classId=$classId, className=$className, startTime=$startTimeMinutes")
        
        // Format the class time for display
        val classTime = formatTime(startTimeMinutes)
        
        // Trigger notification display (Requirement 8.1)
        notificationHelper.showClassReminder(
            context = context,
            classId = classId,
            className = className,
            classTime = classTime
        )
        
        Log.d(TAG, "Notification displayed for class: $className")
        
        // Mark reminder as delivered in database (Requirement 10.4)
        // Use goAsync() to allow asynchronous work in the receiver
        val pendingResult = goAsync()
        scope.launch {
            try {
                reminderRepository.markReminderAsDelivered(reminderId)
                Log.d(TAG, "Marked reminder $reminderId as delivered")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark reminder $reminderId as delivered", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    /**
     * Format time in minutes to HH:mm string.
     * 
     * @param minutes Time in minutes since midnight
     * @return Formatted time string (e.g., "09:30")
     */
    private fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
    }
}
