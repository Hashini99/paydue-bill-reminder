package com.hrdevzone.paydue.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrdevzone.paydue.ui.LocalAppContainer
import com.hrdevzone.paydue.ui.SimpleViewModelFactory
import com.hrdevzone.paydue.util.canUseBiometric
import com.hrdevzone.paydue.util.showBiometricPrompt

@Composable
fun LockScreen() {
    val container = LocalAppContainer.current
    val viewModel: LockViewModel = viewModel(
        factory = SimpleViewModelFactory { LockViewModel(container.userPreferences, container.appLockState) }
    )
    val settings by viewModel.settings.collectAsState()
    val pin by viewModel.pin.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricAvailable = activity != null && canUseBiometric(context) && settings.biometricEnabled

    LaunchedEffect(biometricAvailable) {
        activity?.takeIf { biometricAvailable }?.let {
            showBiometricPrompt(it, onSuccess = { viewModel.unlockWithBiometric() })
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("PayDue is locked", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Enter your PIN to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(4) { index ->
                    val filled = index < pin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    error -> MaterialTheme.colorScheme.error
                                    filled -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "back")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(keys) { key ->
                    when {
                        key == "back" -> KeypadButton(onClick = viewModel::onBackspace) {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
                        }
                        key.isEmpty() -> Spacer(Modifier.size(64.dp))
                        else -> KeypadButton(onClick = { viewModel.onDigit(key.first()) }) {
                            Text(key, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }

            if (activity != null && canUseBiometric(context) && settings.biometricEnabled) {
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = {
                    showBiometricPrompt(activity, onSuccess = { viewModel.unlockWithBiometric() })
                }) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = null)
                    Text(" Use fingerprint", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
