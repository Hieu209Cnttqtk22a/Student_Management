package com.studentmanagement.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.data.repository.StudentRepository
import com.studentmanagement.app.service.FileParserService
import com.studentmanagement.app.service.ParseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for student import functionality.
 * Handles file selection, parsing, and bulk student import.
 * Requirements: 3.1, 5.1, 5.2
 */
@HiltViewModel
class ImportViewModel @Inject constructor(
    private val fileParserService: FileParserService,
    private val studentRepository: StudentRepository
) : ViewModel() {
    
    private val _parseResult = MutableStateFlow<ParseResult?>(null)
    val parseResult: StateFlow<ParseResult?> = _parseResult.asStateFlow()
    
    private val _importProgress = MutableStateFlow(ImportProgress(0, 0, false))
    val importProgress: StateFlow<ImportProgress> = _importProgress.asStateFlow()
    
    private val _importSummary = MutableStateFlow<ImportSummary?>(null)
    val importSummary: StateFlow<ImportSummary?> = _importSummary.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Requirement 12.4: Track parsing state
    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()
    
    /**
     * Select and parse a file from the given URI.
     * Requirement 1.1, 1.2, 1.3, 4.1, 12.4: File selection and parsing with error handling and loading state
     */
    fun selectFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Requirement 12.4: Set loading state
                _isParsing.value = true
                _errorMessage.value = null
                _parseResult.value = null
                
                // Parse the file
                val result = fileParserService.parseFile(uri)
                
                // Validate that we have data
                if (result.rows.isEmpty()) {
                    _errorMessage.value = "File contains no data rows"
                    _isParsing.value = false
                    return@launch
                }
                
                // Check if name columns were detected
                val nameInfo = result.detectedNameColumns
                if (nameInfo.fullNameColumn == null && 
                    nameInfo.firstNameColumn == null && 
                    nameInfo.lastNameColumn == null) {
                    // Requirement 4.2: Inform user if no names detected
                    _errorMessage.value = "No name columns detected. Please select the name column manually."
                }
                
                _parseResult.value = result
                _isParsing.value = false
            } catch (e: IllegalArgumentException) {
                // Requirement 4.1, 12.4: Display clear error messages
                _errorMessage.value = e.message ?: "Failed to read file"
                _parseResult.value = null
                _isParsing.value = false
            } catch (e: Exception) {
                // Requirement 4.1, 12.4: Handle unexpected errors
                _errorMessage.value = "Unexpected error: ${e.message}"
                _parseResult.value = null
                _isParsing.value = false
            }
        }
    }
    
    /**
     * Confirm import and create student records using bulk import.
     * Requirements: 3.1, 3.3, 3.4, 4.1, 4.3, 5.1, 5.2, 5.3
     */
    fun confirmImport(classId: Long, nameColumnIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = _parseResult.value
                if (result == null) {
                    _errorMessage.value = "No file data available. Please select a file first."
                    return@launch
                }
                
                // Validate column index
                if (nameColumnIndex < 0 || nameColumnIndex >= result.headers.size) {
                    _errorMessage.value = "Invalid column selection. Please select a valid name column."
                    return@launch
                }
                
                _importProgress.value = ImportProgress(0, result.rows.size, true)
                _errorMessage.value = null
                
                // Extract student names from the selected column
                val studentNames = mutableListOf<String>()
                val extractionErrors = mutableListOf<String>()
                
                result.rows.forEachIndexed { index, row ->
                    try {
                        val name = row.getOrNull(nameColumnIndex)
                        if (name != null) {
                            studentNames.add(name)
                        }
                    } catch (e: Exception) {
                        // Requirement 4.1: Track extraction errors with line numbers
                        extractionErrors.add("Row ${index + 2}: Failed to extract name - ${e.message}")
                    }
                }
                
                if (studentNames.isEmpty()) {
                    _errorMessage.value = "No valid student names found in the selected column"
                    _importProgress.value = ImportProgress(0, 0, false)
                    return@launch
                }
                
                // Update progress as we prepare
                _importProgress.value = ImportProgress(result.rows.size / 2, result.rows.size, true)
                
                // Use bulk import method with transaction support (Requirements 5.1, 5.2, 5.3)
                val importResult = try {
                    studentRepository.bulkImportStudents(studentNames, classId)
                } catch (e: Exception) {
                    // Requirement 4.3: Handle transaction rollback
                    throw IllegalStateException("Import transaction failed and was rolled back: ${e.message}", e)
                }
                
                // Update final progress
                _importProgress.value = ImportProgress(result.rows.size, result.rows.size, false)
                
                // Combine extraction errors with import errors
                val allErrors = extractionErrors + importResult.errors
                
                // Set import summary (Requirement 5.2)
                _importSummary.value = ImportSummary(
                    added = importResult.added,
                    skipped = importResult.skipped,
                    errors = allErrors
                )
                
                // Requirement 4.1: Show error message if there were errors but some succeeded
                if (allErrors.isNotEmpty() && importResult.added > 0) {
                    _errorMessage.value = "Import completed with ${allErrors.size} error(s). See summary for details."
                }
            } catch (e: IllegalStateException) {
                // Requirement 4.3: Handle transaction rollback errors
                _errorMessage.value = e.message ?: "Import failed and was rolled back"
                _importProgress.value = ImportProgress(0, 0, false)
                _importSummary.value = null
            } catch (e: IllegalArgumentException) {
                // Requirement 4.1: Handle validation errors
                _errorMessage.value = e.message ?: "Invalid import data"
                _importProgress.value = ImportProgress(0, 0, false)
                _importSummary.value = null
            } catch (e: Exception) {
                // Requirement 4.1, 4.3: Handle unexpected errors
                _errorMessage.value = "Import failed: ${e.message}"
                _importProgress.value = ImportProgress(0, 0, false)
                _importSummary.value = null
            }
        }
    }
    
    /**
     * Cancel the import process and reset state.
     */
    fun cancelImport() {
        _parseResult.value = null
        _importProgress.value = ImportProgress(0, 0, false)
        _importSummary.value = null
        _errorMessage.value = null
        _isParsing.value = false
    }
    
    /**
     * Clear error message.
     * Requirement 12.4: Allow dismissing errors
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * Data class representing import progress.
 * Requirement 5.1: Track current/total progress
 */
data class ImportProgress(
    val current: Int,
    val total: Int,
    val isImporting: Boolean
)

/**
 * Data class representing import summary.
 * Requirement 5.2: Show added/skipped/errors
 */
data class ImportSummary(
    val added: Int,
    val skipped: Int,
    val errors: List<String>
)
