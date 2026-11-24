package com.hrdevzone.paydue.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdevzone.paydue.data.prefs.UserPreferences
import com.hrdevzone.paydue.data.prefs.UserSettings
import com.hrdevzone.paydue.util.AppLockState
import com.hrdevzone.paydue.util.PinHasher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val PIN_LENGTH = 4

class LockViewModel(
    private val userPreferences: UserPreferences,
    private val appLockState: AppLockState
) : ViewModel() {

    val settings: StateFlow<UserSettings> = userPreferences.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error.asStateFlow()

    fun onDigit(digit: Char) {
        if (_pin.value.length >= PIN_LENGTH) return
        _error.value = false
        _pin.value += digit
        if (_pin.value.length == PIN_LENGTH) verify()
    }

    fun onBackspace() {
        if (_pin.value.isNotEmpty()) {
            _error.value = false
            _pin.value = _pin.value.dropLast(1)
        }
    }

    fun unlockWithBiometric() {
        appLockState.unlock()
    }

    private fun verify() {
        viewModelScope.launch {
            val current = userPreferences.settings.first()
            val hash = current.pinHash
            val salt = current.pinSalt
            val matches = hash != null && salt != null && PinHasher.matches(_pin.value, salt, hash)
            if (matches) {
                appLockState.unlock()
            } else {
                _error.value = true
                delay(350)
                _pin.value = ""
            }
        }
    }
}
