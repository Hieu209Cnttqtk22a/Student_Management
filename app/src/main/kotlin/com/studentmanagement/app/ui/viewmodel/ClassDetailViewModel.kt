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
import kotlinx.coroutines.launch
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
    
    private val _dailyRecords = MutableStateFlow<Map<Long, Float?>>(emptyMap())
    val dailyRecords: StateFlow<Map<Long, Float?>> = _dailyRecords.asStateFlow()

    fun loadClassDetail(classId: Long, date: String = "") {
        viewModelScope.launch {
            try {
                val classEntity = classRepository.getClassById(classId)
                if (classEntity != null) {
                    studentRepository.getStudentsByClass(classId).collect { students ->
                        _uiState.value = ClassDetailUiState.Success(classEntity, students)
                        
                        // Load daily records for the selected date
                        if (date.isNotEmpty()) {
                            loadDailyRecords(classId, date)
                        }
                    }
                } else {
                    _uiState.value = ClassDetailUiState.Error("Class not found")
                }
            } catch (e: Exception) {
                _uiState.value = ClassDetailUiState.Error(e.message ?: "Unknown error")
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
                        val recordsMap = records.associate { it.studentId to it.score }
                        _dailyRecords.value = recordsMap
                    }
                }
            } catch (e: Exception) {
                // Silently fail, just don't show scores
            }
        }
    }
    
    fun refreshDailyRecords(classId: Long, date: String) {
        loadDailyRecords(classId, date)
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
        val students: List<StudentEntity>
    ) : ClassDetailUiState()
    data class Error(val message: String) : ClassDetailUiState()
}
