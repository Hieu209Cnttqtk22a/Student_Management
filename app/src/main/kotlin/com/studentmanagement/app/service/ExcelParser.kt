package com.studentmanagement.app.service

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import javax.inject.Inject

/**
 * Excel file parser implementation using Apache POI
 * Supports both .xls (HSSF) and .xlsx (XSSF) formats
 * Requirements: 1.2, 1.3
 */
class ExcelParser @Inject constructor(
    @ApplicationContext private val context: Context
) : FileParser {
    
    /**
     * Parse Excel file from URI
     * Reads first sheet and converts all cells to strings
     * Requirements: 1.2, 1.3, 4.1, 4.3
     */
    override suspend fun parseFile(uri: Uri): ParseResult = withContext(Dispatchers.IO) {
        val rows = mutableListOf<List<String>>()
        var currentRowNumber = 0
        
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val workbook = try {
                    createWorkbook(inputStream, uri)
                } catch (e: Exception) {
                    // Requirement 4.1: Handle invalid Excel format
                    throw IllegalArgumentException("Invalid Excel file format. Please ensure the file is a valid .xls or .xlsx file.", e)
                }
                
                if (workbook.numberOfSheets == 0) {
                    workbook.close()
                    throw IllegalArgumentException("Excel file contains no sheets")
                }
                
                val sheet = workbook.getSheetAt(0) // Get first sheet
                
                // Iterate through all rows
                for (row in sheet) {
                    currentRowNumber = row.rowNum + 1 // Excel rows are 0-indexed
                    try {
                        val rowData = mutableListOf<String>()
                        
                        // Get the last cell number to ensure we capture all columns
                        val lastCellNum = row.lastCellNum.toInt()
                        
                        for (cellIndex in 0 until lastCellNum) {
                            val cell = row.getCell(cellIndex)
                            rowData.add(getCellValueAsString(cell))
                        }
                        
                        // Only add non-empty rows
                        if (rowData.any { it.isNotBlank() }) {
                            rows.add(rowData)
                        }
                    } catch (e: Exception) {
                        // Requirement 4.1: Handle parse errors with line numbers
                        throw IllegalArgumentException("Error parsing row $currentRowNumber: ${e.message}", e)
                    }
                }
                
                workbook.close()
            } ?: throw IllegalArgumentException("Cannot open file. Please check file permissions.")
        } catch (e: IllegalArgumentException) {
            // Re-throw our custom exceptions
            throw e
        } catch (e: Exception) {
            // Requirement 4.1: Catch file read errors
            throw IllegalArgumentException("Failed to read Excel file: ${e.message}", e)
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
     * Create appropriate workbook based on file extension
     * .xls -> HSSFWorkbook
     * .xlsx -> XSSFWorkbook
     */
    private fun createWorkbook(inputStream: InputStream, uri: Uri): Workbook {
        val fileName = uri.lastPathSegment ?: ""
        
        return when {
            fileName.endsWith(".xlsx", ignoreCase = true) -> {
                XSSFWorkbook(inputStream)
            }
            fileName.endsWith(".xls", ignoreCase = true) -> {
                HSSFWorkbook(inputStream)
            }
            else -> {
                // Try XLSX first, fallback to XLS
                try {
                    XSSFWorkbook(inputStream)
                } catch (e: Exception) {
                    HSSFWorkbook(inputStream)
                }
            }
        }
    }
    
    /**
     * Convert cell value to string based on cell type
     * Handles all cell types: STRING, NUMERIC, BOOLEAN, FORMULA, BLANK, ERROR
     */
    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                // Check if it's a date
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue.toString()
                } else {
                    // Format numeric value, remove trailing .0 for whole numbers
                    val numericValue = cell.numericCellValue
                    if (numericValue == numericValue.toLong().toDouble()) {
                        numericValue.toLong().toString()
                    } else {
                        numericValue.toString()
                    }
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                // Try to get cached formula result
                try {
                    when (cell.cachedFormulaResultType) {
                        CellType.STRING -> cell.stringCellValue
                        CellType.NUMERIC -> {
                            val numericValue = cell.numericCellValue
                            if (numericValue == numericValue.toLong().toDouble()) {
                                numericValue.toLong().toString()
                            } else {
                                numericValue.toString()
                            }
                        }
                        CellType.BOOLEAN -> cell.booleanCellValue.toString()
                        else -> ""
                    }
                } catch (e: Exception) {
                    ""
                }
            }
            CellType.BLANK -> ""
            CellType.ERROR -> ""
            else -> ""
        }
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
