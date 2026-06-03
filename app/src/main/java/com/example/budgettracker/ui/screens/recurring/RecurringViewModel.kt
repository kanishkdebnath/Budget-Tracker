package com.example.budgettracker.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.OpResult
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.RecurringTemplate
import com.example.budgettracker.data.repository.CategoryRepository
import com.example.budgettracker.data.repository.PreferencesRepository
import com.example.budgettracker.data.repository.RecurringRepository
import com.example.budgettracker.domain.time.MonthUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId

enum class RecurringState { APPLIED, ACTIONABLE, INACTIVE }

data class RecurringRow(val template: RecurringTemplate, val categoryName: String, val state: RecurringState)

data class RecurringSections(val active: List<RecurringRow>, val inactive: List<RecurringRow>, val dueCount: Int)

fun classifyRecurring(template: RecurringTemplate, currentMonth: String): RecurringState = when {
    !template.active -> RecurringState.INACTIVE
    template.lastRunMonth == currentMonth -> RecurringState.APPLIED
    else -> RecurringState.ACTIONABLE
}

/** Pure: split templates into active (applied-first, then actionable, by createdAt) + inactive (§F5.2/§5.5). */
fun buildRecurringRows(
    templates: List<RecurringTemplate>,
    categoriesById: Map<Long, Category>,
    currentMonth: String,
): RecurringSections {
    val rows = templates.map {
        RecurringRow(it, categoriesById[it.categoryId]?.name ?: "—", classifyRecurring(it, currentMonth))
    }
    val active = rows.filter { it.state != RecurringState.INACTIVE }
        .sortedWith(compareBy({ if (it.state == RecurringState.APPLIED) 0 else 1 }, { it.template.createdAt }))
    val inactive = rows.filter { it.state == RecurringState.INACTIVE }.sortedBy { it.template.createdAt }
    return RecurringSections(active, inactive, active.count { it.state == RecurringState.ACTIONABLE })
}

class RecurringViewModel(
    private val recurringRepository: RecurringRepository,
    private val categoryRepository: CategoryRepository,
    preferencesRepository: PreferencesRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val currentMonth = MonthUtils.monthOf(now(), zoneId)

    val currency: StateFlow<String> = preferencesRepository.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "INR")

    val liveCategories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sections: StateFlow<RecurringSections> = combine(
        recurringRepository.observeAll(),
        categoryRepository.observeCategories(includeArchived = true),
    ) { templates, categories -> buildRecurringRows(templates, categories.associateBy { it.id }, currentMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringSections(emptyList(), emptyList(), 0))

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    fun consumeMessage() { _message.value = null }

    fun apply(id: Long) = viewModelScope.launch {
        when (val result = recurringRepository.apply(id, currentMonth, zoneId)) {
            is OpResult.Failure -> _message.value = result.reason
            is OpResult.Success -> _message.value = "Applied to ${MonthUtils.monthLabel(currentMonth)}"
        }
    }

    fun create(label: String, categoryId: Long, amount: Long, dayOfMonth: Int, active: Boolean) =
        viewModelScope.launch { recurringRepository.create(label, categoryId, amount, dayOfMonth, active) }

    fun update(id: Long, label: String, categoryId: Long, amount: Long, dayOfMonth: Int, active: Boolean) =
        viewModelScope.launch { recurringRepository.update(id, label, categoryId, amount, dayOfMonth, active) }

    fun toggleActive(id: Long, active: Boolean) =
        viewModelScope.launch { recurringRepository.setActive(id, active) }

    fun delete(id: Long) = viewModelScope.launch { recurringRepository.delete(id) }
}
