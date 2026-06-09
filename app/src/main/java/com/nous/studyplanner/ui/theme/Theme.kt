package com.nous.studyplanner.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Accent color presets ──
private val accentMap = mapOf(
    "blue" to Color(0xFF007AFF),
    "green" to Color(0xFF34C759),
    "orange" to Color(0xFFFF9500),
    "red" to Color(0xFFFF3B30),
    "purple" to Color(0xFF9B59B6),
    "pink" to Color(0xFFFF6B6B),
)

private val lightBase = lightColorScheme(
    primary = SystemBlue, onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F0FF), onPrimaryContainer = SystemBlue,
    secondary = SystemGray, onSecondary = Color.White,
    background = Background, onBackground = TextPrimary,
    surface = Surface, onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF9F9FB), onSurfaceVariant = TextSecondary,
    outline = Separator, outlineVariant = SystemGray5,
    error = SystemRed,
)

private val darkBase = darkColorScheme(
    primary = Color(0xFF0A84FF), onPrimary = Color.White,
    primaryContainer = Color(0xFF004499), onPrimaryContainer = Color(0xFFB3D7FF),
    secondary = Color(0xFF98989D), onSecondary = Color.Black,
    background = DarkBackground, onBackground = DarkTextPrimary,
    surface = DarkSurface, onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface2, onSurfaceVariant = DarkTextSecondary,
    outline = DarkSeparator, outlineVariant = Color(0xFF48484A),
    error = Color(0xFFFF453A),
)

@Composable
fun StudyPlannerTheme(content: @Composable () -> Unit) {
    // Re-read prefs when ThemeRefresh changes
    ThemeRefresh.state.value // triggers recomposition
    val ctx = LocalContext.current
    val prefs = ctx.getSharedPreferences("alex_settings", 0)

    val themeMode = prefs.getString("theme", "system") ?: "system"
    val accentKey = prefs.getString("accent", "blue") ?: "blue"
    val accent = accentMap[accentKey] ?: SystemBlue

    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }

    val colorScheme = if (darkTheme) {
        darkBase.copy(primary = accent)
    } else {
        lightBase.copy(primary = accent)
    }

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
