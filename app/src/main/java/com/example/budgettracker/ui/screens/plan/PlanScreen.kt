package com.example.budgettracker.ui.screens.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgettracker.ui.AppViewModelProvider
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PlanSaveBar(onSave = {
                viewModel.save()
                scope.launch { snackbarHostState.showSnackbar("Targets saved") }
            })
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Spacer(Modifier.height(8.dp))
            if (banner != PrefillBanner.NONE) {
                PlanBanner(banner, Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
            }
            NetBand(totals.income, totals.expense, totals.net, currency, Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sections, key = { it.group.id }) { group ->
                    PlanGroupCard(group, inputs, currency, viewModel::onInputChange)
                }
            }
        }
    }
}
