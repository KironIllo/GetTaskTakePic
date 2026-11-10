package com.kironillo.getatapi.ui.theme

import androidx.compose.ui.graphics.Color

/** Raw palette from docs/05-design-system.docx. */
object GeTaTaPiPalette {
    val Blue400 = Color(0xFF60A5FA)
    val Blue500 = Color(0xFF3B82F6)

    val Neutral50 = Color(0xFFFAFAFA)
    val Neutral100 = Color(0xFFF3F4F6)
    val Neutral200 = Color(0xFFE5E7EB)
    val Neutral500 = Color(0xFF4F5A6C)
    val Neutral700 = Color(0xFF374151)
    val Neutral800 = Color(0xFF1F2937)
    val Neutral900 = Color(0xFF171717)

    val Green500 = Color(0xFF22C55E)
    val Red500 = Color(0xFFEF4444)
    val Amber500 = Color(0xFFF59E0B)
}

data class GeTaTaPiColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val success: Color,
    val error: Color,
    val warning: Color,
)

val LightGeTaTaPiColors =
    GeTaTaPiColors(
        primary = GeTaTaPiPalette.Blue500,
        onPrimary = GeTaTaPiPalette.Neutral50,
        background = GeTaTaPiPalette.Neutral50,
        surface = GeTaTaPiPalette.Neutral100,
        onSurface = GeTaTaPiPalette.Neutral700,
        onSurfaceVariant = GeTaTaPiPalette.Neutral500,
        outline = GeTaTaPiPalette.Neutral200,
        success = GeTaTaPiPalette.Green500,
        error = GeTaTaPiPalette.Red500,
        warning = GeTaTaPiPalette.Amber500,
    )

val DarkGeTaTaPiColors =
    GeTaTaPiColors(
        primary = GeTaTaPiPalette.Blue400,
        onPrimary = GeTaTaPiPalette.Neutral900,
        background = GeTaTaPiPalette.Neutral900,
        surface = GeTaTaPiPalette.Neutral800,
        onSurface = GeTaTaPiPalette.Neutral50,
        onSurfaceVariant = GeTaTaPiPalette.Neutral200,
        outline = GeTaTaPiPalette.Neutral700,
        success = GeTaTaPiPalette.Green500,
        error = GeTaTaPiPalette.Red500,
        warning = GeTaTaPiPalette.Amber500,
    )
