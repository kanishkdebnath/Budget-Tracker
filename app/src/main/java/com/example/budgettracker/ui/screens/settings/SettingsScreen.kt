package com.example.budgettracker.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.budgettracker.ui.components.EmptyState

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.Settings,
        title = "Settings",
        subtitle = "Currency, theme, density and export — coming soon.",
        modifier = modifier,
    )
}
