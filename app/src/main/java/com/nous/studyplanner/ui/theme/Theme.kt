package com.nous.studyplanner.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SystemBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F0FF),
    onPrimaryContainer = SystemBlue,
    secondary = SystemGray,
    onSecondary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF9F9FB),
    onSurfaceVariant = TextSecondary,
    outline = Separator,
    outlineVariant = SystemGray5,
    error = SystemRed,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004499),
    onPrimaryContainer = Color(0xFFB3D7FF),
    secondary = Color(0xFF98989D),
    onSecondary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkSeparator,
    outlineVariant = Color(0xFF48484A),
    error = Color(0xFFFF453A),
)

@Composable
fun StudyPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
