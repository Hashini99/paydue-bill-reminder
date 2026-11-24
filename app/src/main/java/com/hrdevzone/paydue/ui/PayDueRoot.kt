package com.hrdevzone.paydue.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hrdevzone.paydue.data.prefs.ThemeMode
import com.hrdevzone.paydue.di.AppContainer
import com.hrdevzone.paydue.ui.lock.LockScreen
import com.hrdevzone.paydue.ui.navigation.PayDueNavHost
import com.hrdevzone.paydue.ui.theme.PayDueAppTheme

@Composable
fun PayDueRoot(container: AppContainer) {
    val settings by container.userPreferences.settings.collectAsState(initial = null)

    val darkTheme = when (settings?.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM, null -> isSystemInDarkTheme()
    }

    val unlocked by container.appLockState.unlocked.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lockEnabled = settings?.appLockEnabled == true

    DisposableEffect(lifecycleOwner, lockEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && lockEnabled) {
                container.appLockState.lock()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CompositionLocalProvider(LocalAppContainer provides container) {
        PayDueAppTheme(darkTheme = darkTheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (lockEnabled && !unlocked) {
                    LockScreen()
                } else {
                    PayDueNavHost()
                }
            }
        }
    }
}
