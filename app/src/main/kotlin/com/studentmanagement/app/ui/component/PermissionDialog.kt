package com.studentmanagement.app.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.studentmanagement.app.util.PermissionHelper

/**
 * Dialog for explaining notification permission requirement.
 * Requirement 9.2: Show explanation dialog if permission denied
 * Requirement 9.3: Provide link to app settings
 */
@Composable
fun NotificationPermissionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Notification Permission Required")
            },
            text = {
                Text(
                    text = "To receive class reminders, this app needs permission to send notifications. " +
                            "Please grant the notification permission in app settings."
                )
            },
            confirmButton = {
                TextButton(onClick = onOpenSettings) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Dialog for explaining why notification permission is needed before requesting.
 * Requirement 9.2: Show explanation dialog if permission denied
 */
@Composable
fun NotificationPermissionRationaleDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Enable Class Reminders")
            },
            text = {
                Text(
                    text = "Class reminders help you stay on schedule by notifying you before your classes start. " +
                            "To enable this feature, please grant notification permission."
                )
            },
            confirmButton = {
                TextButton(onClick = onRequestPermission) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Not Now")
                }
            }
        )
    }
}
