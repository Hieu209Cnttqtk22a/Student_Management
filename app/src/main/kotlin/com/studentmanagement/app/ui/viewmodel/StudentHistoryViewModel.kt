package com.studentmanagement.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.entity.DailyRecordEntity
import com.studentmanagement.app.data.repository.DailyRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyRecordWithDetails(
    val id: Long,
    val studentId: Long,
    val classId: Long,
    val date: String,
    val score: Float?,
    val note: String?,
    val tags: List<String>,
    val imageUrls: List<String>
)

@HiltViewModel
class StudentHistoryViewModel @Inject constructor(
    private val dailyRecordRepository: DailyRecordRepository,
    private val studentRepository: com.studentmanagement.app.data.repository.StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentHistoryUiState>(StudentHistoryUiState.Loading)
    val uiState: StateFlow<StudentHistoryUiState> = _uiState.asStateFlow()

    private val _pageSize = MutableStateFlow(10)
    val pageSize: StateFlow<Int> = _pageSize.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _studentName = MutableStateFlow("")
    val studentName: StateFlow<String> = _studentName.asStateFlow()
    
    fun deleteStudent(studentId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val student = studentRepository.getStudentById(studentId)
                if (student != null) {
                    studentRepository.deleteStudent(student)
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.value = StudentHistoryUiState.Error(e.message ?: "Failed to delete student")
            }
        }
    }
    
    fun deleteDailyRecord(recordId: Long, studentId: Long, classId: Long, date: String) {
        viewModelScope.launch {
            try {
                // Create entity to delete
                val record = com.studentmanagement.app.data.entity.DailyRecordEntity(
                    id = recordId,
                    studentId = studentId,
                    classId = classId,
                    date = date,
                    score = null,
                    note = null
                )
                dailyRecordRepository.deleteDailyRecord(record)
                // Reload will happen automatically via Flow
            } catch (e: Exception) {
                _uiState.value = StudentHistoryUiState.Error(e.message ?: "Failed to delete record")
            }
        }
    }

    fun loadStudentHistory(studentId: Long, startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = StudentHistoryUiState.Loading
                
                // Load student name
                val student = studentRepository.getStudentById(studentId)
                _studentName.value = student?.name ?: ""
                
                val flow = if (startDate != null && endDate != null) {
                    dailyRecordRepository.getRecordsByStudentAndDateRange(studentId, startDate, endDate)
                } else {
                    dailyRecordRepository.getRecordsByStudent(studentId)
                }

                flow.collect { records ->
                    if (records.isEmpty()) {
                        _uiState.value = StudentHistoryUiState.Empty
                    } else {
                        // Load tags and attachments for each record
                        val recordsWithDetails = records.map { record ->
                            val tags = dailyRecordRepository.getTagsByDailyRecord(record.id)
                            val attachments = dailyRecordRepository.getAttachmentsByDailyRecord(record.id)
                            
                            DailyRecordWithDetails(
                                id = record.id,
                                studentId = record.studentId,
                                classId = record.classId,
                                date = record.date,
                                score = record.score,
                                note = record.note,
                                tags = tags.map { it.displayName },
                                imageUrls = attachments.map { it.uri }
                            )
                        }
                        
                        val totalPages = (recordsWithDetails.size + _pageSize.value - 1) / _pageSize.value
                        val pagedRecords = recordsWithDetails.chunked(_pageSize.value)
                        
                        _uiState.value = StudentHistoryUiState.Success(
                            allRecords = recordsWithDetails,
                            pagedRecords = pagedRecords,
                            totalPages = totalPages,
                            averageScore = calculateAverageScore(recordsWithDetails)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = StudentHistoryUiState.Error(e.message ?: "Failed to load history")
            }
        }
    }

    fun setPageSize(size: Int) {
        _pageSize.value = size
        _currentPage.value = 0
    }

    fun nextPage() {
        val state = _uiState.value
        if (state is StudentHistoryUiState.Success) {
            if (_currentPage.value < state.totalPages - 1) {
                _currentPage.value++
            }
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
        }
    }

    private fun calculateAverageScore(records: List<DailyRecordWithDetails>): Float {
        val scoresOnly = records.mapNotNull { it.score }
        return if (scoresOnly.isNotEmpty()) {
            scoresOnly.average().toFloat()
        } else {
            0f
        }
    }
}

sealed class StudentHistoryUiState {
    object Loading : StudentHistoryUiState()
    object Empty : StudentHistoryUiState()
    data class Success(
        val allRecords: List<DailyRecordWithDetails>,
        val pagedRecords: List<List<DailyRecordWithDetails>>,
        val totalPages: Int,
        val averageScore: Float
    ) : StudentHistoryUiState()
    data class Error(val message: String) : StudentHistoryUiState()
}
