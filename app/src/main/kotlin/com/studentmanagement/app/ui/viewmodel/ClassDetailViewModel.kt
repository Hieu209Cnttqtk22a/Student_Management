package com.studentmanagement.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.data.repository.ClassRepository
import com.studentmanagement.app.data.repository.StudentRepository
import com.studentmanagement.app.ui.model.FilterState
import com.studentmanagement.app.ui.model.FilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val dailyRecordRepository: com.studentmanagement.app.data.repository.DailyRecordRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    companion object {
        private const val KEY_FILTER_TYPE = "filter_type"
        private const val KEY_FILTER_DATE = "filter_date"
    }

    private val _uiState = MutableStateFlow<ClassDetailUiState>(ClassDetailUiState.Loading)
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()
    
    private val _dailyRecords = MutableStateFlow<Map<Pair<Long, String>, Float?>>(emptyMap())
    val dailyRecords: StateFlow<Map<Pair<Long, String>, Float?>> = _dailyRecords.asStateFlow()
    
    // Restore filter state from SavedStateHandle
    private val _filterState = MutableStateFlow(restoreFilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    
    private val _filteredStudents = MutableStateFlow<List<StudentEntity>>(emptyList())
    val filteredStudents: StateFlow<List<StudentEntity>> = _filteredStudents.asStateFlow()
    
    /**
     * Restore filter state from SavedStateHandle.
     * @return The restored FilterState or default FilterState if not found
     */
    private fun restoreFilterState(): FilterState {
        val filterTypeName = savedStateHandle.get<String>(KEY_FILTER_TYPE)
        val filterDate = savedStateHandle.get<String>(KEY_FILTER_DATE)
        
        val filterType = filterTypeName?.let {
            try {
                FilterType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                FilterType.ALL
            }
        } ?: FilterType.ALL
        
        return FilterState(type = filterType, selectedDate = filterDate)
    }
    
    /**
     * Save filter state to SavedStateHandle.
     * @param filterState The filter state to save
     */
    private fun saveFilterState(filterState: FilterState) {
        savedStateHandle[KEY_FILTER_TYPE] = filterState.type.name
        savedStateHandle[KEY_FILTER_DATE] = filterState.selectedDate
    }

    /**
     * Calculate all scheduled dates for a class based on its schedule configuration.
     * @param classEntity The class entity containing schedule information
     * @return List of LocalDate objects representing scheduled class dates
     */
    private fun calculateScheduledDates(classEntity: ClassEntity): List<LocalDate> {
        try {
            // Parse scheduled days from JSON
            val scheduledDays = parseScheduledDays(classEntity.scheduleDaysOfWeek)
            if (scheduledDays.isEmpty()) {
                return emptyList()
            }

            // Convert createdAt timestamp to LocalDate as start date
            val startDate = Instant.ofEpochMilli(classEntity.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            // Calculate end date based on repeatInterval and repeatUnit
            val endDate = getEndDate(startDate, classEntity.repeatInterval, classEntity.repeatUnit)

            // Generate list of dates matching scheduled weekdays
            val scheduledDates = mutableListOf<LocalDate>()
            var currentDate = startDate

            while (!currentDate.isAfter(endDate)) {
                // Convert LocalDate day of week to UI format and check if it matches
                val localDateDayOfWeek = currentDate.dayOfWeek.value // 1=Monday, 7=Sunday
                val uiDayOfWeek = if (localDateDayOfWeek == 7) 1 else localDateDayOfWeek + 1 // Convert to UI format (1=Sunday)

                if (scheduledDays.contains(uiDayOfWeek)) {
                    // Only add dates that are on or after the class creation date
                    // and on or before the end date (already ensured by while condition)
                    if (!currentDate.isBefore(startDate)) {
                        scheduledDates.add(currentDate)
                    }
                }

                currentDate = currentDate.plusDays(1)
            }

            return scheduledDates.sorted()
        } catch (e: Exception) {
            // Return empty list on error
            return emptyList()
        }
    }

    /**
     * Parse scheduleDaysOfWeek JSON string to extract list of weekday numbers.
     * Supports both old format (array of integers) and new format (array of ScheduleDay objects).
     * @param scheduleDaysJson JSON string containing array of weekday numbers (1-7) or ScheduleDay objects
     * @return List of integers representing scheduled weekdays in UI format (1=Sunday, 2=Monday, etc.)
     */
    private fun parseScheduledDays(scheduleDaysJson: String): List<Int> {
        return try {
            if (scheduleDaysJson.isEmpty()) {
                return emptyList()
            }

            val jsonArray = JSONArray(scheduleDaysJson)
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
     * Calculate the end date of a class based on start date, repeat interval, and repeat unit.
     * Changed to always extend 1 year from today to preserve all historical records
     * and allow continuous score entry.
     * @param startDate The start date of the class
     * @param repeatInterval The number of units to repeat (e.g., 4 for "4 weeks")
     * @param repeatUnit The unit of repetition ("WEEK", "MONTH", or "YEAR")
     * @return The calculated end date (always 1 year from today)
     */
    private fun getEndDate(startDate: LocalDate, repeatInterval: Int, repeatUnit: String): LocalDate {
        // Always extend to 1 year from today to preserve historical records
        // and allow continuous score entry without losing old data
        return LocalDate.now().plusYears(1)
    }

    fun loadClassDetail(classId: Long, date: String = "") {
        viewModelScope.launch {
            try {
                val classEntity = classRepository.getClassById(classId)
                if (classEntity != null) {
                    // Calculate scheduled dates for the class
                    val scheduledDates = calculateScheduledDates(classEntity)
                    
                    studentRepository.getStudentsByClass(classId).collect { students ->
                        _uiState.value = ClassDetailUiState.Success(
                            classEntity = classEntity,
                            students = students,
                            scheduledDates = scheduledDates
                        )
                        
                        // Load daily records for all scheduled dates
                        loadDailyRecordsForDates(classId, scheduledDates)
                        
                        // Apply filter after loading students
                        applyFilter()
                    }
                } else {
                    _uiState.value = ClassDetailUiState.Error("Class not found")
                }
            } catch (e: Exception) {
                _uiState.value = ClassDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Load daily records for all scheduled dates.
     * @param classId The class ID
     * @param dates List of scheduled dates
     */
    private fun loadDailyRecordsForDates(classId: Long, dates: List<LocalDate>) {
        viewModelScope.launch {
            try {
                val recordsMap = mutableMapOf<Pair<Long, String>, Float?>()
                
                // Load records for each scheduled date sequentially
                for (date in dates) {
                    // Format date as yyyy-MM-dd for database query
                    val formattedDateForDb = date.toString() // LocalDate.toString() returns yyyy-MM-dd
                    
                    // Format date as dd/MM/yyyy for map key (UI format)
                    val dateString = String.format("%02d/%02d/%04d", 
                        date.dayOfMonth, 
                        date.monthValue, 
                        date.year)
                    
                    // Load records for this date (use first() to get snapshot, not collect)
                    try {
                        val records = dailyRecordRepository.getRecordsByClassAndDate(classId, formattedDateForDb).first()
                        
                        // Build map with (studentId, dateString) as key
                        records.forEach { record ->
                            val key = Pair(record.studentId, dateString)
                            // Only set if not already set (prevent duplicates for same student+date)
                            if (!recordsMap.containsKey(key)) {
                                recordsMap[key] = record.score
                            }
                        }
                    } catch (e: Exception) {
                        // Skip this date if error
                    }
                }
                
                // Update the state once with all records
                _dailyRecords.value = recordsMap.toMap()
                
                // Apply filter after loading records
                applyFilter()
            } catch (e: Exception) {
                // Silently fail, just don't show scores
            }
        }
    }
    
    private fun loadDailyRecords(classId: Long, date: String) {
        viewModelScope.launch {
            try {
                // Convert date from dd/MM/yyyy to yyyy-MM-dd
                val parts = date.split("/")
                if (parts.size == 3) {
                    val formattedDate = "${parts[2]}-${parts[1]}-${parts[0]}"
                    
                    dailyRecordRepository.getRecordsByClassAndDate(classId, formattedDate).collect { records ->
                        val recordsMap = records.associate { Pair(it.studentId, date) to it.score }
                        _dailyRecords.value = recordsMap
                    }
                }
            } catch (e: Exception) {
                // Silently fail, just don't show scores
            }
        }
    }
    
    fun refreshDailyRecords(classId: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ClassDetailUiState.Success) {
                loadDailyRecordsForDates(classId, currentState.scheduledDates)
                // Reapply current filter after refreshing records
                applyFilter()
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }
    
    /**
     * Set the filter state and apply filtering to the student list.
     * @param type The filter type to apply
     * @param date The selected date for date-specific filters (format: dd/MM/yyyy)
     */
    fun setFilter(type: FilterType, date: String? = null) {
        val newFilterState = FilterState(type = type, selectedDate = date)
        _filterState.value = newFilterState
        saveFilterState(newFilterState)
        applyFilter()
    }
    
    /**
     * Reset the filter to show all students.
     */
    fun resetFilter() {
        val defaultFilterState = FilterState()
        _filterState.value = defaultFilterState
        saveFilterState(defaultFilterState)
        applyFilter()
    }
    
    /**
     * Apply the current filter to the student list.
     */
    private fun applyFilter() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentState = _uiState.value
            if (currentState is ClassDetailUiState.Success) {
                val filtered = filterStudents(
                    students = currentState.students,
                    records = _dailyRecords.value,
                    filterState = _filterState.value
                )
                withContext(Dispatchers.Main) {
                    _filteredStudents.value = filtered
                }
            }
        }
    }
    
    /**
     * Filter students based on the current filter state.
     * @param students The list of all students
     * @param records Map of daily records with (studentId, date) as key
     * @param filterState The current filter state
     * @return Filtered list of students
     */
    private fun filterStudents(
        students: List<StudentEntity>,
        records: Map<Pair<Long, String>, Float?>,
        filterState: FilterState
    ): List<StudentEntity> {
        // If no filter is active, return all students
        if (filterState.type == FilterType.ALL) {
            return students
        }
        
        // For date-specific filters, we need a selected date
        val selectedDate = filterState.selectedDate ?: return students
        
        return when (filterState.type) {
            FilterType.ALL -> students
            
            FilterType.LOW_SCORE -> {
                // Show only students with scores < 7.0 on the selected date
                students.filter { student ->
                    val score = records[Pair(student.id, selectedDate)]
                    score != null && score < 7.0f
                }
            }
            
            FilterType.NO_SCORE -> {
                // Show only students with no score (null) on the selected date
                students.filter { student ->
                    val score = records[Pair(student.id, selectedDate)]
                    score == null
                }
            }
            
            FilterType.PERFECT_SCORE -> {
                // Show only students with score == 10.0 on the selected date
                students.filter { student ->
                    val score = records[Pair(student.id, selectedDate)]
                    score != null && score == 10.0f
                }
            }
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            try {
                studentRepository.deleteStudent(student)
            } catch (e: Exception) {
                _uiState.value = ClassDetailUiState.Error(e.message ?: "Failed to delete student")
            }
        }
    }

    fun createStudent(classId: Long, name: String) {
        viewModelScope.launch {
            try {
                val student = StudentEntity(
                    name = name,
                    classId = classId,
                    nickname = null,
                    parentPhone = null,
                    note = null
                )
                studentRepository.createStudent(student)
            } catch (e: Exception) {
                _uiState.value = ClassDetailUiState.Error(e.message ?: "Failed to create student")
            }
        }
    }

    fun updateStudentName(studentId: Long, newName: String) {
        viewModelScope.launch {
            try {
                val student = studentRepository.getStudentById(studentId)
                if (student != null) {
                    val updatedStudent = student.copy(name = newName)
                    studentRepository.updateStudent(updatedStudent)
                }
            } catch (e: Exception) {
                _uiState.value = ClassDetailUiState.Error(e.message ?: "Failed to update student")
            }
        }
    }
}

sealed class ClassDetailUiState {
    object Loading : ClassDetailUiState()
    data class Success(
        val classEntity: ClassEntity,
        val students: List<StudentEntity>,
        val scheduledDates: List<LocalDate> = emptyList()
    ) : ClassDetailUiState()
    data class Error(val message: String) : ClassDetailUiState()
}
