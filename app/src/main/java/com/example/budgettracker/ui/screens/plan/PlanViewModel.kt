package com.example.budgettracker.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.repository.CategoryRepository
import com.example.budgettracker.data.repository.PreferencesRepository
import com.example.budgettracker.data.repository.TargetRepository
import com.example.budgettracker.domain.money.Money
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

enum class PrefillBanner { NONE, CARRIED_FORWARD, FIRST_TIME }

data class PlanGroup(val group: CategoryGroup, val categories: List<Category>)

data class PlanTotals(val income: Long, val expense: Long, val net: Long)

/** Live groups with their live categories (income + expense), in order; empty groups dropped. */
fun buildPlanGroups(groups: List<CategoryGroup>, categories: List<Category>): List<PlanGroup> {
    val byGroup = categories.filter { !it.archived }.groupBy { it.groupId }
    return groups.filter { !it.archived }.sortedBy { it.order }
        .map { g -> PlanGroup(g, (byGroup[g.id] ?: emptyList()).sortedBy { it.order }) }
        .filter { it.categories.isNotEmpty() }
}

/** Pure: sum entered target inputs by category kind (§5.2 target Net band). */
fun computeTargetTotals(inputs: Map<Long, String>, categoriesById: Map<Long, Category>): PlanTotals {
    var income = 0L
    var expense = 0L
    inputs.forEach { (categoryId, text) ->
        val amount = Money.parseTargetToMinor(text) ?: 0L
        when (categoriesById[categoryId]?.kind) {
            Kind.INCOME -> income += amount
            Kind.EXPENSE -> expense += amount
            null -> Unit
        }
    }
    return PlanTotals(income, expense, income - expense)
}

class PlanViewModel(
    private val categoryRepository: CategoryRepository,
    private val targetRepository: TargetRepository,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _month = MutableStateFlow<String?>(null)
    private val _inputs = MutableStateFlow<Map<Long, String>>(emptyMap())
    val inputs: StateFlow<Map<Long, String>> = _inputs

    private val _banner = MutableStateFlow(PrefillBanner.NONE)
    val banner: StateFlow<PrefillBanner> = _banner

    val currency: StateFlow<String> = preferencesRepository.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "INR")

    val sections: StateFlow<List<PlanGroup>> = combine(
        categoryRepository.observeGroups(),
        categoryRepository.observeCategories(),
    ) { groups, categories -> buildPlanGroups(groups, categories) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totals: StateFlow<PlanTotals> = combine(
        _inputs,
        categoryRepository.observeCategories(),
    ) { inputs, categories -> computeTargetTotals(inputs, categories.associateBy { it.id }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanTotals(0, 0, 0))

    fun setMonth(month: String) {
        if (_month.value == month) return
        _month.value = month
        loadTargets(month)
    }

    private fun loadTargets(month: String) = viewModelScope.launch {
        val current = targetRepository.getMonth(month)
        if (current.isNotEmpty()) {
            _inputs.value = current.associate { it.categoryId to Money.toMajorInput(it.amount) }
            _banner.value = PrefillBanner.NONE
            return@launch
        }
        val previous = targetRepository.getMonth(YearMonth.parse(month).minusMonths(1).toString())
        if (previous.isNotEmpty()) {
            _inputs.value = previous.associate { it.categoryId to Money.toMajorInput(it.amount) }
            _banner.value = PrefillBanner.CARRIED_FORWARD
        } else {
            _inputs.value = emptyMap()
            _banner.value = PrefillBanner.FIRST_TIME
        }
    }

    fun onInputChange(categoryId: Long, text: String) {
        _inputs.value = _inputs.value + (categoryId to text)
    }

    /** Bulk upsert all live categories' targets for the month; blank/invalid clears (§F3.3/§3.5). */
    fun save() = viewModelScope.launch {
        val month = _month.value ?: return@launch
        val amounts = sections.value.flatMap { it.categories }
            .associate { it.id to Money.parseTargetToMinor(_inputs.value[it.id].orEmpty()) }
        targetRepository.bulkSave(month, amounts)
        _banner.value = PrefillBanner.NONE
    }
}
