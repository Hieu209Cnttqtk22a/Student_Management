package com.studentmanagement.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.studentmanagement.app.data.entity.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Insert
    suspend fun insert(classEntity: ClassEntity): Long

    @Update
    suspend fun update(classEntity: ClassEntity)

    @Delete
    suspend fun delete(classEntity: ClassEntity)

    @Query("SELECT * FROM classes WHERE id = :id")
    suspend fun getById(id: Long): ClassEntity?

    @Query("SELECT * FROM classes ORDER BY createdAt DESC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT COUNT(*) FROM classes")
    fun getClassCount(): Flow<Int>
}
