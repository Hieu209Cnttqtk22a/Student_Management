package com.studentmanagement.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.studentmanagement.app.data.dao.AttachmentDao
import com.studentmanagement.app.data.dao.ClassDao
import com.studentmanagement.app.data.dao.DailyRecordDao
import com.studentmanagement.app.data.dao.DailyRecordTagDao
import com.studentmanagement.app.data.dao.StudentDao
import com.studentmanagement.app.data.dao.TagDao
import com.studentmanagement.app.data.entity.AttachmentEntity
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.entity.DailyRecordEntity
import com.studentmanagement.app.data.entity.DailyRecordTagCrossRef
import com.studentmanagement.app.data.entity.DailyTag
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.data.entity.TagEntity

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        DailyRecordEntity::class,
        TagEntity::class,
        DailyRecordTagCrossRef::class,
        AttachmentEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class StudentManagementDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun tagDao(): TagDao
    abstract fun dailyRecordTagDao(): DailyRecordTagDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile
        private var INSTANCE: StudentManagementDatabase? = null

        fun getDatabase(context: Context): StudentManagementDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentManagementDatabase::class.java,
                    "student_management_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
