package com.hrdevzone.paydue.ui.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdevzone.paydue.data.local.Due
import com.hrdevzone.paydue.data.model.DueCategory
import com.hrdevzone.paydue.data.model.DueDirection
import com.hrdevzone.paydue.data.model.RepeatType
import com.hrdevzone.paydue.data.repository.DueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddEditDueState(
    val id: Long = 0L,
    val title: String = "",
    val amountText: String = "",
    val dueDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val category: DueCategory = DueCategory.OTHER,
    val notes: String = "",
    val repeatType: RepeatType = RepeatType.ONCE,
    val direction: DueDirection = DueDirection.PAYABLE,
    val personName: String = "",
    val reminderEnabled: Boolean = true,
    val isPaid: Boolean = false,
    val lastPaidEpochDay: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val titleError: String? = null,
    val amountError: String? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false
)

class AddEditDueViewModel(
    private val dueRepository: DueRepository,
    private val dueId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditDueState(isLoading = dueId != null))
    val state: StateFlow<AddEditDueState> = _state.asStateFlow()

    init {
        if (dueId != null) {
            viewModelScope.launch {
                val due = dueRepository.getById(dueId)
                if (due != null) {
                    _state.value = due.toFormState()
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }
    }

    fun updateTitle(value: String) {
        _state.value = _state.value.copy(title = value, titleError = null)
    }

    fun updateAmount(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _state.value = _state.value.copy(amountText = value, amountError = null)
        }
    }

    fun updateDueDate(epochDay: Long) {
        _state.value = _state.value.copy(dueDateEpochDay = epochDay)
    }

    fun updateCategory(category: DueCategory) {
        _state.value = _state.value.copy(category = category)
    }

    fun updateNotes(value: String) {
        _state.value = _state.value.copy(notes = value)
    }

    fun updateRepeatType(repeatType: RepeatType) {
        _state.value = _state.value.copy(repeatType = repeatType)
    }

    fun updateDirection(direction: DueDirection) {
        _state.value = _state.value.copy(direction = direction)
    }

    fun updatePersonName(value: String) {
        _state.value = _state.value.copy(personName = value)
    }

    fun updateReminderEnabled(value: Boolean) {
        _state.value = _state.value.copy(reminderEnabled = value)
    }

    fun save() {
        val current = _state.value
        val amount = current.amountText.toDoubleOrNull()
        var hasError = false
        var titleError: String? = null
        var amountError: String? = null

        if (current.title.isBlank()) {
            titleError = "Give this due a name"
            hasError = true
        }
        if (amount == null || amount <= 0.0) {
            amountError = "Enter a valid amount"
            hasError = true
        }
        if (hasError) {
            _state.value = current.copy(titleError = titleError, amountError = amountError)
            return
        }

        viewModelScope.launch {
            val due = Due(
                id = current.id,
                title = current.title.trim(),
                amount = amount!!,
                dueDateEpochDay = current.dueDateEpochDay,
                category = current.category,
                notes = current.notes.trim(),
                repeatType = current.repeatType,
                direction = current.direction,
                personName = current.personName.trim(),
                isPaid = current.isPaid,
                lastPaidEpochDay = current.lastPaidEpochDay,
                reminderEnabled = current.reminderEnabled,
                createdAt = current.createdAt
            )
            dueRepository.save(due)
            _state.value = _state.value.copy(saved = true)
        }
    }

    fun delete() {
        val current = _state.value
        if (!current.isEditing) return
        viewModelScope.launch {
            val due = dueRepository.getById(current.id) ?: return@launch
            dueRepository.delete(due)
            _state.value = _state.value.copy(deleted = true)
        }
    }

    private fun Due.toFormState() = AddEditDueState(
        id = id,
        title = title,
        amountText = if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString(),
        dueDateEpochDay = dueDateEpochDay,
        category = category,
        notes = notes,
        repeatType = repeatType,
        direction = direction,
        personName = personName,
        reminderEnabled = reminderEnabled,
        isPaid = isPaid,
        lastPaidEpochDay = lastPaidEpochDay,
        createdAt = createdAt,
        isEditing = true,
        isLoading = false
    )
}
