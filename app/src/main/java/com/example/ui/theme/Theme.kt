package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = CeylonEmerald,
    onPrimary = Color.White,
    primaryContainer = CeylonMintContainer,
    onPrimaryContainer = CeylonOnMintContainer,
    secondary = TerracottaSpice,
    onSecondary = Color.White,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,
    tertiary = GoldenTurmeric,
    onTertiary = Color.White,
    tertiaryContainer = GoldenTurmericContainer,
    onTertiaryContainer = OnGoldenTurmericContainer,
    error = RoseAlert,
    onError = Color.White,
    errorContainer = RoseAlertContainer,
    onErrorContainer = RoseAlert,
    background = IvoryBackground,
    onBackground = InkPrimary,
    surface = WarmWhiteSurface,
    onSurface = InkPrimary,
    surfaceVariant = SoftSandSurface,
    onSurfaceVariant = InkSecondary,
    surfaceContainerLowest = WarmWhiteSurface,
    surfaceContainerLow = IvoryBackground,
    surfaceContainer = SoftSandSurface,
    surfaceContainerHigh = WarmSandVariant,
    outline = OutlineWarm,
    outlineVariant = WarmSandVariant
)

private val DarkColors = darkColorScheme(
    primary = MintHighlight,
    onPrimary = MintOnHighlight,
    primaryContainer = CeylonEmeraldDark,
    onPrimaryContainer = CeylonMintContainer,
    secondary = TerracottaHighlight,
    onSecondary = OnTerracottaContainer,
    secondaryContainer = Color(0xFF5D2411),
    onSecondaryContainer = TerracottaContainer,
    tertiary = TurmericHighlight,
    onTertiary = OnGoldenTurmericContainer,
    tertiaryContainer = Color(0xFF523500),
    onTertiaryContainer = GoldenTurmericContainer,
    error = Color(0xFFFF8A9F),
    onError = Color(0xFF5C0019),
    errorContainer = Color(0xFF7D0D29),
    onErrorContainer = RoseAlertContainer,
    background = DeepForestBackground,
    onBackground = DarkTextPrimary,
    surface = DeepForestSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DeepForestVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = DeepForestBackground,
    surfaceContainerLow = DeepForestSurface,
    surfaceContainer = DeepForestCard,
    surfaceContainerHigh = DeepForestVariant,
    outline = DarkOutline,
    outlineVariant = DeepForestVariant
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun DailyAppTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
