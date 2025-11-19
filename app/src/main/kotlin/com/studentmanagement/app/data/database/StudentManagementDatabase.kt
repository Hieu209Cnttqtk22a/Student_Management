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
import com.studentmanagement.app.data.dao.ReminderDao
import com.studentmanagement.app.data.dao.StudentDao
import com.studentmanagement.app.data.dao.TagDao
import com.studentmanagement.app.data.entity.AttachmentEntity
import com.studentmanagement.app.data.entity.ClassEntity
import com.studentmanagement.app.data.entity.DailyRecordEntity
import com.studentmanagement.app.data.entity.DailyRecordTagCrossRef
import com.studentmanagement.app.data.entity.ReminderEntity
import com.studentmanagement.app.data.entity.StudentEntity
import com.studentmanagement.app.data.entity.TagEntity

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        DailyRecordEntity::class,
        TagEntity::class,
        DailyRecordTagCrossRef::class,
        AttachmentEntity::class,
        ReminderEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class StudentManagementDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun tagDao(): TagDao
    abstract fun dailyRecordTagDao(): DailyRecordTagDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: StudentManagementDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Remove duplicate records, keeping only the most recent one
                database.execSQL("""
                    DELETE FROM daily_records 
                    WHERE id NOT IN (
                        SELECT MAX(id) 
                        FROM daily_records 
                        GROUP BY studentId, classId, date
                    )
                """)
                
                // Create unique index to prevent future duplicates
                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_daily_records_studentId_classId_date 
                    ON daily_records(studentId, classId, date)
                """)
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add reminder fields to classes table
                database.execSQL("""
                    ALTER TABLE classes ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE classes ADD COLUMN reminderLeadTimeMinutes INTEGER NOT NULL DEFAULT 30
                """)
                
                // Create reminders table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        classId INTEGER NOT NULL,
                        scheduledTime INTEGER NOT NULL,
                        leadTimeMinutes INTEGER NOT NULL,
                        isDelivered INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(classId) REFERENCES classes(id) ON DELETE CASCADE
                    )
                """)
                
                // Create index on classId for better query performance
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_reminders_classId ON reminders(classId)
                """)
            }
        }

        fun getDatabase(context: Context): StudentManagementDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentManagementDatabase::class.java,
                    "student_management_db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
