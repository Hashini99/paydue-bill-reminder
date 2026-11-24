package com.hrdevzone.paydue.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdevzone.paydue.data.prefs.ThemeMode
import com.hrdevzone.paydue.data.prefs.UserPreferences
import com.hrdevzone.paydue.data.prefs.UserSettings
import com.hrdevzone.paydue.data.repository.DueRepository
import com.hrdevzone.paydue.util.CurrencyOption
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val dueCount: Int = 0
)

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val dueRepository: DueRepository
) : ViewModel() {

    private val dueCountFlow = flow {
        emit(dueRepository.count())
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.settings,
        dueCountFlow
    ) { settings, count ->
        SettingsUiState(settings = settings, dueCount = count)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateBusinessName(value: String) {
        viewModelScope.launch { userPreferences.setBusinessName(value) }
    }

    fun updateCurrency(option: CurrencyOption) {
        viewModelScope.launch { userPreferences.setCurrency(option) }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch { userPreferences.setReminderTime(hour, minute) }
    }

    fun updateDefaultPaymentNote(value: String) {
        viewModelScope.launch { userPreferences.setDefaultPaymentNote(value) }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch { userPreferences.setThemeMode(mode) }
    }

    fun setPro(value: Boolean) {
        viewModelScope.launch { userPreferences.setPro(value) }
    }

    fun setAppLockEnabled(value: Boolean) {
        viewModelScope.launch { userPreferences.setAppLockEnabled(value) }
    }

    fun setBiometricEnabled(value: Boolean) {
        viewModelScope.launch { userPreferences.setBiometricEnabled(value) }
    }

    fun removePin() {
        viewModelScope.launch { userPreferences.clearPin() }
    }
}
