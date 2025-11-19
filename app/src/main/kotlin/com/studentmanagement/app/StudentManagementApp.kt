package com.studentmanagement.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.studentmanagement.app.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for Student Management App.
 * Requirement 8.3: Initialize notification channel on app startup
 * Requirement 12.3: Configure WorkManager with Hilt
 */
@HiltAndroidApp
class StudentManagementApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize notification channel (Requirement 8.3)
        // Note: NotificationHelper is injected by Hilt and creates the channel in its init block
        // This ensures the channel is created when the app starts
        Timber.d("StudentManagementApp initialized")
    }
    
    /**
     * Provide WorkManager configuration with Hilt worker factory.
     * Requirement 12.3: Configure WorkManager for reminder persistence
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
