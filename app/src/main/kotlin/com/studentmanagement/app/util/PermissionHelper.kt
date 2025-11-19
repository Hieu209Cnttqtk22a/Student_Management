package com.studentmanagement.app.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class for managing runtime permissions.
 * Requirements: 9.1, 9.2, 9.3
 */
object PermissionHelper {
    
    const val REQUEST_NOTIFICATION_PERMISSION = 1001
    
    /**
     * Check if notification permission is granted.
     * Requirement 9.1: Check for POST_NOTIFICATIONS permission (Android 13+)
     * 
     * @param context The application context
     * @return true if permission is granted, false otherwise
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // No permission needed for older versions
            true
        }
    }
    
    /**
     * Request notification permission.
     * Requirement 9.1: Request permission when enabling reminders
     * 
     * @param activity The activity to request permission from
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }
    
    /**
     * Check if the user has permanently denied the permission.
     * Requirement 9.2: Show explanation dialog if permission denied
     * 
     * @param activity The activity to check
     * @return true if permission should show rationale, false otherwise
     */
    fun shouldShowNotificationPermissionRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            false
        }
    }
    
    /**
     * Open app settings to allow user to grant permission manually.
     * Requirement 9.3: Provide link to app settings
     * 
     * @param context The application context
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    /**
     * Check if notification permission was granted from the result.
     * 
     * @param requestCode The request code
     * @param grantResults The grant results array
     * @return true if permission was granted, false otherwise
     */
    fun isNotificationPermissionGranted(
        requestCode: Int,
        grantResults: IntArray
    ): Boolean {
        return requestCode == REQUEST_NOTIFICATION_PERMISSION &&
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
    }
}
