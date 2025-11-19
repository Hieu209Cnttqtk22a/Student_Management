package com.studentmanagement.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.studentmanagement.app.worker.ReminderRescheduleWorker

/**
 * Receiver to handle device boot and app updates.
 * Requirement 12.3: Reschedule reminders after app update and device reboot
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Device boot completed, scheduling reminder reschedule")
                scheduleReminderReschedule(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "App updated, scheduling reminder reschedule")
                scheduleReminderReschedule(context)
            }
        }
    }
    
    /**
     * Schedule a WorkManager task to reschedule all reminders.
     * Requirement 12.3: Use WorkManager for reliability across reboots
     */
    private fun scheduleReminderReschedule(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<ReminderRescheduleWorker>()
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            ReminderRescheduleWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        Log.d(TAG, "Reminder reschedule work enqueued")
    }
}
