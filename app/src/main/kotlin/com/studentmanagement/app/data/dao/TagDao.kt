package com.studentmanagement.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.studentmanagement.app.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert
    suspend fun insert(tagEntity: TagEntity): Long

    @Query("SELECT * FROM tags ORDER BY displayName ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): TagEntity?
}
