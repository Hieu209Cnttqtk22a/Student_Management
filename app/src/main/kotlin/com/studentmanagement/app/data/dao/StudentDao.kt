package com.studentmanagement.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.studentmanagement.app.data.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert
    suspend fun insert(studentEntity: StudentEntity): Long

    @Update
    suspend fun update(studentEntity: StudentEntity)

    @Delete
    suspend fun delete(studentEntity: StudentEntity)

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY name ASC")
    fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>>

    @Query("SELECT COUNT(*) FROM students WHERE classId = :classId")
    fun getStudentCountByClass(classId: Long): Flow<Int>

    @Insert
    suspend fun insertAll(students: List<StudentEntity>): List<Long>

    @Query("SELECT * FROM students WHERE classId = :classId")
    suspend fun getStudentsByClassSync(classId: Long): List<StudentEntity>

    @Query("SELECT COUNT(*) FROM students WHERE classId = :classId AND LOWER(TRIM(name)) = LOWER(TRIM(:name))")
    suspend fun countStudentByNameInClass(classId: Long, name: String): Int
}
