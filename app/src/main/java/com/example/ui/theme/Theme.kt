package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.core.view.WindowCompat

class ThemeController(initialDark: Boolean = false) {
    var isDark by mutableStateOf(initialDark)
        private set

    fun toggleTheme() {
        isDark = !isDark
    }

    fun setTheme(dark: Boolean) {
        isDark = dark
    }
}

val LocalThemeController = compositionLocalOf { ThemeController(false) }

private val DarkColorScheme = darkColorScheme(
    primary = CyanSpark,
    onPrimary = DarkBackground,
    primaryContainer = ElectricIndigoDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary = VioletGlow,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = RoseSpark,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkSurfaceElevated
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = LightSurface,
    primaryContainer = LightSurfaceElevated,
    onPrimaryContainer = ElectricIndigoDark,
    secondary = VioletGlow,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    tertiary = RoseSpark,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightSurfaceElevated
)

@Composable
fun NovaGeminiTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val themeController = remember { ThemeController(darkTheme) }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (themeController.isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeController.isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !themeController.isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !themeController.isDark
        }
    }

    CompositionLocalProvider(
        LocalThemeController provides themeController
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.bodyLarge) {
                content()
            }
        }
    }
}
