package com.example.budgettracker.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.TransactionEntity
import com.example.budgettracker.data.repository.CategoryRepository
import com.example.budgettracker.data.repository.PreferencesRepository
import com.example.budgettracker.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class TxnFilter(val label: String) { ALL("All"), INCOME("Income"), EXPENSE("Expense") }

data class TxnRow(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val groupName: String,
    val leadingColor: String,   // category.color ?: group.color
    val kind: Kind,
    val amount: Long,
    val note: String?,
    val date: Long,
)

data class DateSection(
    val dayLabel: String,   // "Jun 5"
    val weekday: String,    // "THU" (uppercased in the UI)
    val dayNet: Long,
    val rows: List<TxnRow>,
)

data class LogUiState(
    val sections: List<DateSection>,
    val income: Long,
    val expense: Long,
    val net: Long,
    val incomeCount: Int,
    val expenseCount: Int,
) {
    companion object {
        val EMPTY = LogUiState(emptyList(), 0, 0, 0, 0, 0)
    }
}

private val DAY_FORMAT = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
private val WEEKDAY_FORMAT = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

/**
 * Pure: build the Log UI state for a month's [transactions]. Net-band totals always reflect the full
 * month; [filter] only narrows the per-date sections (§F1.2/§5.1). A transaction whose category is
 * missing is skipped.
 */
fun buildLogState(
    transactions: List<TransactionEntity>,
    categoriesById: Map<Long, Category>,
    groupsById: Map<Long, CategoryGroup>,
    filter: TxnFilter,
    zone: ZoneId,
): LogUiState {
    data class Resolved(val txn: TransactionEntity, val category: Category)
    val resolved = transactions.mapNotNull { t -> categoriesById[t.categoryId]?.let { Resolved(t, it) } }

    val income = resolved.filter { it.category.kind == Kind.INCOME }.sumOf { it.txn.amount }
    val expense = resolved.filter { it.category.kind == Kind.EXPENSE }.sumOf { it.txn.amount }
    val incomeCount = resolved.count { it.category.kind == Kind.INCOME }
    val expenseCount = resolved.count { it.category.kind == Kind.EXPENSE }

    val filtered = when (filter) {
        TxnFilter.ALL -> resolved
        TxnFilter.INCOME -> resolved.filter { it.category.kind == Kind.INCOME }
        TxnFilter.EXPENSE -> resolved.filter { it.category.kind == Kind.EXPENSE }
    }

    val sections = filtered
        .groupBy { Instant.ofEpochMilli(it.txn.date).atZone(zone).toLocalDate() }
        .entries
        .sortedByDescending { it.key }
        .map { (date, items) ->
            val rows = items
                .sortedWith(compareByDescending<Resolved> { it.txn.date }.thenByDescending { it.txn.createdAt })
                .map { r ->
                    val group = groupsById[r.category.groupId]
                    TxnRow(
                        id = r.txn.id,
                        categoryId = r.category.id,
                        categoryName = r.category.name,
                        groupName = group?.name ?: "",
                        leadingColor = r.category.color ?: group?.color ?: "#64748b",
                        kind = r.category.kind,
                        amount = r.txn.amount,
                        note = r.txn.description,
                        date = r.txn.date,
                    )
                }
            val dayNet = rows.sumOf { if (it.kind == Kind.INCOME) it.amount else -it.amount }
            DateSection(date.format(DAY_FORMAT), date.format(WEEKDAY_FORMAT), dayNet, rows)
        }

    return LogUiState(sections, income, expense, income - expense, incomeCount, expenseCount)
}

class LogViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    preferencesRepository: PreferencesRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val month = MutableStateFlow<String?>(null)
    private val _filter = MutableStateFlow(TxnFilter.ALL)
    val filter: StateFlow<TxnFilter> = _filter

    val currency: StateFlow<String> = preferencesRepository.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "INR")

    /** Live categories for the add/edit picker. */
    val liveCategories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<LogUiState> = combine(
        month.filterNotNull().flatMapLatest { transactionRepository.observeMonth(it, zoneId) },
        categoryRepository.observeCategories(includeArchived = true),
        categoryRepository.observeGroups(includeArchived = true),
        _filter,
    ) { txns, cats, groups, filter ->
        buildLogState(txns, cats.associateBy { it.id }, groups.associateBy { it.id }, filter, zoneId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogUiState.EMPTY)

    fun setMonth(value: String) { month.value = value }
    fun setFilter(value: TxnFilter) { _filter.value = value }

    fun addTransaction(date: Long, categoryId: Long, amount: Long, note: String?) =
        viewModelScope.launch { transactionRepository.add(date, categoryId, amount, note) }

    fun editTransaction(id: Long, date: Long, categoryId: Long, amount: Long, note: String?) =
        viewModelScope.launch { transactionRepository.edit(id, date, categoryId, amount, note) }

    fun deleteTransaction(id: Long) =
        viewModelScope.launch { transactionRepository.delete(id) }
}
