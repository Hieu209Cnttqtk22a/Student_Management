package com.studentmanagement.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.repository.ClassRepository
import com.studentmanagement.app.service.ScheduleService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassListViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val scheduleService: ScheduleService,
    private val studentRepository: com.studentmanagement.app.data.repository.StudentRepository,
    private val reminderService: com.studentmanagement.app.service.ReminderService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassListUiState>(ClassListUiState.Loading)
    val uiState: StateFlow<ClassListUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            try {
                Log.d("ClassListViewModel", "Loading classes...")
                val classes = classRepository.getAllClasses().first()
                Log.d("ClassListViewModel", "Loaded ${classes.size} classes")
                
                if (classes.isEmpty()) {
                    _uiState.value = ClassListUiState.Empty
                } else {
                    // Get student count for each class
                    val studentCounts = mutableMapOf<Long, Int>()
                    classes.forEach { classEntity ->
                        val count = studentRepository.getStudentCountByClass(classEntity.id).first()
                        studentCounts[classEntity.id] = count
                    }
                    _uiState.value = ClassListUiState.Success(classes, studentCounts)
                }
            } catch (e: Exception) {
                Log.e("ClassListViewModel", "Failed to load classes", e)
                _uiState.value = ClassListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun createClass(classEntity: ClassEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                Log.d("ClassListViewModel", "Creating class: ${classEntity.name}")
                // Create class and capture the returned ID
                val newClassId = classRepository.createClass(classEntity)
                Log.d("ClassListViewModel", "Class created with ID: $newClassId")
                
                // Create a copy of classEntity with the actual ID
                val classWithId = classEntity.copy(id = newClassId)
                
                // Generate schedule for the newly created class with valid ID
                scheduleService.generateScheduleForClass(classWithId)
                Log.d("ClassListViewModel", "Schedule generated successfully")
                
                // Schedule reminders if enabled (Requirement 7.2)
                if (classWithId.reminderEnabled) {
                    reminderService.scheduleRemindersForClass(classWithId)
                    Log.d("ClassListViewModel", "Reminders scheduled for new class")
                }
                
                // Reload classes to update UI
                loadClasses()
                
                // Notify completion
                onComplete()
            } catch (e: Exception) {
                Log.e("ClassListViewModel", "Failed to create class", e)
                _uiState.value = ClassListUiState.Error(e.message ?: "Failed to create class")
                onComplete()
            }
        }
    }

    fun deleteClass(classEntity: ClassEntity) {
        viewModelScope.launch {
            try {
                // Cancel all reminders for this class before deletion (Requirement 7.4)
                reminderService.cancelRemindersForClass(classEntity.id)
                classRepository.deleteClass(classEntity)
            } catch (e: Exception) {
                _uiState.value = ClassListUiState.Error(e.message ?: "Failed to delete class")
            }
        }
    }

    fun deleteClassById(classId: Long) {
        viewModelScope.launch {
            try {
                val classEntity = classRepository.getClassById(classId)
                if (classEntity != null) {
                    // Cancel all reminders for this class before deletion (Requirement 7.4)
                    reminderService.cancelRemindersForClass(classId)
                    classRepository.deleteClass(classEntity)
                }
            } catch (e: Exception) {
                _uiState.value = ClassListUiState.Error(e.message ?: "Failed to delete class")
            }
        }
    }

    fun updateClass(classId: Long, name: String) {
        viewModelScope.launch {
            try {
                val classEntity = classRepository.getClassById(classId)
                if (classEntity != null) {
                    val updatedClass = classEntity.copy(name = name)
                    classRepository.updateClass(updatedClass)
                }
            } catch (e: Exception) {
                _uiState.value = ClassListUiState.Error(e.message ?: "Failed to update class")
            }
        }
    }
}

sealed class ClassListUiState {
    object Loading : ClassListUiState()
    object Empty : ClassListUiState()
    data class Success(
        val classes: List<ClassEntity>,
        val studentCounts: Map<Long, Int> = emptyMap()
    ) : ClassListUiState()
    data class Error(val message: String) : ClassListUiState()
}
