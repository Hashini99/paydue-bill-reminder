package com.hrdevzone.paydue.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NavyPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = NavyPrimaryDark,
    onPrimaryContainer = Color(0xFFCBD9EC),
    secondary = TealAccentLight,
    onSecondary = Color(0xFF00201A),
    secondaryContainer = Color(0xFF0F3B33),
    onSecondaryContainer = Color(0xFFB6F0E1),
    tertiary = StatusUpcomingDark,
    background = DarkBackground,
    onBackground = Color(0xFFE3E8F0),
    surface = DarkSurface,
    onSurface = Color(0xFFE3E8F0),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = StatusOverdueDark,
    onError = Color(0xFF3A0A0A),
    errorContainer = StatusOverdueContainerDark,
    onErrorContainer = Color(0xFFFFDAD8),
    outline = Color(0xFF3A4557)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE6F2),
    onPrimaryContainer = NavyPrimaryDark,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFF0E6),
    onSecondaryContainer = Color(0xFF033227),
    tertiary = StatusUpcomingLight,
    background = LightBackground,
    onBackground = Color(0xFF1B2430),
    surface = LightSurface,
    onSurface = Color(0xFF1B2430),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = StatusOverdueLight,
    onError = Color.White,
    errorContainer = StatusOverdueContainerLight,
    onErrorContainer = Color(0xFF410001),
    outline = Color(0xFFC3CCD8)
)

@Composable
fun PayDueAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val statusColors = if (darkTheme) PayDueDarkStatusColors else PayDueLightStatusColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalPayDueStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PayDueTypography,
            content = content
        )
    }
}
