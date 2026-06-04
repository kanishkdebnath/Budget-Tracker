package com.example.budgettracker.ui.screens.report

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.domain.time.MonthUtils
import com.example.budgettracker.export.ExportBundle
import com.example.budgettracker.export.ExportFormat
import com.example.budgettracker.export.ExportManager
import com.example.budgettracker.ui.AppViewModelProvider
import com.example.budgettracker.ui.components.BudgetCard
import com.example.budgettracker.ui.components.cardEntrance
import com.example.budgettracker.ui.components.GradientButton
import com.example.budgettracker.ui.components.GradientButtonTone
import com.example.budgettracker.ui.components.NetBand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReportScreen(
    month: String,
    modifier: Modifier = Modifier,
    exportSheetOpen: Boolean = false,
    onExportSheetClose: () -> Unit = {},
    viewModel: ReportViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    LaunchedEffect(month) { viewModel.setMonth(month) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val exportBundle by viewModel.exportBundle.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Build the file off the main thread, then hand it to the Android share sheet (§F6). Shared by
    // the inline export card and the top-bar export sheet.
    val onExport: (ExportFormat) -> Unit = { format ->
        val bundle = exportBundle
        if (bundle != null) {
            scope.launch {
                val uri = withContext(Dispatchers.IO) {
                    ExportManager.export(context, bundle, format, now = System.currentTimeMillis())
                }
                context.startActivity(Intent.createChooser(ExportManager.shareIntent(uri, format), "Share"))
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { NarrativeBox(state.narrative, MonthUtils.monthLabel(month)) }
        item {
            NetBand(
                state.data.actuals.income, state.data.actuals.expense, state.data.actuals.net, state.currency,
                plannedIncome = state.data.targets.income, plannedExpense = state.data.targets.expense,
            )
        }
        if (state.recurringDueCount > 0) {
            item { RecurringDueBanner(state.recurringDueCount) }
        }
        itemsIndexed(state.data.groups, key = { _, it -> it.group.id }) { index, group ->
            ReportGroupCard(group, state.currency, Modifier.cardEntrance(index, month))
        }
        item { ExportCard(enabled = exportBundle != null, onExport = onExport) }
    }

    if (exportSheetOpen) {
        ExportSheet(
            monthLabel = MonthUtils.monthLabel(month),
            enabled = exportBundle != null,
            onExport = { format -> onExport(format); onExportSheetClose() },
            onDismiss = onExportSheetClose,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportSheet(
    monthLabel: String,
    enabled: Boolean,
    onExport: (ExportFormat) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Export $monthLabel", style = MaterialTheme.typography.titleLarge)
            Text(
                "Share this month's transactions (Excel) or the target-vs-actual summary (PDF).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(
                    "Excel", onClick = { onExport(ExportFormat.EXCEL) }, enabled = enabled,
                    tone = GradientButtonTone.TONAL, modifier = Modifier.weight(1f),
                )
                GradientButton(
                    "PDF", onClick = { onExport(ExportFormat.PDF) }, enabled = enabled,
                    tone = GradientButtonTone.TONAL, modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExportCard(enabled: Boolean, onExport: (ExportFormat) -> Unit) {
    BudgetCard {
        Column(Modifier.padding(16.dp)) {
            Text("Export this month", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GradientButton(
                    "Excel", onClick = { onExport(ExportFormat.EXCEL) }, enabled = enabled,
                    tone = GradientButtonTone.TONAL, modifier = Modifier.weight(1f),
                )
                GradientButton(
                    "PDF", onClick = { onExport(ExportFormat.PDF) }, enabled = enabled,
                    tone = GradientButtonTone.TONAL, modifier = Modifier.weight(1f),
                )
            }
            if (!enabled) {
                Spacer(Modifier.height(6.dp))
                Text("Loading this month's data…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
