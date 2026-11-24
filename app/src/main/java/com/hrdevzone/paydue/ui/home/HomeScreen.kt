package com.hrdevzone.paydue.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrdevzone.paydue.ui.LocalAppContainer
import com.hrdevzone.paydue.ui.SimpleViewModelFactory
import com.hrdevzone.paydue.ui.components.ChipOption
import com.hrdevzone.paydue.ui.components.DueCard
import com.hrdevzone.paydue.ui.components.EmptyState
import com.hrdevzone.paydue.ui.components.SummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddDue: () -> Unit,
    onEditDue: (Long) -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(
        factory = SimpleViewModelFactory {
            HomeViewModel(container.dueRepository, container.userPreferences)
        }
    )
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (granted != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("PayDue") },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDue) {
                Icon(Icons.Filled.Add, contentDescription = "Add due")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummaryCard(
                    payableTotal = state.payableTotal,
                    receivableTotal = state.receivableTotal,
                    currencySymbol = state.currencySymbol
                )
            }

            item {
                LazyRowFilters(
                    selected = state.filter,
                    onSelect = viewModel::setFilter
                )
            }

            if (state.filteredDues.isEmpty() && !state.isLoading) {
                item {
                    EmptyState(
                        icon = Icons.Filled.EventAvailable,
                        title = emptyTitle(state.filter),
                        message = emptyMessage(state.filter)
                    )
                }
            } else {
                items(state.filteredDues, key = { it.id }) { due ->
                    DueCard(
                        due = due,
                        currencySymbol = state.currencySymbol,
                        onClick = { onEditDue(due.id) },
                        onTogglePaid = { viewModel.togglePaid(due) }
                    )
                }
            }

            item { Box(Modifier.padding(bottom = 72.dp)) }
        }
    }
}

@Composable
private fun LazyRowFilters(
    selected: HomeFilter,
    onSelect: (HomeFilter) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(HomeFilter.entries) { filter ->
            ChipOption(
                label = filter.label,
                selected = selected == filter,
                onClick = { onSelect(filter) }
            )
        }
    }
}

private fun emptyTitle(filter: HomeFilter): String = when (filter) {
    HomeFilter.ALL -> "No dues yet"
    HomeFilter.THIS_WEEK -> "Nothing due this week"
    HomeFilter.OVERDUE -> "You're all caught up"
    HomeFilter.PAID -> "No payments yet"
}

private fun emptyMessage(filter: HomeFilter): String = when (filter) {
    HomeFilter.ALL -> "Tap + to add rent, subscriptions, EMIs or money someone owes you."
    HomeFilter.THIS_WEEK -> "Enjoy the calm — nothing is due in the next 7 days."
    HomeFilter.OVERDUE -> "No overdue items right now."
    HomeFilter.PAID -> "Items you mark paid this month will show up here."
}
