package com.studentmanagement.app

import android.app.Application
// import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

// @HiltAndroidApp
class StudentManagementApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Timber disabled for now
        // if (BuildConfig.DEBUG) {
        //     Timber.plant(Timber.DebugTree())
        // }
    }
}
