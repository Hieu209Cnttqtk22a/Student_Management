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
class ClassListViewModel @Inject constructor(
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassListUiState>(ClassListUiState.Loading)
    val uiState: StateFlow<ClassListUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            try {
                classRepository.getAllClasses().collect { classes ->
                    _uiState.value = if (classes.isEmpty()) {
                        ClassListUiState.Empty
                    } else {
                        ClassListUiState.Success(classes)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ClassListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun createClass(classEntity: ClassEntity) {
        viewModelScope.launch {
            try {
                classRepository.createClass(classEntity)
            } catch (e: Exception) {
                _uiState.value = ClassListUiState.Error(e.message ?: "Failed to create class")
            }
        }
    }

    fun deleteClass(classEntity: ClassEntity) {
        viewModelScope.launch {
            try {
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
    data class Success(val classes: List<ClassEntity>) : ClassListUiState()
    data class Error(val message: String) : ClassListUiState()
}
