package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.adhani.model.AccentPalette
import com.example.adhani.model.AppThemeMode

data class GlassConfig(
    val opacity: Float = 0.25f,
    val blurRadiusDp: Float = 24f,
    val motionSpeedMultiplier: Float = 1.0f,
    val isOled: Boolean = false
)

val LocalGlassConfig = compositionLocalOf { GlassConfig() }

@Composable
fun AdhaniTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    accentPalette: AccentPalette = AccentPalette.EMERALD,
    glassOpacity: Float = 0.25f,
    glassBlurDp: Float = 24f,
    motionSpeed: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK, AppThemeMode.OLED -> true
        AppThemeMode.DYNAMIC -> systemInDark
    }

    val isOled = themeMode == AppThemeMode.OLED

    val primaryColor = Color(accentPalette.primaryHex)
    val secondaryColor = Color(accentPalette.secondaryHex)

    val colorScheme = when {
        themeMode == AppThemeMode.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isOled -> {
            darkColorScheme(
                primary = primaryColor,
                secondary = secondaryColor,
                tertiary = GoldAccent,
                background = OledBackground,
                surface = OledSurface,
                surfaceVariant = OledSurfaceVariant,
                onPrimary = Color.Black,
                onSecondary = Color.White,
                onBackground = Color(0xFFF1F5F9),
                onSurface = Color(0xFFF8FAFC)
            )
        }
        isDark -> {
            darkColorScheme(
                primary = primaryColor,
                secondary = secondaryColor,
                tertiary = GoldAccent,
                background = DarkBackground,
                surface = DarkSurface,
                surfaceVariant = DarkSurfaceVariant,
                onPrimary = Color.Black,
                onSecondary = Color.White,
                onBackground = Color(0xFFE2E8F0),
                onSurface = Color(0xFFF8FAFC)
            )
        }
        else -> {
            lightColorScheme(
                primary = primaryColor,
                secondary = secondaryColor,
                tertiary = GoldAccent,
                background = LightBackground,
                surface = LightSurface,
                surfaceVariant = LightSurfaceVariant,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF0F172A),
                onSurface = Color(0xFF1E293B)
            )
        }
    }

    val glassConfig = GlassConfig(
        opacity = glassOpacity,
        blurRadiusDp = glassBlurDp,
        motionSpeedMultiplier = motionSpeed,
        isOled = isOled
    )

    CompositionLocalProvider(LocalGlassConfig provides glassConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
