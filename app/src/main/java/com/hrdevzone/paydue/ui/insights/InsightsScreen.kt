package com.hrdevzone.paydue.ui.insights

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrdevzone.paydue.ui.LocalAppContainer
import com.hrdevzone.paydue.ui.SimpleViewModelFactory
import com.hrdevzone.paydue.ui.components.DonutChart
import com.hrdevzone.paydue.ui.components.DonutLegendRow
import com.hrdevzone.paydue.ui.components.DonutSlice
import com.hrdevzone.paydue.ui.components.EmptyState
import com.hrdevzone.paydue.ui.components.MonthlyBarChart
import com.hrdevzone.paydue.ui.theme.PayDueTheme
import com.hrdevzone.paydue.ui.theme.categoryColor
import com.hrdevzone.paydue.util.formatAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen() {
    val container = LocalAppContainer.current
    val viewModel: InsightsViewModel = viewModel(
        factory = SimpleViewModelFactory {
            InsightsViewModel(
                container.dueRepository,
                container.paymentEventRepository,
                container.userPreferences
            )
        }
    )
    val state by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val statusColors = PayDueTheme.statusColors

    Scaffold(
        topBar = { TopAppBar(title = { Text("Insights") }) }
    ) { padding ->
        if (!state.hasAnyData && !state.isLoading) {
            EmptyState(
                icon = Icons.Filled.PieChart,
                title = "Nothing to show yet",
                message = "Add a few dues and mark some paid — your spending picture will show up here.",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Paid this month",
                        value = formatAmount(state.paidThisMonth, state.currencySymbol),
                        color = statusColors.paid,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending",
                        value = formatAmount(state.pendingPayable, state.currencySymbol),
                        color = statusColors.overdue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (state.categoryBreakdown.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Where your money goes", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Unpaid bills by category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                            )
                            val slices = state.categoryBreakdown.map {
                                DonutSlice(it.label, it.amount, categoryColor(it.category, isDark))
                            }
                            val total = slices.sumOf { it.value }
                            DonutChart(
                                slices = slices,
                                centerLabel = "Unpaid",
                                centerValue = formatAmount(total, state.currencySymbol),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 48.dp)
                            )
                            Column(
                                modifier = Modifier.padding(top = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                slices.forEach { slice ->
                                    val percent = if (total > 0) (slice.value / total * 100) else 0.0
                                    DonutLegendRow(
                                        slice = slice,
                                        percentText = "${percent.toInt()}%",
                                        valueText = formatAmount(slice.value, state.currencySymbol)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Last 6 months", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Paid vs received",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                        )
                        MonthlyBarChart(
                            bars = state.monthlyBars,
                            payableColor = MaterialTheme.colorScheme.primary,
                            receivableColor = statusColors.receivable
                        )
                        Row(
                            modifier = Modifier.padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            LegendDot(label = "You paid", color = MaterialTheme.colorScheme.primary)
                            LegendDot(label = "You received", color = statusColors.receivable)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun LegendDot(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}
