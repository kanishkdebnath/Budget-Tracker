package com.example.budgettracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

/** Transparent containers so the brand top-glow (see [BudgetBackground]) shows through the bar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun transparentBarColors(): TopAppBarColors =
    TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
    )

/** Month-nav top bar for Log / Plan / Report (design §5). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthNavTopBar(
    monthLabel: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSettings: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(monthLabel, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }
        },
        actions = {
            IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
        },
        colors = transparentBarColors(),
    )
}

/** Titled top bar for Categories / Recurring, with an optional Search action + the Settings gear. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionTopBar(title: String, onSettings: () -> Unit, onSearch: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        actions = {
            if (onSearch != null) {
                IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = "Search") }
            }
            IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
        },
        colors = transparentBarColors(),
    )
}

/** Back top bar for Settings. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        colors = transparentBarColors(),
    )
}
