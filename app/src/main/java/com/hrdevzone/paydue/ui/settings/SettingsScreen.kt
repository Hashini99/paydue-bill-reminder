package com.hrdevzone.paydue.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrdevzone.paydue.data.prefs.ThemeMode
import com.hrdevzone.paydue.ui.LocalAppContainer
import com.hrdevzone.paydue.ui.SimpleViewModelFactory
import com.hrdevzone.paydue.util.canUseBiometric
import com.hrdevzone.paydue.util.currencyOptionForCode
import com.hrdevzone.paydue.util.currencyOptions

private const val FREE_DUE_LIMIT = 15

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenBackup: () -> Unit,
    onOpenPinSetup: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SimpleViewModelFactory {
            SettingsViewModel(container.userPreferences, container.dueRepository)
        }
    )
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val biometricSupported = canUseBiometric(context)

    var businessName by remember(state.settings.businessName) {
        mutableStateOf(state.settings.businessName)
    }
    var paymentNote by remember(state.settings.defaultPaymentNote) {
        mutableStateOf(state.settings.defaultPaymentNote)
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var showProInfo by remember { mutableStateOf(false) }
    var currencyMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Workspaces,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (state.settings.isPro) "PayDue Pro" else "Free plan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Text(
                            text = if (state.settings.isPro) {
                                "Unlimited dues and invoice PDFs — thanks for supporting PayDue."
                            } else {
                                "${state.dueCount}/$FREE_DUE_LIMIT dues used on the free plan."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        if (!state.settings.isPro) {
                            TextButton(
                                onClick = { showProInfo = true },
                                modifier = Modifier.padding(top = 4.dp)
                            ) { Text("Upgrade to Pro") }
                        }
                    }
                }
            }

            item {
                SettingsSectionCard(title = "Invoice details") {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = {
                            businessName = it
                            viewModel.updateBusinessName(it)
                        },
                        label = { Text("Your name / business") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = paymentNote,
                        onValueChange = {
                            paymentNote = it
                            viewModel.updateDefaultPaymentNote(it)
                        },
                        label = { Text("Default payment details") },
                        placeholder = { Text("UPI ID or bank note") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        minLines = 2
                    )
                }
            }

            item {
                SettingsSectionCard(title = "Preferences") {
                    val selectedCurrency = currencyOptionForCode(state.settings.currencyCode)
                    ExposedDropdownMenuBox(
                        expanded = currencyMenuExpanded,
                        onExpandedChange = { currencyMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedCurrency.symbol}  ${selectedCurrency.displayName} (${selectedCurrency.code})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Currency") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyMenuExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = currencyMenuExpanded,
                            onDismissRequest = { currencyMenuExpanded = false }
                        ) {
                            currencyOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("${option.symbol}  ${option.displayName} (${option.code})") },
                                    onClick = {
                                        viewModel.updateCurrency(option)
                                        currencyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    SettingsRow(
                        icon = Icons.Filled.AccessTime,
                        title = "Reminder time",
                        subtitle = "%02d:%02d".format(
                            state.settings.reminderHour,
                            state.settings.reminderMinute
                        ),
                        onClick = { showTimePicker = true }
                    )
                }
            }

            item {
                SettingsSectionCard(title = "Appearance") {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(
                            ThemeMode.SYSTEM to "System",
                            ThemeMode.LIGHT to "Light",
                            ThemeMode.DARK to "Dark"
                        )
                        options.forEachIndexed { index, (mode, label) ->
                            SegmentedButton(
                                selected = state.settings.themeMode == mode,
                                onClick = { viewModel.updateThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            item {
                SettingsSectionCard(title = "App lock") {
                    if (state.settings.pinHash == null) {
                        SettingsRow(
                            icon = Icons.Filled.Password,
                            title = "Set up a PIN",
                            subtitle = "Require a PIN (and optionally fingerprint) to open PayDue",
                            onClick = onOpenPinSetup
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Require PIN to open", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    "Lock PayDue whenever you leave the app",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.settings.appLockEnabled,
                                onCheckedChange = { viewModel.setAppLockEnabled(it) }
                            )
                        }

                        if (biometricSupported) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Use fingerprint / face", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text(
                                        "Faster unlock — PIN still works as backup",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = state.settings.biometricEnabled,
                                    enabled = state.settings.appLockEnabled,
                                    onCheckedChange = { viewModel.setBiometricEnabled(it) }
                                )
                            }
                        }

                        Row(modifier = Modifier.padding(top = 6.dp)) {
                            TextButton(onClick = onOpenPinSetup) { Text("Change PIN") }
                            TextButton(onClick = { viewModel.removePin() }) { Text("Remove PIN") }
                        }
                    }
                }
            }

            item {
                SettingsSectionCard(title = "Your data") {
                    SettingsRow(
                        icon = Icons.Filled.Backup,
                        title = "Backup & restore",
                        subtitle = "Export or import your dues as a file",
                        onClick = onOpenBackup
                    )
                    SettingsRow(
                        icon = Icons.Filled.Lock,
                        title = "Privacy",
                        subtitle = "Everything stays on this device. No account, no cloud, no internet required.",
                        onClick = null
                    )
                }
            }

            item {
                Text(
                    text = "PayDue Offline · v1.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.settings.reminderHour,
            initialMinute = state.settings.reminderMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateReminderTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    if (showProInfo) {
        AlertDialog(
            onDismissRequest = { showProInfo = false },
            confirmButton = {
                TextButton(onClick = { showProInfo = false }) { Text("Got it") }
            },
            title = { Text("PayDue Pro") },
            text = {
                Text(
                    "Pro unlocks unlimited dues, watermark-free invoices, and full backups. " +
                        "In-app purchase via Google Play Billing isn't wired into this build yet — " +
                        "connect a Play Billing flow here when you're ready to publish."
                )
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?
) {
    var rowModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp) as Modifier
    if (onClick != null) {
        rowModifier = rowModifier.clickable(onClick = onClick)
    }
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
