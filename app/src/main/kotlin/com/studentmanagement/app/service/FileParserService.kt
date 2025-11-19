package com.studentmanagement.app.service

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified file parser service that handles both CSV and Excel files
 * Automatically detects file type and uses appropriate parser
 * Requirements: 1.2, 1.3, 2.1, 2.2, 2.3
 */
@Singleton
class FileParserService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val csvParser = CSVParser(context)
    private val excelParser = ExcelParser(context)
    
    /**
     * Parse file from URI, automatically detecting file type
     * Supports .csv, .xls, and .xlsx files
     * Requirements: 1.2, 1.3, 4.1
     */
    suspend fun parseFile(uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        // Requirement 4.1: Validate file size before parsing
        if (!validateFileSize(uri)) {
            throw IllegalArgumentException("File is too large. Maximum file size is 10MB.")
        }
        
        val fileName = getFileName(uri)
        val fileExtension = fileName.substringAfterLast('.', "").lowercase()
        
        try {
            when (fileExtension) {
                "csv" -> csvParser.parseFile(uri)
                "xls", "xlsx" -> excelParser.parseFile(uri)
                else -> {
                    // Try to detect by content
                    try {
                        csvParser.parseFile(uri)
                    } catch (e: Exception) {
                        try {
                            excelParser.parseFile(uri)
                        } catch (e2: Exception) {
                            throw IllegalArgumentException("Unsupported file format. Please use .csv, .xls, or .xlsx files.")
                        }
                    }
                }
            }
        } catch (e: IllegalArgumentException) {
            // Re-throw our custom exceptions with clear messages
            throw e
        } catch (e: Exception) {
            // Requirement 4.1: Catch unexpected errors
            throw IllegalArgumentException("Failed to parse file: ${e.message}", e)
        }
    }
    
    /**
     * Get file name from URI
     */
    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment ?: uri.toString()
    }
    
    /**
     * Detect name columns from headers
     * Scans headers for keywords: "tên", "họ", "name", "student", "học sinh" (case-insensitive)
     * Identifies first name, last name, and full name columns
     * Requirements: 2.1, 2.2, 2.3
     */
    fun detectNameColumns(headers: List<String>): NameColumnInfo {
        var firstNameColumn: Int? = null
        var lastNameColumn: Int? = null
        var fullNameColumn: Int? = null
        
        headers.forEachIndexed { index, header ->
            val normalizedHeader = header.lowercase().trim()
            
            when {
                // Full name patterns - highest priority
                normalizedHeader.contains("tên") && normalizedHeader.contains("đầy đủ") ||
                normalizedHeader.contains("họ và tên") ||
                normalizedHeader.contains("họ tên") ||
                normalizedHeader.contains("full name") -> {
                    fullNameColumn = index
                }
                // Standalone "tên" is likely full name in Vietnamese context
                normalizedHeader == "tên" && fullNameColumn == null -> {
                    fullNameColumn = index
                }
                // First name patterns
                normalizedHeader.contains("tên") && !normalizedHeader.contains("họ") && 
                !normalizedHeader.contains("đầy đủ") && normalizedHeader != "tên" ||
                normalizedHeader.contains("first name") ||
                normalizedHeader.contains("given name") -> {
                    if (fullNameColumn == null) {
                        firstNameColumn = index
                    }
                }
                // Last name patterns
                normalizedHeader.contains("họ") && !normalizedHeader.contains("tên") ||
                normalizedHeader.contains("last name") ||
                normalizedHeader.contains("surname") ||
                normalizedHeader.contains("family name") -> {
                    lastNameColumn = index
                }
                // Generic name patterns - lowest priority
                normalizedHeader.contains("name") && !normalizedHeader.contains("first") && 
                !normalizedHeader.contains("last") && !normalizedHeader.contains("full") ||
                normalizedHeader.contains("student") && normalizedHeader.contains("name") ||
                normalizedHeader.contains("học sinh") -> {
                    if (fullNameColumn == null && firstNameColumn == null) {
                        fullNameColumn = index
                    }
                }
            }
        }
        
        return NameColumnInfo(
            firstNameColumn = firstNameColumn,
            lastNameColumn = lastNameColumn,
            fullNameColumn = fullNameColumn
        )
    }
    
    /**
     * Combine first and last name if both are present
     * Requirements: 2.3
     * 
     * @param row The data row
     * @param nameColumnInfo The detected name column information
     * @return Combined full name or null if no name found
     */
    fun extractName(row: List<String>, nameColumnInfo: NameColumnInfo): String? {
        return when {
            // Use full name if available
            nameColumnInfo.fullNameColumn != null -> {
                row.getOrNull(nameColumnInfo.fullNameColumn)?.trim()?.takeIf { it.isNotBlank() }
            }
            // Combine first and last name if both available
            nameColumnInfo.firstNameColumn != null && nameColumnInfo.lastNameColumn != null -> {
                val firstName = row.getOrNull(nameColumnInfo.firstNameColumn)?.trim() ?: ""
                val lastName = row.getOrNull(nameColumnInfo.lastNameColumn)?.trim() ?: ""
                
                if (firstName.isNotBlank() || lastName.isNotBlank()) {
                    // Vietnamese format: Last name + First name
                    "$lastName $firstName".trim()
                } else {
                    null
                }
            }
            // Use first name only
            nameColumnInfo.firstNameColumn != null -> {
                row.getOrNull(nameColumnInfo.firstNameColumn)?.trim()?.takeIf { it.isNotBlank() }
            }
            // Use last name only
            nameColumnInfo.lastNameColumn != null -> {
                row.getOrNull(nameColumnInfo.lastNameColumn)?.trim()?.takeIf { it.isNotBlank() }
            }
            else -> null
        }
    }
    
    /**
     * Validate file size before parsing
     * Requirements: 4.1 (from design document)
     * 
     * @param uri The file URI
     * @param maxSizeBytes Maximum allowed file size in bytes (default 10MB)
     * @return true if file size is acceptable
     */
    suspend fun validateFileSize(uri: Uri, maxSizeBytes: Long = 10 * 1024 * 1024): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val fileSize = inputStream.available().toLong()
                    fileSize <= maxSizeBytes
                } ?: false
            } catch (e: Exception) {
                false
            }
        }
}
