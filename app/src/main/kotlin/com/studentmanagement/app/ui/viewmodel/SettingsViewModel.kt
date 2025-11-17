package com.studentmanagement.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studentmanagement.app.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(true)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderMinutes = MutableStateFlow(15)
    val reminderMinutes: StateFlow<Int> = _reminderMinutes.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.darkModeFlow.collect { enabled ->
                _darkMode.value = enabled
            }
        }
        viewModelScope.launch {
            settingsDataStore.reminderEnabledFlow.collect { enabled ->
                _reminderEnabled.value = enabled
            }
        }
        viewModelScope.launch {
            settingsDataStore.reminderMinutesFlow.collect { minutes ->
                _reminderMinutes.value = minutes
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkMode(enabled)
            _darkMode.value = enabled
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setReminderEnabled(enabled)
            _reminderEnabled.value = enabled
        }
    }

    fun setReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.setReminderMinutes(minutes)
            _reminderMinutes.value = minutes
        }
    }
}
