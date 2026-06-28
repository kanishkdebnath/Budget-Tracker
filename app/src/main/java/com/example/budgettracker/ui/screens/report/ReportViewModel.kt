package com.example.budgettracker.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Target
import com.example.budgettracker.data.entity.TransactionEntity
import com.example.budgettracker.data.repository.CategoryRepository
import com.example.budgettracker.data.repository.PreferencesRepository
import com.example.budgettracker.data.repository.RecurringRepository
import com.example.budgettracker.data.repository.TargetRepository
import com.example.budgettracker.data.repository.TransactionRepository
import com.example.budgettracker.domain.report.ReportData
import com.example.budgettracker.domain.report.ReportTotals
import com.example.budgettracker.domain.report.aggregateReport
import com.example.budgettracker.domain.report.generateNarrative
import com.example.budgettracker.export.ExportBundle
import com.example.budgettracker.ui.screens.log.TxnRow
import com.example.budgettracker.export.buildExportRecurringRows
import com.example.budgettracker.export.buildExportTxnRows
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId

fun buildCategoryTxnMap(
    transactions: List<TransactionEntity>,
    categoriesById: Map<Long, Category>,
    groupsById: Map<Long, CategoryGroup>,
): Map<Long, List<TxnRow>> =
    transactions
        .mapNotNull { txn ->
            val cat = categoriesById[txn.categoryId] ?: return@mapNotNull null
            val group = groupsById[cat.groupId]
            TxnRow(
                id = txn.id,
                categoryId = cat.id,
                categoryName = cat.name,
                groupName = group?.name ?: "",
                leadingColor = cat.color ?: group?.color ?: "#64748b",
                iconKey = cat.icon,
                kind = cat.kind,
                amount = txn.amount,
                note = txn.description,
                date = txn.date,
            )
        }
        .groupBy { it.categoryId }
        .mapValues { (_, rows) -> rows.sortedByDescending { it.date } }

data class ReportUiState(
    val data: ReportData,
    val narrative: String,
    val recurringDueCount: Int,
    val currency: String,
    val loaded: Boolean,
    val transactionsByCategoryId: Map<Long, List<TxnRow>> = emptyMap(),
) {
    companion object {
        val EMPTY = ReportUiState(
            data = ReportData(emptyList(), ReportTotals(0, 0), ReportTotals(0, 0)),
            narrative = "",
            recurringDueCount = 0,
            currency = "INR",
            loaded = false,
        )
    }
}

class ReportViewModel(
    private val transactionRepository: TransactionRepository,
    private val targetRepository: TargetRepository,
    private val categoryRepository: CategoryRepository,
    private val recurringRepository: RecurringRepository,
    preferencesRepository: PreferencesRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val month = MutableStateFlow<String?>(null)
    private val currency: StateFlow<String> = preferencesRepository.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "INR")

    private data class MonthScoped(val month: String, val transactions: List<TransactionEntity>, val targets: List<Target>)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthScoped = month.filterNotNull().flatMapLatest { m ->
        combine(transactionRepository.observeMonth(m, zoneId), targetRepository.observeMonth(m)) { txns, targets ->
            MonthScoped(m, txns, targets)
        }
    }

    val uiState: StateFlow<ReportUiState> = combine(
        monthScoped,
        categoryRepository.observeCategories(includeArchived = true),
        categoryRepository.observeGroups(includeArchived = true),
        recurringRepository.observeAll(),
        currency,
    ) { scoped, categories, groups, recurring, currency ->
        val data = aggregateReport(scoped.transactions, scoped.targets, categories, groups)
        val narrative = generateNarrative(scoped.month, data, currency, scoped.transactions.isNotEmpty())
        val dueCount = recurring.count { it.active && it.lastRunMonth != scoped.month }
        val catsById = categories.associateBy { it.id }
        val groupsById = groups.associateBy { it.id }
        ReportUiState(
            data = data,
            narrative = narrative,
            recurringDueCount = dueCount,
            currency = currency,
            loaded = true,
            transactionsByCategoryId = buildCategoryTxnMap(scoped.transactions, catsById, groupsById),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportUiState.EMPTY)

    /** Full data bundle for Excel/PDF export (§8). Null until a month's data has loaded. */
    val exportBundle: StateFlow<ExportBundle?> = combine(
        monthScoped,
        categoryRepository.observeCategories(includeArchived = true),
        categoryRepository.observeGroups(includeArchived = true),
        recurringRepository.observeAll(),
        currency,
    ) { scoped, categories, groups, recurring, currency ->
        val report = aggregateReport(scoped.transactions, scoped.targets, categories, groups)
        val narrative = generateNarrative(scoped.month, report, currency, scoped.transactions.isNotEmpty())
        ExportBundle(
            month = scoped.month,
            currency = currency,
            narrative = narrative,
            report = report,
            transactions = buildExportTxnRows(scoped.transactions, categories.associateBy { it.id }, groups.associateBy { it.id }),
            recurring = buildExportRecurringRows(recurring, scoped.month),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setMonth(value: String) { month.value = value }
}
