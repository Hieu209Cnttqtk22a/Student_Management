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
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassDetailUiState>(ClassDetailUiState.Loading)
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun loadClassDetail(classId: Long) {
        viewModelScope.launch {
            try {
                val classEntity = classRepository.getClassById(classId)
                if (classEntity != null) {
                    studentRepository.getStudentsByClass(classId).collect { students ->
                        _uiState.value = ClassDetailUiState.Success(classEntity, students)
                    }
                } else {
                    _uiState.value = ClassDetailUiState.Error("Class not found")
                }
            } catch (e: Exception) {
                _uiState.value = ClassDetailUiState.Error(e.message ?: "Unknown error")
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
}

sealed class ClassDetailUiState {
    object Loading : ClassDetailUiState()
    data class Success(
        val classEntity: ClassEntity,
        val students: List<StudentEntity>
    ) : ClassDetailUiState()
    data class Error(val message: String) : ClassDetailUiState()
}
