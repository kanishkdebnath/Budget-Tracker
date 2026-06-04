package com.example.budgettracker.ui.screens.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.domain.report.inferGroupKind
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgettracker.ui.AppViewModelProvider
import com.example.budgettracker.ui.components.cardEntrance
import com.example.budgettracker.ui.components.NetBand
import kotlinx.coroutines.launch

@Composable
fun PlanScreen(
    month: String,
    modifier: Modifier = Modifier,
    viewModel: PlanViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    LaunchedEffect(month) { viewModel.setMonth(month) }

    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val inputs by viewModel.inputs.collectAsStateWithLifecycle()
    val banner by viewModel.banner.collectAsStateWithLifecycle()
    val totals by viewModel.totals.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        // Transparent so the app-level BudgetBackground glow shows through; pin contentColor or
        // contentColorFor(Transparent) → Unspecified renders text black.
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        // The app-level Scaffold already applies system-bar + top-bar insets; don't double them.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height(8.dp))
                if (banner != PrefillBanner.NONE) {
                    PlanBanner(banner, Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(12.dp))
                }
                NetBand(totals.income, totals.expense, totals.net, currency, Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
                val incomeGroups = sections.filter { inferGroupKind(it.categories) == Kind.INCOME }
                val expenseGroups = sections.filter { inferGroupKind(it.categories) == Kind.EXPENSE }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    // Extra bottom inset so the last card scrolls clear of the floating Save bar.
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (incomeGroups.isNotEmpty()) {
                        item("income-label") { PlanSectionLabel("Income") }
                        itemsIndexed(incomeGroups, key = { _, it -> it.group.id }) { index, group ->
                            PlanGroupCard(group, inputs, currency, viewModel::onInputChange, Modifier.cardEntrance(index, month))
                        }
                    }
                    if (expenseGroups.isNotEmpty()) {
                        item("expense-label") { PlanSectionLabel("Expense groups") }
                        itemsIndexed(expenseGroups, key = { _, it -> it.group.id }) { index, group ->
                            PlanGroupCard(group, inputs, currency, viewModel::onInputChange, Modifier.cardEntrance(index, month))
                        }
                    }
                }
            }
            PlanSaveBar(
                targetCount = inputs.count { Money.parseTargetToMinor(it.value) != null },
                onSave = {
                    viewModel.save()
                    scope.launch { snackbarHostState.showSnackbar("Targets saved") }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
    }
}
