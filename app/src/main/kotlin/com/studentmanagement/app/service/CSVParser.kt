package com.studentmanagement.app.service

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * CSV file parser implementation
 * Requirements: 1.2, 1.3
 */
class CSVParser @Inject constructor(
    @ApplicationContext private val context: Context
) : FileParser {
    
    /**
     * Parse CSV file from URI
     * Supports comma and semicolon delimiters
     * Handles quoted fields and escape characters
     * Requirements: 1.2, 1.3, 4.1, 4.3
     */
    override suspend fun parseFile(uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        val rows = mutableListOf<List<String>>()
        var lineNumber = 0
        
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        lineNumber++
                        line?.let {
                            if (it.isNotBlank()) {
                                try {
                                    val parsedRow = parseCSVLine(it)
                                    rows.add(parsedRow)
                                } catch (e: Exception) {
                                    // Requirement 4.1: Handle parse errors with line numbers
                                    throw IllegalArgumentException("Error parsing line $lineNumber: ${e.message}", e)
                                }
                            }
                        }
                    }
                }
            } ?: throw IllegalArgumentException("Cannot open file. Please check file permissions.")
        } catch (e: IllegalArgumentException) {
            // Re-throw our custom exceptions
            throw e
        } catch (e: Exception) {
            // Requirement 4.1: Catch file read errors
            throw IllegalArgumentException("Failed to read CSV file: ${e.message}", e)
        }
        
        if (rows.isEmpty()) {
            throw IllegalArgumentException("File is empty or contains no valid data")
        }
        
        val headers = rows.first()
        val dataRows = rows.drop(1)
        val detectedNameColumns = detectNameColumns(headers)
        
        ParseResult(
            headers = headers,
            rows = dataRows,
            detectedNameColumns = detectedNameColumns
        )
    }
    
    /**
     * Parse a single CSV line handling quoted fields and escape characters
     * Supports both comma and semicolon delimiters
     */
    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0
        
        // Detect delimiter (comma or semicolon)
        val delimiter = if (line.contains(';')) ';' else ','
        
        while (i < line.length) {
            val char = line[i]
            
            when {
                // Handle quotes
                char == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // Escaped quote
                        currentField.append('"')
                        i++ // Skip next quote
                    } else {
                        // Toggle quote state
                        inQuotes = !inQuotes
                    }
                }
                // Handle delimiter
                char == delimiter && !inQuotes -> {
                    result.add(currentField.toString().trim())
                    currentField.clear()
                }
                // Regular character
                else -> {
                    currentField.append(char)
                }
            }
            i++
        }
        
        // Add last field
        result.add(currentField.toString().trim())
        
        return result
    }
    
    /**
     * Detect name columns from headers
     * Scans for keywords: "tên", "họ", "name", "student", "học sinh" (case-insensitive)
     * Requirements: 2.1, 2.2, 2.3
     */
    override fun detectNameColumns(headers: List<String>): NameColumnInfo {
        var firstNameColumn: Int? = null
        var lastNameColumn: Int? = null
        var fullNameColumn: Int? = null
        
        headers.forEachIndexed { index, header ->
            val normalizedHeader = header.lowercase().trim()
            
            when {
                // Full name patterns
                normalizedHeader.contains("tên") && normalizedHeader.contains("đầy đủ") ||
                normalizedHeader.contains("họ và tên") ||
                normalizedHeader.contains("họ tên") ||
                normalizedHeader.contains("full name") ||
                normalizedHeader == "tên" && fullNameColumn == null -> {
                    fullNameColumn = index
                }
                // First name patterns
                normalizedHeader.contains("tên") && !normalizedHeader.contains("họ") ||
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
                // Generic name patterns
                normalizedHeader.contains("name") ||
                normalizedHeader.contains("student") ||
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
}
