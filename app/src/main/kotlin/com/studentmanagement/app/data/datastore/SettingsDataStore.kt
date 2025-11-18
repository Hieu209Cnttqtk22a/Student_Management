package com.studentmanagement.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.studentmanagement.app.ui.screen.calendar.CalendarPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val REMINDER_ENABLED_KEY = booleanPreferencesKey("reminder_enabled")
        private val REMINDER_MINUTES_KEY = intPreferencesKey("reminder_minutes")
        private val CALENDAR_PERIOD_KEY = stringPreferencesKey("calendar_period")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val reminderEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_ENABLED_KEY] ?: true
    }

    val reminderMinutesFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REMINDER_MINUTES_KEY] ?: 15
    }

    val calendarPeriodFlow: Flow<CalendarPeriod> = context.dataStore.data.map { preferences ->
        val periodString = preferences[CALENDAR_PERIOD_KEY] ?: CalendarPeriod.MONTH.name
        try {
            CalendarPeriod.valueOf(periodString)
        } catch (e: IllegalArgumentException) {
            CalendarPeriod.MONTH
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_ENABLED_KEY] = enabled
        }
    }

    suspend fun setReminderMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_MINUTES_KEY] = minutes
        }
    }

    suspend fun setCalendarPeriod(period: CalendarPeriod) {
        context.dataStore.edit { preferences ->
            preferences[CALENDAR_PERIOD_KEY] = period.name
        }
    }
}
