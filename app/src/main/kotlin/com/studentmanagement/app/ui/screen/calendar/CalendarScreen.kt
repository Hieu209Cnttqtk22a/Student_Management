package com.studentmanagement.app.ui.screen.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.studentmanagement.app.ui.component.EmptyState
import com.studentmanagement.app.ui.theme.Primary
import com.studentmanagement.app.ui.theme.Secondary
import com.studentmanagement.app.ui.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val headerText = when (uiState.selectedPeriod) {
        CalendarPeriod.WEEK -> {
            val startOfWeek = uiState.selectedDate.with(java.time.DayOfWeek.MONDAY)
            val endOfWeek = startOfWeek.plusDays(6)
            "${startOfWeek.dayOfMonth} - ${endOfWeek.dayOfMonth} ${uiState.currentYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("vi"))}, ${uiState.currentYearMonth.year}"
        }
        CalendarPeriod.MONTH -> {
            "${uiState.currentYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("vi")).replaceFirstChar { it.uppercase() }} ${uiState.currentYearMonth.year}"
        }
        CalendarPeriod.YEAR -> {
            "Năm ${uiState.currentYearMonth.year}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch học",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Period selector
            CalendarPeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodChange = { viewModel.setPeriod(it) }
            )

            // Calendar header with navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateToPreviousPeriod() }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Kỳ trước",
                        tint = Primary
                    )
                }
                Text(
                    text = headerText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { viewModel.navigateToToday() }
                )
                IconButton(onClick = { viewModel.navigateToNextPeriod() }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Kỳ sau",
                        tint = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar view based on selected period
            when (uiState.selectedPeriod) {
                CalendarPeriod.WEEK -> {
                    val weekClassesMap = mutableMapOf<LocalDate, List<com.studentmanagement.app.data.entity.ClassEntity>>()
                    val startOfWeek = uiState.selectedDate.with(java.time.DayOfWeek.MONDAY)
                    for (i in 0..6) {
                        val date = startOfWeek.plusDays(i.toLong())
                        val classes = uiState.allClasses.filter { classEntity ->
                            val scheduledDays = parseScheduledDays(classEntity.scheduleDaysOfWeek)
                            scheduledDays.contains(date.dayOfWeek.value)
                        }
                        if (classes.isNotEmpty()) {
                            weekClassesMap[date] = classes
                        }
                    }
                    
                    WeekCalendarView(
                        selectedDate = uiState.selectedDate,
                        currentDate = uiState.currentDate,
                        classesMap = weekClassesMap,
                        onDateSelected = { viewModel.selectDate(it) }
                    )
                }
                CalendarPeriod.MONTH -> {
                    val monthClassesMap = viewModel.getClassesForMonth(uiState.currentYearMonth)
                    MonthCalendarView(
                        yearMonth = uiState.currentYearMonth,
                        selectedDate = uiState.selectedDate,
                        currentDate = uiState.currentDate,
                        classesMap = monthClassesMap,
                        onDateSelected = { viewModel.selectDate(it) }
                    )
                }
                CalendarPeriod.YEAR -> {
                    val yearClassesMap = viewModel.getClassesForYear(uiState.currentYearMonth.year)
                    YearCalendarView(
                        year = uiState.currentYearMonth.year,
                        currentYearMonth = uiState.currentYearMonth,
                        classCountByMonth = yearClassesMap,
                        onMonthSelected = { yearMonth ->
                            viewModel.setPeriod(CalendarPeriod.MONTH)
                            viewModel.selectDate(LocalDate.of(yearMonth.year, yearMonth.month, 1))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Classes for selected date
            Text(
                text = if (uiState.selectedDate == uiState.currentDate) {
                    "Buổi học hôm nay"
                } else {
                    "Buổi học ngày ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.classesForSelectedDate.isEmpty()) {
                EmptyState(
                    title = "Không có buổi học nào",
                    description = "Chưa có lớp học nào được lên lịch cho ngày này",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.classesForSelectedDate) { classEntity ->
                        ClassSessionCard(
                            classEntity = classEntity,
                            onClick = {
                                navController.navigate("class_detail/${classEntity.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassSessionCard(
    classEntity: com.studentmanagement.app.data.entity.ClassEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = classEntity.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                classEntity.startTimeMinutes?.let { startMinutes ->
                    val hours = startMinutes / 60
                    val minutes = startMinutes % 60
                    val endMinutes = startMinutes + (classEntity.durationMinutes ?: 90)
                    val endHours = endMinutes / 60
                    val endMins = endMinutes % 60
                    
                    Text(
                        text = String.format("%02d:%02d - %02d:%02d", hours, minutes, endHours, endMins),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .background(
                        color = Secondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Xem chi tiết",
                    fontSize = 10.sp,
                    color = Secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun parseScheduledDays(scheduleDaysJson: String): List<Int> {
    if (scheduleDaysJson.isEmpty()) return emptyList()
    return try {
        val jsonArray = org.json.JSONArray(scheduleDaysJson)
        List(jsonArray.length()) { i -> jsonArray.getInt(i) }
    } catch (e: Exception) {
        emptyList()
    }
}
