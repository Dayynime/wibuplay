package com.dayynime.wibuplay.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentViolet,
    onPrimary = TextWhite,
    primaryContainer = AccentVioletDark,
    onPrimaryContainer = TextWhite,
    secondary = AccentVioletLight,
    onSecondary = TextWhite,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = TextSecondary,
    tertiary = AccentVioletLight,
    onTertiary = TextWhite,
    background = BackgroundDark,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceElevated,
    outlineVariant = SurfaceVariantDark,
    error = ErrorRed,
    onError = TextWhite
)

@Composable
fun WibuplayTheme(
    darkTheme: Boolean = true, // Wibuplay is designed as a dark theme experience
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BackgroundDark.toArgb()
                window.navigationBarColor = BackgroundDark.toArgb()
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                windowInsetsController.isAppearanceLightStatusBars = false
                windowInsetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
