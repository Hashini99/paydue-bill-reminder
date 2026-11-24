package com.hrdevzone.paydue.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdevzone.paydue.data.backup.BackupManager
import com.hrdevzone.paydue.data.repository.DueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BackupMessage {
    data class Success(val text: String) : BackupMessage()
    data class Error(val text: String) : BackupMessage()
}

data class BackupUiState(
    val dueCount: Int = 0,
    val isBusy: Boolean = false,
    val message: BackupMessage? = null
)

class BackupViewModel(
    private val dueRepository: DueRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    init {
        refreshCount()
    }

    private fun refreshCount() {
        viewModelScope.launch {
            _state.value = _state.value.copy(dueCount = dueRepository.count())
        }
    }

    fun exportJson(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            runCatching {
                val dues = dueRepository.getAllOnce()
                backupManager.exportJson(uri, dues)
            }.onSuccess {
                _state.value = _state.value.copy(
                    isBusy = false,
                    message = BackupMessage.Success("Backup saved successfully.")
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isBusy = false,
                    message = BackupMessage.Error(error.message ?: "Could not save backup.")
                )
            }
        }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            runCatching {
                val dues = dueRepository.getAllOnce()
                backupManager.exportCsv(uri, dues)
            }.onSuccess {
                _state.value = _state.value.copy(
                    isBusy = false,
                    message = BackupMessage.Success("CSV exported successfully.")
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isBusy = false,
                    message = BackupMessage.Error(error.message ?: "Could not export CSV.")
                )
            }
        }
    }

    fun importJson(uri: Uri, replaceExisting: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true)
            runCatching {
                val imported = backupManager.importJson(uri)
                if (replaceExisting) {
                    dueRepository.replaceAll(imported)
                } else {
                    imported.forEach { dueRepository.save(it) }
                }
                imported.size
            }.onSuccess { count ->
                refreshCount()
                _state.value = _state.value.copy(
                    isBusy = false,
                    message = BackupMessage.Success("Imported $count dues.")
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isBusy = false,
                    message = BackupMessage.Error(error.message ?: "Could not read this file.")
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
