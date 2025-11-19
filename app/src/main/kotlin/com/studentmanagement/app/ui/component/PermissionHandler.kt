package com.studentmanagement.app.ui.component

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.studentmanagement.app.util.PermissionHelper

/**
 * Composable for handling notification permission requests.
 * Requirements: 9.1, 9.2, 9.3
 * 
 * @param onPermissionResult Callback when permission result is received
 * @return A function to trigger the permission request
 */
@Composable
fun rememberNotificationPermissionState(
    onPermissionResult: (Boolean) -> Unit
): NotificationPermissionState {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(PermissionHelper.hasNotificationPermission(context)) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        onPermissionResult(isGranted)
        
        if (!isGranted) {
            // Show settings dialog if permission was denied
            showSettingsDialog = true
        }
    }
    
    // Check permission status on composition
    LaunchedEffect(Unit) {
        hasPermission = PermissionHelper.hasNotificationPermission(context)
    }
    
    return NotificationPermissionState(
        hasPermission = hasPermission,
        showRationaleDialog = showRationaleDialog,
        showSettingsDialog = showSettingsDialog,
        onRequestPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // No permission needed for older versions
                onPermissionResult(true)
            }
        },
        onShowRationale = { show ->
            showRationaleDialog = show
        },
        onShowSettings = { show ->
            showSettingsDialog = show
        },
        onOpenSettings = {
            PermissionHelper.openAppSettings(context)
            showSettingsDialog = false
        }
    )
}

/**
 * State holder for notification permission.
 */
data class NotificationPermissionState(
    val hasPermission: Boolean,
    val showRationaleDialog: Boolean,
    val showSettingsDialog: Boolean,
    val onRequestPermission: () -> Unit,
    val onShowRationale: (Boolean) -> Unit,
    val onShowSettings: (Boolean) -> Unit,
    val onOpenSettings: () -> Unit
)
