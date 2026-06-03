package com.example.budgettracker.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.ui.AppViewModelProvider
import com.example.budgettracker.ui.components.NetBand

@Composable
fun ReportScreen(
    month: String,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    LaunchedEffect(month) { viewModel.setMonth(month) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { NarrativeBox(state.narrative) }
        item {
            NetBand(state.data.actuals.income, state.data.actuals.expense, state.data.actuals.net, state.currency)
        }
        item {
            Text(
                "Planned   Income ${Money.format(state.data.targets.income, state.currency)}   ·   " +
                    "Expense ${Money.format(state.data.targets.expense, state.currency)}",
                modifier = Modifier.padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.recurringDueCount > 0) {
            item { RecurringDueBanner(state.recurringDueCount) }
        }
        items(state.data.groups, key = { it.group.id }) { group ->
            ReportGroupCard(group, state.currency)
        }
    }
}
