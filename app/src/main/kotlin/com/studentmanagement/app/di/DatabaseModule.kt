package com.studentmanagement.app.di

import android.content.Context
import com.studentmanagement.app.data.database.StudentManagementDatabase
import com.studentmanagement.app.data.dao.ClassDao
import com.studentmanagement.app.data.dao.StudentDao
import com.studentmanagement.app.data.dao.DailyRecordDao
import com.studentmanagement.app.data.dao.TagDao
import com.studentmanagement.app.data.dao.DailyRecordTagDao
import com.studentmanagement.app.data.dao.AttachmentDao
import com.studentmanagement.app.data.dao.ReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): StudentManagementDatabase {
        return StudentManagementDatabase.getDatabase(context)
    }

    @Provides
    fun provideClassDao(database: StudentManagementDatabase): ClassDao {
        return database.classDao()
    }

    @Provides
    fun provideStudentDao(database: StudentManagementDatabase): StudentDao {
        return database.studentDao()
    }

    @Provides
    fun provideDailyRecordDao(database: StudentManagementDatabase): DailyRecordDao {
        return database.dailyRecordDao()
    }

    @Provides
    fun provideTagDao(database: StudentManagementDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun provideDailyRecordTagDao(database: StudentManagementDatabase): DailyRecordTagDao {
        return database.dailyRecordTagDao()
    }

    @Provides
    fun provideAttachmentDao(database: StudentManagementDatabase): AttachmentDao {
        return database.attachmentDao()
    }

    @Provides
    fun provideReminderDao(database: StudentManagementDatabase): ReminderDao {
        return database.reminderDao()
    }
}
