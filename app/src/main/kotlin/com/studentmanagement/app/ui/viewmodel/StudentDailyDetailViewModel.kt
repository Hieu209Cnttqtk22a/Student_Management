package com.studentmanagement.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.entity.DailyRecordEntity
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.data.entity.TagEntity
import com.studentmanagement.app.data.repository.DailyRecordRepository
import com.studentmanagement.app.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentDailyDetailViewModel @Inject constructor(
    private val dailyRecordRepository: DailyRecordRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentDailyDetailUiState>(StudentDailyDetailUiState.Loading)
    val uiState: StateFlow<StudentDailyDetailUiState> = _uiState.asStateFlow()

    fun loadRecordDetail(recordId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = StudentDailyDetailUiState.Loading
                
                // This is a placeholder - need to add getRecordById to repository
                // For now, show error message
                _uiState.value = StudentDailyDetailUiState.Error("Chức năng đang được phát triển")
            } catch (e: Exception) {
                _uiState.value = StudentDailyDetailUiState.Error(e.message ?: "Failed to load record")
            }
        }
    }
}

sealed class StudentDailyDetailUiState {
    object Loading : StudentDailyDetailUiState()
    data class Success(
        val record: DailyRecordEntity,
        val student: StudentEntity,
        val tags: List<TagEntity>,
        val imageUrls: List<String>
    ) : StudentDailyDetailUiState()
    data class Error(val message: String) : StudentDailyDetailUiState()
}
