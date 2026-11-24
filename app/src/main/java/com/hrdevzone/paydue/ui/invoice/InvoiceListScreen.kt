package com.hrdevzone.paydue.ui.invoice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrdevzone.paydue.ui.LocalAppContainer
import com.hrdevzone.paydue.ui.SimpleViewModelFactory
import com.hrdevzone.paydue.ui.components.EmptyState
import com.hrdevzone.paydue.util.formatAmount
import com.hrdevzone.paydue.util.formatDate
import com.hrdevzone.paydue.util.shareInvoicePdf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    onCreateInvoice: () -> Unit,
    onOpenInvoice: (Long) -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: InvoiceListViewModel = viewModel(
        factory = SimpleViewModelFactory {
            InvoiceListViewModel(container.invoiceRepository, container.userPreferences)
        }
    )
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Invoices") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateInvoice,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New invoice") }
            )
        }
    ) { padding ->
        if (state.invoices.isEmpty() && !state.isLoading) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = "No invoices yet",
                message = "Create a clean, shareable invoice PDF in seconds — fully offline.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.invoices, key = { it.id }) { invoice ->
                    Card(
                        onClick = { onOpenInvoice(invoice.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = invoice.clientName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${invoice.invoiceNumber} · ${formatDate(invoice.issueDateEpochDay)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatAmount(invoice.amount, state.currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { shareInvoicePdf(context, invoice) }) {
                                Icon(Icons.Filled.IosShare, contentDescription = "Share invoice")
                            }
                        }
                    }
                }
            }
        }
    }
}
