package com.studentmanagement.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.studentmanagement.app.MainActivity
import com.studentmanagement.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing notifications.
 * Requirements: 8.1, 8.2, 8.3, 8.4
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "NotificationHelper"
        private const val CHANNEL_ID = "class_reminders"
        private const val CHANNEL_NAME = "Class Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for upcoming class sessions"
    }
    
    init {
        // Create notification channel on initialization
        createNotificationChannel()
    }
    
    /**
     * Display a notification for a class reminder.
     * Requirement 8.1: Display notification when reminder time arrives
     * Requirement 8.2: Include class name and start time in notification
     * Requirement 8.3: Play system default notification sound
     * Requirement 8.4: Open class detail screen when notification is tapped
     * 
     * @param context The application context
     * @param classId The class ID
     * @param className The class name
     * @param classTime The formatted class start time
     */
    fun showClassReminder(
        context: Context,
        classId: Long,
        className: String,
        classTime: String
    ) {
        Log.d(TAG, "Showing class reminder notification for: $className at $classTime")
        
        // Create intent to open class detail when notification is tapped (Requirement 8.4)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "class_detail")
            putExtra("class_id", classId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            classId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification (Requirements 8.1, 8.2, 8.3)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Using launcher icon as notification icon
            .setContentTitle("Class Reminder: $className") // Requirement 8.2
            .setContentText("Class starts at $classTime") // Requirement 8.2
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Requirement 8.4
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Requirement 8.3
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        // Display the notification
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Check for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(classId.toInt(), notification)
                Log.d(TAG, "Notification displayed successfully for class: $className")
            } else {
                Log.w(TAG, "Notification permission not granted, cannot display notification")
            }
        } else {
            // No permission check needed for older Android versions
            notificationManager.notify(classId.toInt(), notification)
            Log.d(TAG, "Notification displayed successfully for class: $className")
        }
    }
    
    /**
     * Create the notification channel for class reminders.
     * Requirement 8.3: Set channel importance to HIGH for sound
     * 
     * This method is called during initialization to ensure the channel exists.
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Requirement 8.3: HIGH importance for sound
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }
}
