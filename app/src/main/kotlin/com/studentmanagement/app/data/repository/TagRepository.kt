package com.studentmanagement.app.data.repository

import com.studentmanagement.app.data.dao.TagDao
import com.studentmanagement.app.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    fun getAllTags(): Flow<List<TagEntity>> {
        return tagDao.getAllTags()
    }

    suspend fun getTagByCode(code: String): TagEntity? {
        return tagDao.getByCode(code)
    }
}
