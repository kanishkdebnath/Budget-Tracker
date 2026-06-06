package com.example.budgettracker.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DensityMedium
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.ui.AppViewModelProvider
import com.example.budgettracker.ui.components.BudgetCard
import com.example.budgettracker.ui.theme.DensityMode

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val densityMode by viewModel.densityMode.collectAsStateWithLifecycle()
    var showCurrency by remember { mutableStateOf(false) }
    var showTheme by remember { mutableStateOf(false) }
    var showDensity by remember { mutableStateOf(false) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSection("Money") {
            SettingTile(
                "Currency",
                Icons.Outlined.Payments,
                "${currencyFlag(currency)} $currency · ${Money.symbolOf(currency).trim()}".trim(),
                onClick = { showCurrency = true },
            )
        }
        SettingsSection("Appearance") {
            SettingTile("Theme", Icons.Outlined.Palette, themeMode.label, onClick = { showTheme = true })
            TileDivider()
            SettingTile("Density", Icons.Outlined.DensityMedium, densityMode.label, onClick = { showDensity = true })
        }
        SettingsSection("About") {
            SettingTile("Version", Icons.Outlined.Info, "1.0")
            TileDivider()
            SettingTile("Privacy", Icons.Outlined.Lock, "All data stays on this device.")
        }
    }

    if (showCurrency) {
        CurrencyPickerSheet(
            current = currency,
            onDismiss = { showCurrency = false },
            onSelect = { viewModel.setCurrency(it); showCurrency = false },
        )
    }
    if (showTheme) {
        ThemeDialog(
            current = themeMode,
            onDismiss = { showTheme = false },
            onSelect = { viewModel.setThemeMode(it); showTheme = false },
        )
    }
    if (showDensity) {
        DensityDialog(
            current = densityMode,
            onDismiss = { showDensity = false },
            onSelect = { viewModel.setDensity(it); showDensity = false },
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title.uppercase(),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BudgetCard(content = content)
    }
}

// Row left padding (16) + leading icon (22) + spacer (16): where the tile text starts, so dividers align to it.
private val TileTextInset = 16.dp + 22.dp + 16.dp

@Composable
private fun SettingTile(
    title: String,
    icon: ImageVector,
    value: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = Modifier.fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(16.dp)
    Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            value?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        // A chevron marks the rows that open a picker; static rows (Version/Privacy) have none.
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** Hairline divider between tiles in a section, inset to start at the tile text. */
@Composable
private fun TileDivider() {
    HorizontalDivider(
        Modifier.padding(start = TileTextInset),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

@Composable
private fun ThemeDialog(current: ThemeMode, onDismiss: () -> Unit, onSelect: (ThemeMode) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Theme") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(mode) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == current, onClick = { onSelect(mode) })
                        Spacer(Modifier.width(8.dp))
                        Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}

@Composable
private fun DensityDialog(current: DensityMode, onDismiss: () -> Unit, onSelect: (DensityMode) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Density") },
        text = {
            Column {
                DensityMode.entries.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(mode) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == current, onClick = { onSelect(mode) })
                        Spacer(Modifier.width(8.dp))
                        Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}
