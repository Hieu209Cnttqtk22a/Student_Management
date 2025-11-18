package com.studentmanagement.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.datastore.SettingsDataStore
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.repository.ClassRepository
import com.studentmanagement.app.ui.screen.calendar.CalendarPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarUiState(
    val selectedPeriod: CalendarPeriod = CalendarPeriod.MONTH,
    val currentDate: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val currentYearMonth: YearMonth = YearMonth.now(),
    val classesForSelectedDate: List<ClassEntity> = emptyList(),
    val allClasses: List<ClassEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = combine(
        _uiState,
        settingsDataStore.calendarPeriodFlow,
        classRepository.getAllClasses()
    ) { state, period, classes ->
        state.copy(
            selectedPeriod = period,
            allClasses = classes,
            classesForSelectedDate = getClassesForDate(state.selectedDate, classes)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    init {
        // Initialize with current date
        selectDate(LocalDate.now())
    }

    fun setPeriod(period: CalendarPeriod) {
        viewModelScope.launch {
            settingsDataStore.setCalendarPeriod(period)
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            currentYearMonth = YearMonth.from(date),
            classesForSelectedDate = getClassesForDate(date, _uiState.value.allClasses)
        )
    }

    fun navigateToNextPeriod() {
        val currentState = _uiState.value
        when (currentState.selectedPeriod) {
            CalendarPeriod.WEEK -> {
                val nextWeek = currentState.selectedDate.plusWeeks(1)
                selectDate(nextWeek)
            }
            CalendarPeriod.MONTH -> {
                val nextMonth = currentState.currentYearMonth.plusMonths(1)
                _uiState.value = currentState.copy(
                    currentYearMonth = nextMonth,
                    selectedDate = LocalDate.of(nextMonth.year, nextMonth.month, 1)
                )
            }
            CalendarPeriod.YEAR -> {
                val nextYear = currentState.currentYearMonth.plusYears(1)
                _uiState.value = currentState.copy(
                    currentYearMonth = nextYear,
                    selectedDate = LocalDate.of(nextYear.year, 1, 1)
                )
            }
        }
    }

    fun navigateToPreviousPeriod() {
        val currentState = _uiState.value
        when (currentState.selectedPeriod) {
            CalendarPeriod.WEEK -> {
                val previousWeek = currentState.selectedDate.minusWeeks(1)
                selectDate(previousWeek)
            }
            CalendarPeriod.MONTH -> {
                val previousMonth = currentState.currentYearMonth.minusMonths(1)
                _uiState.value = currentState.copy(
                    currentYearMonth = previousMonth,
                    selectedDate = LocalDate.of(previousMonth.year, previousMonth.month, 1)
                )
            }
            CalendarPeriod.YEAR -> {
                val previousYear = currentState.currentYearMonth.minusYears(1)
                _uiState.value = currentState.copy(
                    currentYearMonth = previousYear,
                    selectedDate = LocalDate.of(previousYear.year, 1, 1)
                )
            }
        }
    }

    fun navigateToToday() {
        val today = LocalDate.now()
        _uiState.value = _uiState.value.copy(
            selectedDate = today,
            currentYearMonth = YearMonth.from(today),
            classesForSelectedDate = getClassesForDate(today, _uiState.value.allClasses)
        )
    }

    private fun getClassesForDate(date: LocalDate, allClasses: List<ClassEntity>): List<ClassEntity> {
        Log.d("CalendarViewModel", "getClassesForDate: date=$date, allClasses.size=${allClasses.size}")
        
        val dayOfWeek = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
        // Convert LocalDate format to UI format for comparison
        val dayOfWeekUI = when (dayOfWeek) {
            1 -> 2  // Monday -> Thứ 2
            2 -> 3  // Tuesday -> Thứ 3
            3 -> 4  // Wednesday -> Thứ 4
            4 -> 5  // Thursday -> Thứ 5
            5 -> 6  // Friday -> Thứ 6
            6 -> 7  // Saturday -> Thứ 7
            7 -> 1  // Sunday -> Chủ nhật
            else -> dayOfWeek
        }
        
        Log.d("CalendarViewModel", "dayOfWeekUI=$dayOfWeekUI")
        
        val filtered = allClasses.filter { classEntity ->
            val scheduledDays = parseScheduledDays(classEntity.scheduleDaysOfWeek)
            val isScheduledDay = scheduledDays.contains(dayOfWeekUI)
            
            // Check if date is within the class duration
            val isWithinDuration = isDateWithinClassDuration(date, classEntity)
            
            Log.d("CalendarViewModel", "Class: ${classEntity.name}, scheduledDays=$scheduledDays, isScheduledDay=$isScheduledDay, isWithinDuration=$isWithinDuration")
            
            isScheduledDay && isWithinDuration
        }
        
        Log.d("CalendarViewModel", "Filtered classes: ${filtered.size}")
        return filtered
    }
    
    /**
     * Kiểm tra xem ngày có nằm trong khoảng thời gian hoạt động của class không
     */
    private fun isDateWithinClassDuration(date: LocalDate, classEntity: ClassEntity): Boolean {
        // Ngày bắt đầu của class
        val startDate = java.time.Instant.ofEpochMilli(classEntity.createdAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        
        Log.d("CalendarViewModel", "isDateWithinClassDuration: date=$date, startDate=$startDate, repeatInterval=${classEntity.repeatInterval}, repeatUnit=${classEntity.repeatUnit}")
        
        // Nếu ngày kiểm tra trước ngày tạo class, return false
        if (date.isBefore(startDate)) {
            Log.d("CalendarViewModel", "Date is before startDate")
            return false
        }
        
        // Tính ngày kết thúc dựa trên repeatInterval và repeatUnit
        val endDate = when (classEntity.repeatUnit) {
            "WEEK" -> startDate.plusWeeks(classEntity.repeatInterval.toLong())
            "MONTH" -> startDate.plusMonths(classEntity.repeatInterval.toLong())
            "YEAR" -> startDate.plusYears(classEntity.repeatInterval.toLong())
            else -> startDate.plusWeeks(classEntity.repeatInterval.toLong())
        }
        
        Log.d("CalendarViewModel", "endDate=$endDate")
        
        // Kiểm tra ngày có nằm trong khoảng [startDate, endDate]
        val result = !date.isAfter(endDate)
        Log.d("CalendarViewModel", "isWithinDuration=$result")
        return result
    }

    fun getClassesForMonth(yearMonth: YearMonth): Map<Int, List<ClassEntity>> {
        val allClasses = _uiState.value.allClasses
        val daysInMonth = yearMonth.lengthOfMonth()
        val result = mutableMapOf<Int, List<ClassEntity>>()
        
        for (day in 1..daysInMonth) {
            val date = LocalDate.of(yearMonth.year, yearMonth.month, day)
            val classes = getClassesForDate(date, allClasses)
            if (classes.isNotEmpty()) {
                result[day] = classes
            }
        }
        
        return result
    }

    fun getClassesForYear(year: Int): Map<Int, Int> {
        val allClasses = _uiState.value.allClasses
        val result = mutableMapOf<Int, Int>()
        
        for (month in 1..12) {
            val yearMonth = YearMonth.of(year, month)
            val classesInMonth = getClassesForMonth(yearMonth)
            result[month] = classesInMonth.values.flatten().distinct().size
        }
        
        return result
    }

    private fun parseScheduledDays(scheduleDaysJson: String): List<Int> {
        if (scheduleDaysJson.isEmpty()) return emptyList()
        return try {
            val jsonArray = JSONArray(scheduleDaysJson)
            
            // Try to parse as new format first (array of ScheduleDay objects)
            if (jsonArray.length() > 0 && jsonArray.getJSONObject(0).has("day")) {
                List(jsonArray.length()) { i -> 
                    jsonArray.getJSONObject(i).getInt("day")
                }
            } else {
                // Fallback to old format (array of integers)
                List(jsonArray.length()) { i -> jsonArray.getInt(i) }
            }
        } catch (e: Exception) {
            Log.e("CalendarViewModel", "Error parsing scheduled days: $scheduleDaysJson", e)
            emptyList()
        }
    }
}
