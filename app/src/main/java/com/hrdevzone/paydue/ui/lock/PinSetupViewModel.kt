package com.hrdevzone.paydue.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdevzone.paydue.data.prefs.UserPreferences
import com.hrdevzone.paydue.util.PinHasher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PIN_LENGTH = 4

enum class PinSetupStep { CREATE, CONFIRM }

data class PinSetupUiState(
    val step: PinSetupStep = PinSetupStep.CREATE,
    val firstPin: String = "",
    val currentInput: String = "",
    val error: Boolean = false,
    val completed: Boolean = false
)

class PinSetupViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(PinSetupUiState())
    val state: StateFlow<PinSetupUiState> = _state.asStateFlow()

    fun onDigit(digit: Char) {
        val current = _state.value
        if (current.currentInput.length >= PIN_LENGTH) return
        val updatedInput = current.currentInput + digit
        _state.value = current.copy(currentInput = updatedInput, error = false)
        if (updatedInput.length == PIN_LENGTH) {
            when (current.step) {
                PinSetupStep.CREATE -> {
                    _state.value = _state.value.copy(
                        step = PinSetupStep.CONFIRM,
                        firstPin = updatedInput,
                        currentInput = ""
                    )
                }
                PinSetupStep.CONFIRM -> {
                    if (updatedInput == current.firstPin) {
                        savePin(updatedInput)
                    } else {
                        viewModelScope.launch {
                            _state.value = _state.value.copy(error = true)
                            delay(400)
                            _state.value = _state.value.copy(
                                step = PinSetupStep.CREATE,
                                firstPin = "",
                                currentInput = "",
                                error = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun onBackspace() {
        val current = _state.value
        if (current.currentInput.isNotEmpty()) {
            _state.value = current.copy(currentInput = current.currentInput.dropLast(1), error = false)
        }
    }

    private fun savePin(pin: String) {
        viewModelScope.launch {
            val salt = PinHasher.randomSalt()
            val hash = PinHasher.hash(pin, salt)
            userPreferences.setPin(hash, salt)
            _state.value = _state.value.copy(completed = true)
        }
    }
}
