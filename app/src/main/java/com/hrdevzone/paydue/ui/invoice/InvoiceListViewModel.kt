package com.hrdevzone.paydue.ui.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdevzone.paydue.data.local.InvoiceRecord
import com.hrdevzone.paydue.data.prefs.UserPreferences
import com.hrdevzone.paydue.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InvoiceListUiState(
    val invoices: List<InvoiceRecord> = emptyList(),
    val currencySymbol: String = "₹",
    val isLoading: Boolean = true
)

class InvoiceListViewModel(
    private val invoiceRepository: InvoiceRepository,
    userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<InvoiceListUiState> = combine(
        invoiceRepository.observeAll(),
        userPreferences.settings
    ) { invoices, settings ->
        InvoiceListUiState(
            invoices = invoices,
            currencySymbol = settings.currencySymbol,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InvoiceListUiState())

    fun delete(invoice: InvoiceRecord) {
        viewModelScope.launch { invoiceRepository.delete(invoice) }
    }
}
