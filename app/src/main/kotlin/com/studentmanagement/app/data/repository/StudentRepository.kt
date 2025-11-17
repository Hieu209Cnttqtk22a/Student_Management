package com.studentmanagement.app.data.repository

import com.studentmanagement.app.data.dao.StudentDao
import com.studentmanagement.app.data.entity.StudentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudentRepository @Inject constructor(
    private val studentDao: StudentDao
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
}
