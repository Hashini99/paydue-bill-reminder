package com.hrdevzone.paydue.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdevzone.paydue.data.local.Due
import com.hrdevzone.paydue.data.model.DueDirection
import com.hrdevzone.paydue.data.prefs.UserPreferences
import com.hrdevzone.paydue.data.repository.DueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class HomeFilter(val label: String) {
    ALL("All"),
    THIS_WEEK("This week"),
    OVERDUE("Overdue"),
    PAID("Paid")
}

data class HomeUiState(
    val allDues: List<Due> = emptyList(),
    val filteredDues: List<Due> = emptyList(),
    val filter: HomeFilter = HomeFilter.ALL,
    val currencySymbol: String = "₹",
    val payableTotal: Double = 0.0,
    val receivableTotal: Double = 0.0,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val dueRepository: DueRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val filter = MutableStateFlow(HomeFilter.ALL)

    val uiState: StateFlow<HomeUiState> = combine(
        dueRepository.observeAll(),
        userPreferences.settings,
        filter
    ) { dues, settings, currentFilter ->
        val today = LocalDate.now()
        val unpaid = dues.filter { !it.isPaid }
        val payableTotal = unpaid.filter { it.direction == DueDirection.PAYABLE }.sumOf { it.amount }
        val receivableTotal = unpaid.filter { it.direction == DueDirection.RECEIVABLE }.sumOf { it.amount }

        val filtered = when (currentFilter) {
            HomeFilter.ALL -> unpaid.sortedBy { it.dueDateEpochDay }
            HomeFilter.THIS_WEEK -> unpaid.filter {
                val date = LocalDate.ofEpochDay(it.dueDateEpochDay)
                !date.isBefore(today) && date.isBefore(today.plusDays(7))
            }.sortedBy { it.dueDateEpochDay }
            HomeFilter.OVERDUE -> unpaid.filter {
                LocalDate.ofEpochDay(it.dueDateEpochDay).isBefore(today)
            }.sortedBy { it.dueDateEpochDay }
            HomeFilter.PAID -> dues.filter { due ->
                due.isPaid || (due.lastPaidEpochDay != null && isWithinCurrentMonth(due.lastPaidEpochDay))
            }.sortedByDescending { it.lastPaidEpochDay ?: 0L }
        }

        HomeUiState(
            allDues = dues,
            filteredDues = filtered,
            filter = currentFilter,
            currencySymbol = settings.currencySymbol,
            payableTotal = payableTotal,
            receivableTotal = receivableTotal,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun setFilter(newFilter: HomeFilter) {
        filter.value = newFilter
    }

    fun togglePaid(due: Due) {
        viewModelScope.launch {
            if (due.isPaid) dueRepository.markUnpaid(due) else dueRepository.markPaid(due)
        }
    }

    fun deleteDue(due: Due) {
        viewModelScope.launch { dueRepository.delete(due) }
    }

    private fun isWithinCurrentMonth(epochDay: Long): Boolean {
        val date = LocalDate.ofEpochDay(epochDay)
        val now = LocalDate.now()
        return date.year == now.year && date.month == now.month
    }
}
