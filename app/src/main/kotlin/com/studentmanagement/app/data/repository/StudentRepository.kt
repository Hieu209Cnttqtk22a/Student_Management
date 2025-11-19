package com.studentmanagement.app.data.repository

import androidx.room.withTransaction
import com.studentmanagement.app.data.dao.StudentDao
import com.studentmanagement.app.data.database.StudentManagementDatabase
import com.studentmanagement.app.data.entity.StudentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudentRepository @Inject constructor(
    private val studentDao: StudentDao,
    private val database: StudentManagementDatabase
) {
    suspend fun createStudent(studentEntity: StudentEntity): Long {
        return studentDao.insert(studentEntity)
    }

    suspend fun updateStudent(studentEntity: StudentEntity) {
        studentDao.update(studentEntity)
    }

    suspend fun deleteStudent(studentEntity: StudentEntity) {
        studentDao.delete(studentEntity)
    }

    suspend fun getStudentById(id: Long): StudentEntity? {
        return studentDao.getById(id)
    }

    fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>> {
        return studentDao.getStudentsByClass(classId)
    }

    fun getStudentCountByClass(classId: Long): Flow<Int> {
        return studentDao.getStudentCountByClass(classId)
    }

    /**
     * Bulk import students with duplicate detection and transaction support.
     * Requirements: 3.1, 3.3, 3.4, 4.1, 4.3
     * 
     * @param studentNames List of student names to import
     * @param classId The class ID to import students into
     * @return ImportResult containing counts of added, skipped, and errors
     */
    suspend fun bulkImportStudents(studentNames: List<String>, classId: Long): ImportResult {
        var added = 0
        var skipped = 0
        val errors = mutableListOf<String>()

        // Validate inputs
        if (studentNames.isEmpty()) {
            return ImportResult(0, 0, listOf("No student names provided"))
        }

        try {
            // Use transaction for atomicity (Requirement 3.1, 4.3)
            database.withTransaction {
                try {
                    // Get existing students for duplicate detection (Requirement 3.4)
                    val existingStudents = studentDao.getStudentsByClassSync(classId)
                    val existingNames = existingStudents
                        .map { it.name.trim().lowercase() }
                        .toSet()

                    val studentsToInsert = mutableListOf<StudentEntity>()

                    studentNames.forEachIndexed { index, name ->
                        try {
                            // Requirement 3.3: Trim whitespace
                            val trimmedName = name.trim()
                            
                            if (trimmedName.isEmpty()) {
                                // Requirement 3.2: Skip empty names
                                skipped++
                            } else if (trimmedName.length > 255) {
                                // Validate name length
                                errors.add("Row ${index + 2}: Name too long (max 255 characters)")
                                skipped++
                            } else {
                                // Requirement 3.4: Check for duplicates (case-insensitive, trimmed)
                                if (existingNames.contains(trimmedName.lowercase())) {
                                    skipped++
                                } else {
                                    studentsToInsert.add(
                                        StudentEntity(
                                            name = trimmedName,
                                            classId = classId
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            // Requirement 4.1: Track errors with context and line numbers
                            errors.add("Row ${index + 2}: ${e.message ?: "Unknown error"}")
                            skipped++
                        }
                    }

                    // Batch insert all valid students (Requirement 3.1)
                    if (studentsToInsert.isNotEmpty()) {
                        try {
                            studentDao.insertAll(studentsToInsert)
                            added = studentsToInsert.size
                        } catch (e: Exception) {
                            // Requirement 4.1, 4.3: Handle database insertion errors
                            throw IllegalStateException("Failed to insert students into database: ${e.message}", e)
                        }
                    }
                } catch (e: Exception) {
                    // Requirement 4.3: Ensure transaction rollback on any error
                    throw e
                }
            }
        } catch (e: IllegalStateException) {
            // Requirement 4.3: Transaction rolled back, re-throw with context
            errors.add("Database error: ${e.message}")
            throw IllegalStateException("Import transaction failed and was rolled back", e)
        } catch (e: Exception) {
            // Requirement 4.1, 4.3: Rollback on failure (handled by Room transaction)
            errors.add("Import failed: ${e.message ?: "Unknown error"}")
            // Transaction automatically rolls back on exception
            throw IllegalStateException("Import failed: ${e.message}", e)
        }

        // Return import summary (Requirement 3.5)
        return ImportResult(added, skipped, errors)
    }

    /**
     * Check if a student with the given name exists in the class.
     * Requirements: 3.4
     * 
     * @param classId The class ID
     * @param name The student name to check
     * @return true if student exists, false otherwise
     */
    suspend fun studentExistsInClass(classId: Long, name: String): Boolean {
        return studentDao.countStudentByNameInClass(classId, name) > 0
    }
}

/**
 * Data class representing the result of a bulk import operation.
 * Requirements: 3.5, 5.1
 */
data class ImportResult(
    val added: Int,
    val skipped: Int,
    val errors: List<String>
)
