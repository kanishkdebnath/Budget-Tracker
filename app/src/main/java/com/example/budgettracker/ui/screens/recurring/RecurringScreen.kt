package com.example.budgettracker.ui.screens.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgettracker.data.entity.RecurringTemplate
import com.example.budgettracker.ui.AppViewModelProvider
import com.example.budgettracker.ui.components.EmptyState
import com.example.budgettracker.ui.components.GradientFab

@Composable
fun RecurringScreen(
    modifier: Modifier = Modifier,
    viewModel: RecurringViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val categories by viewModel.liveCategories.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSheet by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RecurringTemplate?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        // Transparent so the app-level BudgetBackground glow shows through; pin contentColor or
        // contentColorFor(Transparent) → Unspecified renders text black.
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        // The app-level Scaffold already applies system-bar + top-bar insets; don't double them.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            GradientFab("New", onClick = { editing = null; showSheet = true })
        },
    ) { padding ->
        if (sections.active.isEmpty() && sections.inactive.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Autorenew,
                title = "No recurring entries",
                subtitle = "Add templates like Salary or Rent to apply each month.",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (sections.dueCount > 0) item { RecurringDueBanner(sections.dueCount) }
                if (sections.active.isNotEmpty()) {
                    item { SectionHeader("Active") }
                    items(sections.active, key = { it.template.id }) { row ->
                        RecurringCard(
                            row, currency,
                            onClick = { editing = row.template; showSheet = true },
                            onApply = { viewModel.apply(row.template.id) },
                        )
                    }
                }
                if (sections.inactive.isNotEmpty()) {
                    item { SectionHeader("Inactive") }
                    items(sections.inactive, key = { it.template.id }) { row ->
                        RecurringCard(
                            row, currency,
                            onClick = { editing = row.template; showSheet = true },
                            onApply = {},
                        )
                    }
                }
            }
        }
    }

    if (showSheet) {
        RecurringSheet(
            existing = editing,
            categories = categories,
            currency = currency,
            onDismiss = { showSheet = false },
            onSave = { label, categoryId, amount, day, active ->
                val current = editing
                if (current == null) viewModel.create(label, categoryId, amount, day, active)
                else viewModel.update(current.id, label, categoryId, amount, day, active)
                showSheet = false
            },
            onDelete = {
                editing?.let { viewModel.delete(it.id) }
                showSheet = false
            },
        )
    }
}
