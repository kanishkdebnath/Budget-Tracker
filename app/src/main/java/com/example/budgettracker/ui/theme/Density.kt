package com.example.budgettracker.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * List density (design §7.2). A single [LocalDensityMode] drives row min-height, in-row vertical
 * padding, and a body-text scale (applied globally via the theme's font scale). Comfortable is the
 * default; Settings → Appearance → Density flips it (stored in DataStore).
 */
enum class DensityMode(val storageValue: String, val label: String) {
    COMFORTABLE("comfortable", "Comfortable"),
    COMPACT("compact", "Compact"),
    ;

    /** Min height for list / transaction rows (keeps the comfortable touch target ≥ 48dp). */
    val rowMinHeight: Dp get() = if (this == COMFORTABLE) 52.dp else 44.dp

    /** Vertical padding inside a list row. */
    val rowPaddingVertical: Dp get() = if (this == COMFORTABLE) 12.dp else 10.dp

    /** Multiplier folded into the theme's font scale so all text tightens in Compact. */
    val bodyScale: Float get() = if (this == COMFORTABLE) 1f else 0.95f

    companion object {
        fun fromStorage(value: String): DensityMode =
            entries.firstOrNull { it.storageValue == value } ?: COMFORTABLE
    }
}

val LocalDensityMode = staticCompositionLocalOf { DensityMode.COMFORTABLE }
