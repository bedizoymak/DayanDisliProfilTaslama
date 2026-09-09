package com.dayandisli.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DayanDarkColorScheme = darkColorScheme(
    primary = AppColors.Blue600,
    onPrimary = AppColors.White,
    secondary = AppColors.Emerald600,
    onSecondary = AppColors.White,
    background = AppColors.Slate900,
    onBackground = AppColors.White,
    surface = AppColors.Slate800,
    onSurface = AppColors.White,
    surfaceVariant = AppColors.Slate700,
    onSurfaceVariant = AppColors.Slate300,
    outline = AppColors.Slate700,
    error = AppColors.Red400,
)

@Composable
fun DayanCalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DayanDarkColorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}

/** Muted secondary text color used throughout the web calculator (text-slate-400). */
val MaterialTheme.mutedText: Color
    @Composable get() = AppColors.Slate400
