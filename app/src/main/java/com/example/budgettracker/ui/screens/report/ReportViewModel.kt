package com.example.budgettracker.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId

data class ReportUiState(
    val data: ReportData,
    val narrative: String,
    val recurringDueCount: Int,
    val currency: String,
    val loaded: Boolean,
) {
    companion object {
        val EMPTY = ReportUiState(
            ReportData(emptyList(), ReportTotals(0, 0), ReportTotals(0, 0)), "", 0, "INR", loaded = false,
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
        ReportUiState(data, narrative, dueCount, currency, loaded = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportUiState.EMPTY)

    fun setMonth(value: String) { month.value = value }
}
