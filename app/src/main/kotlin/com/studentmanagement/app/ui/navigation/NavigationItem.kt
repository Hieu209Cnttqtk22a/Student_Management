package com.studentmanagement.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Home("home", "Lớp học", Icons.Default.Home),
    Calendar("calendar", "Lịch", Icons.Default.DateRange),
    Settings("settings", "Cài đặt", Icons.Default.Settings)
}
