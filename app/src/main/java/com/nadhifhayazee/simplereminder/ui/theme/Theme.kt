package com.nadhifhayazee.simplereminder.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Indigo400,
    onPrimary = Color.White,
    primaryContainer = Slate800,
    onPrimaryContainer = Indigo400,
    secondary = Indigo400,
    onSecondary = Color.White,
    tertiary = Rose500,
    background = Slate950,
    surface = Slate900,
    onBackground = Neutral95,
    onSurface = Neutral95,
    surfaceVariant = Slate800,
    onSurfaceVariant = Neutral90,
    outline = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Neutral95,
    onPrimaryContainer = Indigo600,
    secondary = Indigo600,
    onSecondary = Color.White,
    tertiary = Rose600,
    background = Neutral99,
    surface = Color.White,
    onBackground = Neutral10,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral20,
    outline = Neutral90
)

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

@Composable
fun SimpleReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
