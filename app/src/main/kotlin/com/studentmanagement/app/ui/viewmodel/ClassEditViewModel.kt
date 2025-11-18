package com.studentmanagement.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.repository.ClassRepository
import com.studentmanagement.app.service.ScheduleService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassEditViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val scheduleService: ScheduleService
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
        scheduleDays: String,
        startTime: Int?,
        repeatInterval: Int,
        repeatUnit: String
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClassEditViewModel", "updateClass called: classId=$classId")
                val classEntity = classRepository.getClassById(classId)
                if (classEntity != null) {
                    android.util.Log.d("ClassEditViewModel", "Found class entity: ${classEntity.name}")
                    val updatedClass = classEntity.copy(
                        name = name,
                        scheduleDaysOfWeek = scheduleDays,
                        startTimeMinutes = startTime,
                        repeatInterval = repeatInterval,
                        repeatUnit = repeatUnit
                    )
                    
                    // Kiểm tra xem lịch học có thay đổi không (bao gồm cả startTime)
                    val scheduleChanged = 
                        updatedClass.scheduleDaysOfWeek != classEntity.scheduleDaysOfWeek ||
                        updatedClass.startTimeMinutes != classEntity.startTimeMinutes ||
                        updatedClass.repeatInterval != classEntity.repeatInterval ||
                        updatedClass.repeatUnit != classEntity.repeatUnit
                    
                    android.util.Log.d("ClassEditViewModel", "Schedule changed: $scheduleChanged")
                    android.util.Log.d("ClassEditViewModel", "Old: days=${classEntity.scheduleDaysOfWeek}, time=${classEntity.startTimeMinutes}, interval=${classEntity.repeatInterval}, unit=${classEntity.repeatUnit}")
                    android.util.Log.d("ClassEditViewModel", "New: days=$scheduleDays, time=$startTime, interval=$repeatInterval, unit=$repeatUnit")
                    
                    // Cập nhật lớp học
                    classRepository.updateClass(updatedClass)
                    android.util.Log.d("ClassEditViewModel", "Class updated in repository")
                    
                    // Nếu lịch học thay đổi, tạo lại lịch cho học sinh
                    if (scheduleChanged) {
                        android.util.Log.d("ClassEditViewModel", "Regenerating schedule...")
                        scheduleService.regenerateScheduleForClass(updatedClass)
                        android.util.Log.d("ClassEditViewModel", "Schedule regenerated")
                    }
                } else {
                    android.util.Log.e("ClassEditViewModel", "Class entity not found for id: $classId")
                }
            } catch (e: Exception) {
                android.util.Log.e("ClassEditViewModel", "Error updating class", e)
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
