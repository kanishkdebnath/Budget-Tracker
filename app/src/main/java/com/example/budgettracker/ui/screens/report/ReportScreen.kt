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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
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
import com.example.budgettracker.export.ExportBundle
import com.example.budgettracker.export.ExportFormat
import com.example.budgettracker.export.ExportManager
import com.example.budgettracker.ui.AppViewModelProvider
import com.example.budgettracker.ui.components.NetBand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReportScreen(
    month: String,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    LaunchedEffect(month) { viewModel.setMonth(month) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val exportBundle by viewModel.exportBundle.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { NarrativeBox(state.narrative) }
        item {
            NetBand(
                state.data.actuals.income, state.data.actuals.expense, state.data.actuals.net, state.currency,
                plannedIncome = state.data.targets.income, plannedExpense = state.data.targets.expense,
            )
        }
        if (state.recurringDueCount > 0) {
            item { RecurringDueBanner(state.recurringDueCount) }
        }
        items(state.data.groups, key = { it.group.id }) { group ->
            ReportGroupCard(group, state.currency)
        }
        item {
            ExportCard(enabled = exportBundle != null) { format ->
                val bundle = exportBundle ?: return@ExportCard
                scope.launch {
                    val uri = withContext(Dispatchers.IO) {
                        ExportManager.export(context, bundle, format, now = System.currentTimeMillis())
                    }
                    context.startActivity(Intent.createChooser(ExportManager.shareIntent(uri, format), "Share"))
                }
            }
        }
    }
}

@Composable
private fun ExportCard(enabled: Boolean, onExport: (ExportFormat) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Export this month", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = { onExport(ExportFormat.EXCEL) }, enabled = enabled, modifier = Modifier.weight(1f)) {
                    Text("Excel")
                }
                FilledTonalButton(onClick = { onExport(ExportFormat.PDF) }, enabled = enabled, modifier = Modifier.weight(1f)) {
                    Text("PDF")
                }
            }
            if (!enabled) {
                Spacer(Modifier.height(6.dp))
                Text("Loading this month's data…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
