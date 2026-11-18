package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val EclDarkColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = ObsidianBlack,
    primaryContainer = GoldContainer,
    onPrimaryContainer = AmberGold,
    secondary = EmeraldPitch,
    onSecondary = ObsidianBlack,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = EmeraldPitch,
    tertiary = SkyDraw,
    onTertiary = ObsidianBlack,
    tertiaryContainer = SkyContainer,
    onTertiaryContainer = SkyDraw,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = SlateCard,
    onSurface = TextPrimary,
    surfaceVariant = SlateElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianBlack,
    surfaceContainerLow = SlateCard,
    surfaceContainer = SlateCard,
    surfaceContainerHigh = SlateElevated,
    surfaceContainerHighest = SlateBorder,
    outline = SlateBorder,
    outlineVariant = ColorTokens.SubtleBorder,
    error = CrimsonDispute,
    onError = TextPrimary,
    errorContainer = CrimsonContainer,
    onErrorContainer = CrimsonDispute,
)

private object ColorTokens {
    val SubtleBorder = androidx.compose.ui.graphics.Color(0xFF232634)
}

val EclShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun EclTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = EclDarkColorScheme,
            typography = EclTypography,
            shapes = EclShapes,
            content = content,
        )
    }
}
