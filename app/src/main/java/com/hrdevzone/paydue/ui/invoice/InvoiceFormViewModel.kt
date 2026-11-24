package com.hrdevzone.paydue.ui.invoice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdevzone.paydue.data.local.InvoiceRecord
import com.hrdevzone.paydue.data.pdf.InvoicePdfGenerator
import com.hrdevzone.paydue.data.prefs.UserPreferences
import com.hrdevzone.paydue.data.repository.DueRepository
import com.hrdevzone.paydue.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class InvoiceFormState(
    val fromName: String = "",
    val clientName: String = "",
    val amountText: String = "",
    val issueDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val paymentNote: String = "",
    val note: String = "",
    val linkedDueId: Long? = null,
    val currencySymbol: String = "₹",
    val clientNameError: String? = null,
    val amountError: String? = null,
    val isSaving: Boolean = false,
    val createdInvoiceId: Long? = null
)

class InvoiceFormViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val dueRepository: DueRepository,
    private val userPreferences: UserPreferences,
    private val prefillDueId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceFormState())
    val state: StateFlow<InvoiceFormState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = userPreferences.settings.first()
            _state.value = _state.value.copy(
                fromName = settings.businessName,
                paymentNote = settings.defaultPaymentNote,
                currencySymbol = settings.currencySymbol
            )
            if (prefillDueId != null) {
                dueRepository.getById(prefillDueId)?.let { due ->
                    _state.value = _state.value.copy(
                        clientName = due.personName,
                        amountText = if (due.amount == due.amount.toLong().toDouble())
                            due.amount.toLong().toString() else due.amount.toString(),
                        note = due.title,
                        linkedDueId = due.id
                    )
                }
            }
        }
    }

    fun updateFromName(value: String) {
        _state.value = _state.value.copy(fromName = value)
    }

    fun updateClientName(value: String) {
        _state.value = _state.value.copy(clientName = value, clientNameError = null)
    }

    fun updateAmount(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _state.value = _state.value.copy(amountText = value, amountError = null)
        }
    }

    fun updateIssueDate(epochDay: Long) {
        _state.value = _state.value.copy(issueDateEpochDay = epochDay)
    }

    fun updatePaymentNote(value: String) {
        _state.value = _state.value.copy(paymentNote = value)
    }

    fun updateNote(value: String) {
        _state.value = _state.value.copy(note = value)
    }

    fun generate(context: Context) {
        val current = _state.value
        val amount = current.amountText.toDoubleOrNull()
        var hasError = false
        var clientError: String? = null
        var amountError: String? = null

        if (current.clientName.isBlank()) {
            clientError = "Who is this invoice for?"
            hasError = true
        }
        if (amount == null || amount <= 0.0) {
            amountError = "Enter a valid amount"
            hasError = true
        }
        if (hasError) {
            _state.value = current.copy(clientNameError = clientError, amountError = amountError)
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true)
            val invoiceNumber = invoiceRepository.nextInvoiceNumber()
            val fileName = "invoice-${invoiceNumber.replace("/", "-")}.pdf"
            val draft = InvoiceRecord(
                invoiceNumber = invoiceNumber,
                fromName = current.fromName.trim(),
                clientName = current.clientName.trim(),
                amount = amount!!,
                issueDateEpochDay = current.issueDateEpochDay,
                paymentNote = current.paymentNote.trim(),
                note = current.note.trim(),
                linkedDueId = current.linkedDueId,
                pdfFileName = fileName
            )
            InvoicePdfGenerator.generate(context, draft, current.currencySymbol)
            val id = invoiceRepository.save(draft)
            _state.value = _state.value.copy(isSaving = false, createdInvoiceId = id)
        }
    }
}
