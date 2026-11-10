package com.kironillo.getatapi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val LocalGeTaTaPiColors = staticCompositionLocalOf { LightGeTaTaPiColors }

object GeTaTaPiTheme {
    val colors: GeTaTaPiColors
        @Composable get() = LocalGeTaTaPiColors.current
}

private fun lightScheme() =
    lightColorScheme(
        primary = LightGeTaTaPiColors.primary,
        onPrimary = LightGeTaTaPiColors.onPrimary,
        background = LightGeTaTaPiColors.background,
        onBackground = LightGeTaTaPiColors.onSurface,
        surface = LightGeTaTaPiColors.surface,
        onSurface = LightGeTaTaPiColors.onSurface,
        surfaceVariant = LightGeTaTaPiColors.surface,
        onSurfaceVariant = LightGeTaTaPiColors.onSurfaceVariant,
        outline = LightGeTaTaPiColors.outline,
        error = LightGeTaTaPiColors.error,
    )

private fun darkScheme() =
    darkColorScheme(
        primary = DarkGeTaTaPiColors.primary,
        onPrimary = DarkGeTaTaPiColors.onPrimary,
        background = DarkGeTaTaPiColors.background,
        onBackground = DarkGeTaTaPiColors.onSurface,
        surface = DarkGeTaTaPiColors.surface,
        onSurface = DarkGeTaTaPiColors.onSurface,
        surfaceVariant = DarkGeTaTaPiColors.surface,
        onSurfaceVariant = DarkGeTaTaPiColors.onSurfaceVariant,
        outline = DarkGeTaTaPiColors.outline,
        error = DarkGeTaTaPiColors.error,
    )

@Suppress("DEPRECATION")
@Composable
fun GeTaTaPiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val appColors = if (darkTheme) DarkGeTaTaPiColors else LightGeTaTaPiColors

    val scheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> darkScheme()
            else -> lightScheme()
        }

    CompositionLocalProvider(LocalGeTaTaPiColors provides appColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = GeTaTaPiTypography,
            shapes = GeTaTaPiShapes,
            content = content,
        )
    }
}
