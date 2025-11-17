package com.studentmanagement.app.data.repository

import com.studentmanagement.app.data.dao.ClassDao
import com.studentmanagement.app.data.entity.ClassEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClassRepository @Inject constructor(
    private val classDao: ClassDao
) {
    suspend fun createClass(classEntity: ClassEntity): Long {
        return classDao.insert(classEntity)
    }

    suspend fun updateClass(classEntity: ClassEntity) {
        classDao.update(classEntity)
    }

    suspend fun deleteClass(classEntity: ClassEntity) {
        classDao.delete(classEntity)
    }

    suspend fun getClassById(id: Long): ClassEntity? {
        return classDao.getById(id)
    }

    fun getAllClasses(): Flow<List<ClassEntity>> {
        return classDao.getAllClasses()
    }

    fun getClassCount(): Flow<Int> {
        return classDao.getClassCount()
    }
}
