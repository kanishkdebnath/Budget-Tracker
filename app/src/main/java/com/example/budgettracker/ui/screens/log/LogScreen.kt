package com.example.budgettracker.ui.screens.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgettracker.domain.time.MonthUtils
import com.example.budgettracker.export.ExportFormat
import com.example.budgettracker.export.ExportManager
import com.example.budgettracker.ui.AppViewModelProvider
import com.example.budgettracker.ui.components.EmptyState
import com.example.budgettracker.ui.components.ExportSheet
import com.example.budgettracker.ui.components.GradientFab
import com.example.budgettracker.ui.components.cardEntrance
import com.example.budgettracker.ui.components.NetBand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId

@Composable
fun LogScreen(
    month: String,
    onMonthChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    exportSheetOpen: Boolean = false,
    onExportSheetClose: () -> Unit = {},
    viewModel: LogViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    LaunchedEffect(month) { viewModel.setMonth(month) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val categories by viewModel.liveCategories.collectAsStateWithLifecycle()
    val exportBundle by viewModel.exportBundle.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var editingRow by remember { mutableStateOf<TxnRow?>(null) }

    val onExport: (ExportFormat) -> Unit = { format ->
        val bundle = exportBundle
        if (bundle != null) {
            scope.launch {
                val uri = withContext(Dispatchers.IO) {
                    ExportManager.export(context, bundle, format, now = System.currentTimeMillis())
                }
                context.startActivity(android.content.Intent.createChooser(ExportManager.shareIntent(uri, format), "Share"))
            }
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
            GradientFab("Add", onClick = { editingRow = null; showSheet = true })
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Spacer(Modifier.height(8.dp))
            NetBand(uiState.income, uiState.expense, uiState.net, currency, Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(12.dp))
            LogFilterChips(filter, uiState.incomeCount, uiState.expenseCount, viewModel::setFilter)
            Spacer(Modifier.height(8.dp))
            if (uiState.sections.isEmpty()) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    title = "No transactions in ${MonthUtils.monthLabel(month)}",
                    subtitle = "Tap + to add your first income or expense.",
                )
            } else {
                LazyColumn(
                    // Extra bottom inset so the last card clears the floating "Add" FAB (~56dp + 16dp margin).
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(uiState.sections, key = { _, it -> it.dayLabel }) { index, section ->
                        DateCard(
                            section, currency,
                            onRowClick = { editingRow = it; showSheet = true },
                            modifier = Modifier.cardEntrance(index, month),
                        )
                    }
                }
            }
        }
    }

    if (exportSheetOpen) {
        ExportSheet(
            title = "Export ${MonthUtils.monthLabel(month)}",
            description = "Share this month's individual transactions as a formatted log.",
            enabled = exportBundle != null,
            onExcel = { onExport(ExportFormat.EXCEL); onExportSheetClose() },
            onPdf = { onExport(ExportFormat.PDF); onExportSheetClose() },
            onDismiss = onExportSheetClose,
        )
    }

    if (showSheet) {
        TransactionSheet(
            existing = editingRow,
            categories = categories,
            currency = currency,
            onDismiss = { showSheet = false },
            onSave = { date, categoryId, amount, note ->
                val editing = editingRow
                if (editing == null) viewModel.addTransaction(date, categoryId, amount, note)
                else viewModel.editTransaction(editing.id, date, categoryId, amount, note)
                showSheet = false
                // §F1.5: a date in another month jumps there with a confirming snackbar.
                val txnMonth = MonthUtils.monthOf(date, ZoneId.systemDefault())
                if (txnMonth != month) {
                    onMonthChange(txnMonth)
                    scope.launch { snackbarHostState.showSnackbar("Saved to ${MonthUtils.monthLabel(txnMonth)}") }
                }
            },
            onDelete = {
                editingRow?.let { viewModel.deleteTransaction(it.id) }
                showSheet = false
            },
        )
    }
}
