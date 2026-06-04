package com.example.budgettracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val LightColors = lightColorScheme(
    primary = LightPrimary, onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer, onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary, onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer, onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary, onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer, onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError, onError = LightOnError,
    errorContainer = LightErrorContainer, onErrorContainer = LightOnErrorContainer,
    background = LightBackground, onBackground = LightOnBackground,
    surface = LightSurface, onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant, onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = LightSurfaceContainerLowest, surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer, surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceBright = LightSurfaceBright, surfaceDim = LightSurfaceDim,
    outline = LightOutline, outlineVariant = LightOutlineVariant,
    inverseSurface = LightInverseSurface, inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary, scrim = LightScrim,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary, onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer, onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary, onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer, onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary, onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer, onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError, onError = DarkOnError,
    errorContainer = DarkErrorContainer, onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground, onBackground = DarkOnBackground,
    surface = DarkSurface, onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant, onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = DarkSurfaceContainerLowest, surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer, surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceBright = DarkSurfaceBright, surfaceDim = DarkSurfaceDim,
    outline = DarkOutline, outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkInverseSurface, inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary, scrim = DarkScrim,
)

/** Fixed semantic signal colors — never replaced by dynamic color (design §7.3). */
@Immutable
data class BudgetSemanticColors(
    val income: Color,
    val expense: Color,
    val overage: Color,
)

val LocalBudgetSemanticColors = staticCompositionLocalOf<BudgetSemanticColors> {
    error("BudgetSemanticColors not provided; wrap content in BudgetTrackerTheme")
}

/** Ergonomic accessor: BudgetTheme.semanticColors.income, BudgetTheme.density.rowMinHeight, etc. */
object BudgetTheme {
    val semanticColors: BudgetSemanticColors
        @Composable @ReadOnlyComposable
        get() = LocalBudgetSemanticColors.current

    val density: DensityMode
        @Composable @ReadOnlyComposable
        get() = LocalDensityMode.current
}

@Composable
fun BudgetTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Fixed navy brand palette is the default; dynamic color is opt-in via Settings (design §2, §7.3).
    dynamicColor: Boolean = false,
    densityMode: DensityMode = DensityMode.COMFORTABLE,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    // income/overage are fixed signal; expense follows the live onSurface ("uses text color", §3.1).
    val semanticColors = BudgetSemanticColors(
        income = if (darkTheme) IncomeDark else IncomeLight,
        overage = if (darkTheme) OverageDark else OverageLight,
        expense = colorScheme.onSurface,
    )

    // Compact tightens all text via the font scale (dp dimensions are untouched, §7.2).
    val base = LocalDensity.current
    val scaledDensity =
        if (densityMode.bodyScale == 1f) base
        else Density(base.density, base.fontScale * densityMode.bodyScale)

    CompositionLocalProvider(
        LocalBudgetSemanticColors provides semanticColors,
        LocalDensityMode provides densityMode,
        LocalDensity provides scaledDensity,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
