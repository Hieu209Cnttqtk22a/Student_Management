package com.studentmanagement.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.studentmanagement.app.ui.screen.home.ClassListScreen
import com.studentmanagement.app.ui.screen.calendar.CalendarScreen
import com.studentmanagement.app.ui.screen.settings.SettingsScreen
import com.studentmanagement.app.ui.screen.`class`.ClassCreateScreen
import com.studentmanagement.app.ui.screen.`class`.ClassDetailScreen
import com.studentmanagement.app.ui.screen.`class`.ClassEditScreen
import com.studentmanagement.app.ui.screen.student.StudentCreateScreen
import com.studentmanagement.app.ui.screen.student.StudentDailyEditScreen
import com.studentmanagement.app.ui.screen.student.StudentDailyHistoryListScreen
import com.studentmanagement.app.ui.screen.student.StudentDailyDetailScreen
import com.studentmanagement.app.ui.screen.student.StudentImportScreen
import com.studentmanagement.app.ui.viewmodel.ClassListViewModel
import com.studentmanagement.app.data.entity.ClassEntity

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in listOf(
        NavigationItem.Home.route,
        NavigationItem.Calendar.route,
        NavigationItem.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationItem.values().forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    restoreState = true
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Bottom navigation screens
            composable(NavigationItem.Home.route) {
                ClassListScreen(navController)
            }
            composable(NavigationItem.Calendar.route) {
                CalendarScreen(navController)
            }
            composable(NavigationItem.Settings.route) {
                SettingsScreen(navController)
            }

            // Class screens
            composable("class/create") {
                val viewModel: ClassListViewModel = hiltViewModel()
                ClassCreateScreen(
                    navController = navController,
                    onSave = { name, scheduleDays, startTime, repeatInterval, repeatUnit ->
                        viewModel.createClass(
                            classEntity = ClassEntity(
                                name = name,
                                scheduleDaysOfWeek = scheduleDays,
                                startTimeMinutes = startTime,
                                repeatInterval = repeatInterval,
                                repeatUnit = repeatUnit
                            ),
                            onComplete = {
                                // Navigate back after class is created successfully
                                navController.popBackStack()
                            }
                        )
                    }
                )
            }
            composable(
                route = "class/{classId}/detail",
                arguments = listOf(navArgument("classId") { type = NavType.LongType })
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
                ClassDetailScreen(navController, classId)
            }
            composable(
                route = "class/{classId}/edit",
                arguments = listOf(navArgument("classId") { type = NavType.LongType })
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
                ClassEditScreen(
                    navController = navController,
                    classId = classId
                )
            }

            // Student screens
            composable(
                route = "class/{classId}/student/create",
                arguments = listOf(navArgument("classId") { type = NavType.LongType })
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
                StudentCreateScreen(
                    navController = navController,
                    classId = classId,
                    onSave = { name, nickname, phone, note ->
                        // TODO: Save to database
                    }
                )
            }
            composable(
                route = "class/{classId}/import",
                arguments = listOf(navArgument("classId") { type = NavType.LongType })
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
                StudentImportScreen(
                    navController = navController,
                    classId = classId
                )
            }
            composable(
                route = "student/{studentId}/daily/edit?classId={classId}&date={date}",
                arguments = listOf(
                    navArgument("studentId") { type = NavType.LongType },
                    navArgument("classId") { 
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                    navArgument("date") { 
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
                val classId = backStackEntry.arguments?.getLong("classId") ?: 0L
                val date = backStackEntry.arguments?.getString("date")
                StudentDailyEditScreen(
                    navController = navController,
                    studentId = studentId,
                    classId = classId,
                    date = date ?: ""
                )
            }
            composable(
                route = "student/{studentId}/history",
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
                StudentDailyHistoryListScreen(navController, studentId)
            }
            composable(
                route = "record/{recordId}/detail",
                arguments = listOf(navArgument("recordId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
                StudentDailyDetailScreen(navController, recordId)
            }
        }
    }
}
