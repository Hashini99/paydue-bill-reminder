package com.hrdevzone.paydue.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrdevzone.paydue.data.model.DueCategory
import com.hrdevzone.paydue.data.model.DueDirection
import com.hrdevzone.paydue.data.prefs.UserPreferences
import com.hrdevzone.paydue.data.repository.DueRepository
import com.hrdevzone.paydue.data.repository.PaymentEventRepository
import com.hrdevzone.paydue.ui.components.MonthBar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TREND_MONTHS = 6

data class CategoryBreakdownItem(
    val category: DueCategory,
    val label: String,
    val amount: Double
)

data class InsightsUiState(
    val currencySymbol: String = "₹",
    val pendingPayable: Double = 0.0,
    val pendingReceivable: Double = 0.0,
    val paidThisMonth: Double = 0.0,
    val categoryBreakdown: List<CategoryBreakdownItem> = emptyList(),
    val monthlyBars: List<MonthBar> = emptyList(),
    val hasAnyData: Boolean = false,
    val isLoading: Boolean = true
)

class InsightsViewModel(
    dueRepository: DueRepository,
    paymentEventRepository: PaymentEventRepository,
    userPreferences: UserPreferences
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = combine(
        dueRepository.observeAll(),
        paymentEventRepository.observeLastMonths(TREND_MONTHS),
        userPreferences.settings
    ) { dues, events, settings ->
        val unpaid = dues.filter { !it.isPaid }
        val pendingPayable = unpaid.filter { it.direction == DueDirection.PAYABLE }.sumOf { it.amount }
        val pendingReceivable = unpaid.filter { it.direction == DueDirection.RECEIVABLE }.sumOf { it.amount }

        val now = LocalDate.now()
        val paidThisMonth = events.filter {
            val d = LocalDate.ofEpochDay(it.paidEpochDay)
            d.year == now.year && d.month == now.month && it.direction == DueDirection.PAYABLE
        }.sumOf { it.amount }

        val categoryTotals = unpaid
            .filter { it.direction == DueDirection.PAYABLE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .filterValues { it > 0.0 }
            .toList()
            .sortedByDescending { it.second }

        val categoryBreakdown = categoryTotals.map { (category, amount) ->
            CategoryBreakdownItem(category = category, label = category.label, amount = amount)
        }

        val monthFormatter = DateTimeFormatter.ofPattern("MMM")
        val monthlyBars = (0 until TREND_MONTHS).map { offset ->
            val month = now.minusMonths((TREND_MONTHS - 1 - offset).toLong())
            val payableTotal = events.filter {
                val d = LocalDate.ofEpochDay(it.paidEpochDay)
                d.year == month.year && d.month == month.month && it.direction == DueDirection.PAYABLE
            }.sumOf { it.amount }
            val receivableTotal = events.filter {
                val d = LocalDate.ofEpochDay(it.paidEpochDay)
                d.year == month.year && d.month == month.month && it.direction == DueDirection.RECEIVABLE
            }.sumOf { it.amount }
            MonthBar(month.format(monthFormatter), payableTotal, receivableTotal)
        }

        InsightsUiState(
            currencySymbol = settings.currencySymbol,
            pendingPayable = pendingPayable,
            pendingReceivable = pendingReceivable,
            paidThisMonth = paidThisMonth,
            categoryBreakdown = categoryBreakdown,
            monthlyBars = monthlyBars,
            hasAnyData = dues.isNotEmpty() || events.isNotEmpty(),
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsUiState())
}
