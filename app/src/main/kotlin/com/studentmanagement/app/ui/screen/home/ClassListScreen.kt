package com.studentmanagement.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.ui.component.ClassCard
import com.studentmanagement.app.ui.theme.Primary
import com.studentmanagement.app.ui.theme.PrimaryLight
import com.studentmanagement.app.ui.viewmodel.ClassListViewModel
import com.studentmanagement.app.ui.viewmodel.ClassListUiState
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ClassListScreen(
    navController: NavController,
    viewModel: ClassListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Reload classes when screen is displayed
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadClasses()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lớp học của tôi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadClasses() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("class/create") },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tạo lớp học",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Header section with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Primary, PrimaryLight)
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Quản lý lớp học",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    val classCount = when (val state = uiState) {
                        is ClassListUiState.Success -> state.classes.size
                        else -> 0
                    }
                    Text(
                        "$classCount lớp đang hoạt động",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Filter and sort row - 2 nút to ở giữa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            color = Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sắp xếp",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            color = Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Lọc",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Class list
            when (val state = uiState) {
                is ClassListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ClassListUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Chưa có lớp học nào",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Nhấn nút + để tạo lớp học mới",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                is ClassListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
                    ) {
                        items(state.classes) { classEntity ->
                            ClassCard(
                                className = classEntity.name,
                                subject = classEntity.subject ?: "",
                                studentCount = state.studentCounts[classEntity.id] ?: 0,
                                nextSessionDate = calculateNextSessionDate(classEntity),
                                reminderEnabled = classEntity.reminderEnabled,
                                onEditClick = { navController.navigate("class/${classEntity.id}/edit") },
                                onDetailClick = { navController.navigate("class/${classEntity.id}/detail") }
                            )
                        }
                    }
                }
                is ClassListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Lỗi: ${state.message}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tính toán ngày học tiếp theo dựa trên lịch của class
 */
private fun calculateNextSessionDate(classEntity: ClassEntity): String {
    try {
        // Parse scheduleDaysOfWeek từ JSON (hỗ trợ cả format cũ và mới)
        val scheduledDays = parseScheduledDays(classEntity.scheduleDaysOfWeek)
        if (scheduledDays.isEmpty()) return "Chưa có lịch"
        
        // Tính toán tất cả các ngày học dựa trên lịch
        val scheduledDates = calculateScheduledDates(classEntity, scheduledDays)
        if (scheduledDates.isEmpty()) return "Chưa có lịch"
        
        // Tìm ngày học tiếp theo (>= hôm nay)
        val today = LocalDate.now()
        val nextDate = scheduledDates.firstOrNull { it >= today }
        
        return when {
            nextDate == null -> "Đã kết thúc"
            nextDate == today -> "Hôm nay"
            nextDate == today.plusDays(1) -> "Ngày mai"
            nextDate.isBefore(today.plusDays(7)) -> {
                // Trong tuần này
                val dayNames = mapOf(
                    1 to "Thứ 2",    // Monday
                    2 to "Thứ 3",    // Tuesday
                    3 to "Thứ 4",    // Wednesday
                    4 to "Thứ 5",    // Thursday
                    5 to "Thứ 6",    // Friday
                    6 to "Thứ 7",    // Saturday
                    7 to "Chủ nhật"  // Sunday
                )
                dayNames[nextDate.dayOfWeek.value] ?: nextDate.format(DateTimeFormatter.ofPattern("dd/MM"))
            }
            else -> nextDate.format(DateTimeFormatter.ofPattern("dd/MM"))
        }
    } catch (e: Exception) {
        return "Chưa có lịch"
    }
}

/**
 * Parse scheduleDaysOfWeek JSON string
 * Supports both old format (array of integers) and new format (array of ScheduleDay objects)
 */
private fun parseScheduledDays(scheduleDaysJson: String): List<Int> {
    return try {
        if (scheduleDaysJson.isEmpty()) return emptyList()
        
        val jsonArray = org.json.JSONArray(scheduleDaysJson)
        val days = mutableListOf<Int>()
        
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.get(i)
            val day = when (item) {
                is Int -> item // Old format: direct integer
                is org.json.JSONObject -> item.getInt("day") // New format: ScheduleDay object
                else -> null
            }
            
            if (day != null && day in 1..7) {
                days.add(day)
            }
        }
        
        days
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Calculate all scheduled dates for a class
 */
private fun calculateScheduledDates(classEntity: ClassEntity, scheduledDays: List<Int>): List<LocalDate> {
    try {
        // Convert createdAt timestamp to LocalDate as start date
        val startDate = java.time.Instant.ofEpochMilli(classEntity.createdAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        
        // Calculate end date based on repeatInterval and repeatUnit
        val endDate = when (classEntity.repeatUnit.uppercase()) {
            "WEEK" -> startDate.plusWeeks(classEntity.repeatInterval.toLong())
            "MONTH" -> startDate.plusMonths(classEntity.repeatInterval.toLong())
            "YEAR" -> startDate.plusYears(classEntity.repeatInterval.toLong())
            else -> startDate.plusWeeks(1)
        }
        
        // Generate list of dates matching scheduled weekdays
        val scheduledDates = mutableListOf<LocalDate>()
        var currentDate = startDate
        
        while (!currentDate.isAfter(endDate)) {
            // Convert LocalDate day of week to UI format
            val localDateDayOfWeek = currentDate.dayOfWeek.value // 1=Monday, 7=Sunday
            val uiDayOfWeek = if (localDateDayOfWeek == 7) 1 else localDateDayOfWeek + 1 // Convert to UI format (1=Sunday)
            
            if (scheduledDays.contains(uiDayOfWeek)) {
                scheduledDates.add(currentDate)
            }
            
            currentDate = currentDate.plusDays(1)
        }
        
        return scheduledDates.sorted()
    } catch (e: Exception) {
        return emptyList()
    }
}
