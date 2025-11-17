package com.studentmanagement.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.entity.DailyRecordEntity
import com.studentmanagement.app.data.entity.TagEntity
import com.studentmanagement.app.data.repository.DailyRecordRepository
import com.studentmanagement.app.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentDailyEditViewModel @Inject constructor(
    private val dailyRecordRepository: DailyRecordRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentDailyEditUiState>(StudentDailyEditUiState.Loading)
    val uiState: StateFlow<StudentDailyEditUiState> = _uiState.asStateFlow()

    private val _availableTags = MutableStateFlow<List<TagEntity>>(emptyList())
    val availableTags: StateFlow<List<TagEntity>> = _availableTags.asStateFlow()

    private val _score = MutableStateFlow<String>("")
    val score: StateFlow<String> = _score.asStateFlow()

    private val _note = MutableStateFlow<String>("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _selectedTags = MutableStateFlow<List<TagEntity>>(emptyList())
    val selectedTags: StateFlow<List<TagEntity>> = _selectedTags.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    val selectedImages: StateFlow<List<String>> = _selectedImages.asStateFlow()

    init {
        loadAvailableTags()
    }

    private fun loadAvailableTags() {
        viewModelScope.launch {
            try {
                tagRepository.getAllTags().collect { tags ->
                    _availableTags.value = tags
                }
            } catch (e: Exception) {
                _uiState.value = StudentDailyEditUiState.Error(e.message ?: "Failed to load tags")
            }
        }
    }

    fun loadDailyRecord(studentId: Long, classId: Long, date: String) {
        viewModelScope.launch {
            try {
                _uiState.value = StudentDailyEditUiState.Loading
                val record = dailyRecordRepository.getDailyRecord(studentId, date)
                
                if (record != null) {
                    _score.value = record.score?.toString() ?: ""
                    _note.value = record.note ?: ""
                    
                    val tags = dailyRecordRepository.getTagsByDailyRecord(record.id)
                    _selectedTags.value = tags
                    
                    val attachments = dailyRecordRepository.getAttachmentsByDailyRecord(record.id)
                    _selectedImages.value = attachments.map { it.uri }
                    
                    _uiState.value = StudentDailyEditUiState.Success(record)
                } else {
                    _uiState.value = StudentDailyEditUiState.New(studentId, classId, date)
                }
            } catch (e: Exception) {
                _uiState.value = StudentDailyEditUiState.Error(e.message ?: "Failed to load record")
            }
        }
    }

    fun setScore(value: String) {
        _score.value = value
    }

    fun setNote(value: String) {
        _note.value = value
    }

    fun toggleTag(tag: TagEntity) {
        val current = _selectedTags.value.toMutableList()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        _selectedTags.value = current
    }

    fun addImage(uri: String) {
        _selectedImages.value = _selectedImages.value + uri
    }

    fun removeImage(uri: String) {
        _selectedImages.value = _selectedImages.value.filter { it != uri }
    }

    fun saveDailyRecord(studentId: Long, classId: Long, date: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val scoreValue = _score.value.toFloatOrNull()
                
                val record = when (val state = _uiState.value) {
                    is StudentDailyEditUiState.Success -> {
                        state.record.copy(
                            score = scoreValue,
                            note = _note.value,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    else -> {
                        DailyRecordEntity(
                            studentId = studentId,
                            classId = classId,
                            date = date,
                            score = scoreValue,
                            note = _note.value
                        )
                    }
                }

                dailyRecordRepository.saveDailyRecord(
                    dailyRecord = record,
                    tags = _selectedTags.value,
                    attachments = _selectedImages.value
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = StudentDailyEditUiState.Error(e.message ?: "Failed to save record")
            }
        }
    }
}

sealed class StudentDailyEditUiState {
    object Loading : StudentDailyEditUiState()
    data class New(val studentId: Long, val classId: Long, val date: String) : StudentDailyEditUiState()
    data class Success(val record: DailyRecordEntity) : StudentDailyEditUiState()
    data class Error(val message: String) : StudentDailyEditUiState()
}
