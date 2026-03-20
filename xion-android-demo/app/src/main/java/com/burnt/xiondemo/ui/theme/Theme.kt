package com.burnt.xiondemo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = XionGreen,
    onPrimary = XionBlack,
    primaryContainer = XionGreenDark,
    onPrimaryContainer = XionWhite,
    secondary = XionBlue,
    onSecondary = XionBlack,
    background = SurfaceDark,
    onBackground = XionWhite,
    surface = SurfaceDark,
    onSurface = XionWhite,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = XionGray300,
    error = XionRed,
    onError = XionWhite,
    outline = XionGray600,
)

private val LightColorScheme = lightColorScheme(
    primary = XionGreenDark,
    onPrimary = XionWhite,
    primaryContainer = XionGreen,
    onPrimaryContainer = XionBlack,
    secondary = XionBlue,
    onSecondary = XionWhite,
    background = SurfaceLight,
    onBackground = XionBlack,
    surface = SurfaceLight,
    onSurface = XionBlack,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = XionGray600,
    error = XionRed,
    onError = XionWhite,
    outline = XionGray300,
)

@Composable
fun XionDemoTheme(
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
