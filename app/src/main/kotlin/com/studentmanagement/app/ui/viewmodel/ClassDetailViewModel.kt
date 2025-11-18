package com.studentmanagement.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.data.repository.ClassRepository
import com.studentmanagement.app.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val studentRepository: StudentRepository,
    private val dailyRecordRepository: com.studentmanagement.app.data.repository.DailyRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassDetailUiState>(ClassDetailUiState.Loading)
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()
    
    private val _dailyRecords = MutableStateFlow<Map<Pair<Long, String>, Float?>>(emptyMap())
    val dailyRecords: StateFlow<Map<Pair<Long, String>, Float?>> = _dailyRecords.asStateFlow()

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
     * @param startDate The start date of the class
     * @param repeatInterval The number of units to repeat (e.g., 4 for "4 weeks")
     * @param repeatUnit The unit of repetition ("WEEK", "MONTH", or "YEAR")
     * @return The calculated end date
     */
    private fun getEndDate(startDate: LocalDate, repeatInterval: Int, repeatUnit: String): LocalDate {
        return when (repeatUnit.uppercase()) {
            "WEEK" -> startDate.plusWeeks(repeatInterval.toLong())
            "MONTH" -> startDate.plusMonths(repeatInterval.toLong())
            "YEAR" -> startDate.plusYears(repeatInterval.toLong())
            else -> {
                // Default to 1 week if invalid
                startDate.plusWeeks(1)
            }
        }
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
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
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
