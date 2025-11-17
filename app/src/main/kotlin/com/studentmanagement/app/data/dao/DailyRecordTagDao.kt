package com.studentmanagement.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.studentmanagement.app.data.entity.DailyRecordTagCrossRef
import com.studentmanagement.app.data.entity.TagEntity

@Dao
interface DailyRecordTagDao {
    @Insert
    suspend fun insert(crossRef: DailyRecordTagCrossRef)

    @Delete
    suspend fun delete(crossRef: DailyRecordTagCrossRef)

    @Query("SELECT tags.* FROM tags INNER JOIN daily_record_tag_cross_ref ON tags.id = daily_record_tag_cross_ref.tagId WHERE daily_record_tag_cross_ref.dailyRecordId = :dailyRecordId")
    suspend fun getTagsByDailyRecord(dailyRecordId: Long): List<TagEntity>

    @Query("DELETE FROM daily_record_tag_cross_ref WHERE dailyRecordId = :dailyRecordId")
    suspend fun deleteTagsByDailyRecord(dailyRecordId: Long)
}
