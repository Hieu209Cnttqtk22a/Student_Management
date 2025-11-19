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
                        val dayOfWeek = date.dayOfWeek.value // 1=Mon, 7=Sun
                        // Convert to UI format
                        val dayOfWeekUI = when (dayOfWeek) {
                            1 -> 2; 2 -> 3; 3 -> 4; 4 -> 5; 5 -> 6; 6 -> 7; 7 -> 1
                            else -> dayOfWeek
                        }
                        val classes = uiState.allClasses.filter { classEntity ->
                            val scheduledDays = parseScheduledDays(classEntity.scheduleDaysOfWeek)
                            val isScheduledDay = scheduledDays.contains(dayOfWeekUI)
                            val isWithinDuration = isDateWithinClassDuration(date, classEntity)
                            isScheduledDay && isWithinDuration
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
                    // Calculate classes for each day in the month
                    val monthClassesMap = mutableMapOf<Int, List<com.studentmanagement.app.data.entity.ClassEntity>>()
                    val daysInMonth = uiState.currentYearMonth.lengthOfMonth()
                    
                    for (day in 1..daysInMonth) {
                        val date = LocalDate.of(uiState.currentYearMonth.year, uiState.currentYearMonth.month, day)
                        val dayOfWeek = date.dayOfWeek.value // 1=Mon, 7=Sun
                        // Convert to UI format
                        val dayOfWeekUI = when (dayOfWeek) {
                            1 -> 2; 2 -> 3; 3 -> 4; 4 -> 5; 5 -> 6; 6 -> 7; 7 -> 1
                            else -> dayOfWeek
                        }
                        
                        val classes = uiState.allClasses.filter { classEntity ->
                            val scheduledDays = parseScheduledDays(classEntity.scheduleDaysOfWeek)
                            val isScheduledDay = scheduledDays.contains(dayOfWeekUI)
                            val isWithinDuration = isDateWithinClassDuration(date, classEntity)
                            isScheduledDay && isWithinDuration
                        }
                        
                        if (classes.isNotEmpty()) {
                            monthClassesMap[day] = classes
                        }
                    }
                    
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

            // Upcoming class section
            val upcomingClass = getNextUpcomingClass(uiState.allClasses, uiState.currentDate)
            if (upcomingClass != null) {
                Text(
                    text = "Buổi học gần nhất",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ClassSessionCard(
                    classEntity = upcomingClass.first,
                    selectedDate = upcomingClass.second,
                    onClick = {
                        navController.navigate("class/${upcomingClass.first.id}/detail")
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }

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
                            selectedDate = uiState.selectedDate,
                            onClick = {
                                navController.navigate("class/${classEntity.id}/detail")
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
    selectedDate: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get the time for the selected date's class from scheduleDaysOfWeek
    val selectedDateTimeMinutes = getTimeForDate(classEntity, selectedDate)
    
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
                
                // Use time from selected date's schedule if available, otherwise use startTimeMinutes
                val startMinutes = selectedDateTimeMinutes ?: classEntity.startTimeMinutes
                startMinutes?.let { startMin ->
                    val hours = startMin / 60
                    val minutes = startMin % 60
                    val endMinutes = startMin + (classEntity.durationMinutes ?: 90)
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

/**
 * Get the start time for a specific date's class from the schedule
 */
private fun getTimeForDate(classEntity: com.studentmanagement.app.data.entity.ClassEntity, date: LocalDate): Int? {
    return try {
        val dayOfWeek = date.dayOfWeek.value // 1=Mon, 7=Sun
        // Convert to UI format (1=Sunday, 2=Monday, etc.)
        val dayOfWeekUI = when (dayOfWeek) {
            1 -> 2; 2 -> 3; 3 -> 4; 4 -> 5; 5 -> 6; 6 -> 7; 7 -> 1
            else -> dayOfWeek
        }
        
        val jsonArray = org.json.JSONArray(classEntity.scheduleDaysOfWeek)
        
        // Try to find the schedule entry for the specified date
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.get(i)
            val scheduleDay = when (item) {
                is org.json.JSONObject -> {
                    val day = item.getInt("day")
                    val startTime = if (item.has("startTime") && !item.isNull("startTime")) {
                        item.getInt("startTime")
                    } else null
                    Pair(day, startTime)
                }
                is Int -> Pair(item, null)
                else -> null
            }
            
            if (scheduleDay != null && scheduleDay.first == dayOfWeekUI) {
                return scheduleDay.second
            }
        }
        
        null
    } catch (e: Exception) {
        null
    }
}

private fun parseScheduledDays(scheduleDaysJson: String): List<Int> {
    if (scheduleDaysJson.isEmpty()) return emptyList()
    return try {
        val jsonArray = org.json.JSONArray(scheduleDaysJson)
        val result = mutableListOf<Int>()
        
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.get(i)
            val day = when (item) {
                is Int -> item // Old format: direct integer
                is org.json.JSONObject -> item.getInt("day") // New format: ScheduleDay object
                else -> null
            }
            
            if (day != null && day in 1..7) {
                result.add(day)
            }
        }
        
        result
    } catch (e: Exception) {
        emptyList()
    }
}


/**
 * Kiểm tra xem ngày có nằm trong khoảng thời gian hoạt động của class không
 */
private fun isDateWithinClassDuration(date: LocalDate, classEntity: com.studentmanagement.app.data.entity.ClassEntity): Boolean {
    // Ngày bắt đầu của class
    val startDate = java.time.Instant.ofEpochMilli(classEntity.createdAt)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
    
    // Nếu ngày kiểm tra trước ngày tạo class, return false
    if (date.isBefore(startDate)) {
        return false
    }
    
    // Tính ngày kết thúc dựa trên repeatInterval và repeatUnit
    val endDate = when (classEntity.repeatUnit) {
        "WEEK" -> startDate.plusWeeks(classEntity.repeatInterval.toLong())
        "MONTH" -> startDate.plusMonths(classEntity.repeatInterval.toLong())
        "YEAR" -> startDate.plusYears(classEntity.repeatInterval.toLong())
        else -> startDate.plusWeeks(classEntity.repeatInterval.toLong())
    }
    
    // Kiểm tra ngày có nằm trong khoảng [startDate, endDate]
    return !date.isAfter(endDate)
}

/**
 * Get the next upcoming class from today
 * Returns Pair of (ClassEntity, LocalDate) or null if no upcoming class
 */
private fun getNextUpcomingClass(
    allClasses: List<com.studentmanagement.app.data.entity.ClassEntity>,
    currentDate: LocalDate
): Pair<com.studentmanagement.app.data.entity.ClassEntity, LocalDate>? {
    var nextClass: Pair<com.studentmanagement.app.data.entity.ClassEntity, LocalDate>? = null
    var nextDate: LocalDate? = null
    
    // Search for the next class within the next 30 days
    for (daysAhead in 0..30) {
        val searchDate = currentDate.plusDays(daysAhead.toLong())
        val dayOfWeek = searchDate.dayOfWeek.value // 1=Mon, 7=Sun
        // Convert to UI format (1=Sunday, 2=Monday, etc.)
        val dayOfWeekUI = when (dayOfWeek) {
            1 -> 2; 2 -> 3; 3 -> 4; 4 -> 5; 5 -> 6; 6 -> 7; 7 -> 1
            else -> dayOfWeek
        }
        
        // Find classes scheduled for this day
        for (classEntity in allClasses) {
            val scheduledDays = parseScheduledDays(classEntity.scheduleDaysOfWeek)
            val isScheduledDay = scheduledDays.contains(dayOfWeekUI)
            val isWithinDuration = isDateWithinClassDuration(searchDate, classEntity)
            
            if (isScheduledDay && isWithinDuration) {
                // If this is today, only return if it's in the future (after current time)
                if (searchDate == currentDate) {
                    val timeMinutes = getTimeForDate(classEntity, searchDate)
                    if (timeMinutes != null) {
                        val now = java.time.LocalTime.now()
                        val nowMinutes = now.hour * 60 + now.minute
                        if (timeMinutes > nowMinutes) {
                            nextClass = Pair(classEntity, searchDate)
                            nextDate = searchDate
                            break
                        }
                    }
                } else {
                    // For future dates, return the first class found
                    nextClass = Pair(classEntity, searchDate)
                    nextDate = searchDate
                    break
                }
            }
        }
        
        if (nextClass != null) {
            break
        }
    }
    
    return nextClass
}
