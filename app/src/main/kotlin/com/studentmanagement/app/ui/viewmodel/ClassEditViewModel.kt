package com.studentmanagement.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassEditViewModel @Inject constructor(
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassEditUiState>(ClassEditUiState.Loading)
    val uiState: StateFlow<ClassEditUiState> = _uiState.asStateFlow()

    fun loadClass(classId: Long) {
        viewModelScope.launch {
            try {
                val classEntity = classRepository.getClassById(classId)
                if (classEntity != null) {
                    _uiState.value = ClassEditUiState.Success(classEntity)
                } else {
                    _uiState.value = ClassEditUiState.Error("Class not found")
                }
            } catch (e: Exception) {
                _uiState.value = ClassEditUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateClass(
        classId: Long,
        name: String,
        scheduleDays: String? = null,
        startTime: Int? = null,
        repeatInterval: Int? = null,
        repeatUnit: String? = null
    ) {
        viewModelScope.launch {
            try {
                val classEntity = classRepository.getClassById(classId)
                if (classEntity != null) {
                    val updatedClass = classEntity.copy(
                        name = name,
                        scheduleDaysOfWeek = scheduleDays ?: classEntity.scheduleDaysOfWeek,
                        startTimeMinutes = startTime ?: classEntity.startTimeMinutes,
                        repeatInterval = repeatInterval ?: classEntity.repeatInterval,
                        repeatUnit = repeatUnit ?: classEntity.repeatUnit
                    )
                    classRepository.updateClass(updatedClass)
                }
            } catch (e: Exception) {
                _uiState.value = ClassEditUiState.Error(e.message ?: "Failed to update class")
            }
        }
    }

    fun deleteClass(classId: Long) {
        viewModelScope.launch {
            try {
                val classEntity = classRepository.getClassById(classId)
                if (classEntity != null) {
                    classRepository.deleteClass(classEntity)
                }
            } catch (e: Exception) {
                _uiState.value = ClassEditUiState.Error(e.message ?: "Failed to delete class")
            }
        }
    }
}

sealed class ClassEditUiState {
    object Loading : ClassEditUiState()
    data class Success(val classEntity: ClassEntity) : ClassEditUiState()
    data class Error(val message: String) : ClassEditUiState()
}
