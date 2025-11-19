package com.studentmanagement.app.service

import android.net.Uri

/**
 * Data class representing the result of parsing a file
 * Requirements: 1.3, 2.1
 */
data class ParseResult(
    val headers: List<String>,
    val rows: List<List<String>>,
    val detectedNameColumns: NameColumnInfo
)

/**
 * Data class containing information about detected name columns
 * Requirements: 2.1, 2.2, 2.3
 */
data class NameColumnInfo(
    val firstNameColumn: Int?,
    val lastNameColumn: Int?,
    val fullNameColumn: Int?
)

/**
 * Interface for parsing files (CSV and Excel)
 * Requirements: 1.2, 1.3, 2.1
 */
interface FileParser {
    /**
     * Parse a file from the given URI
     * @param uri The URI of the file to parse
     * @return ParseResult containing headers, rows, and detected name columns
     * @throws Exception if file cannot be read or parsed
     */
    suspend fun parseFile(uri: Uri): ParseResult
    
    /**
     * Detect name columns from headers
     * Scans headers for keywords: "tên", "họ", "name", "student", "học sinh" (case-insensitive)
     * Requirements: 2.1, 2.2, 2.3
     * 
     * @param headers List of column headers
     * @return NameColumnInfo with detected column indices
     */
    fun detectNameColumns(headers: List<String>): NameColumnInfo
}
