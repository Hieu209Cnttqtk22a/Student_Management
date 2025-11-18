package com.studentmanagement.app.ui.viewmodel

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
        val dayOfWeek = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
        return allClasses.filter { classEntity ->
            val scheduledDays = parseScheduledDays(classEntity.scheduleDaysOfWeek)
            scheduledDays.contains(dayOfWeek)
        }
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
            List(jsonArray.length()) { i -> jsonArray.getInt(i) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
