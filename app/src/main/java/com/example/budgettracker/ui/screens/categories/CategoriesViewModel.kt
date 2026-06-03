package com.example.budgettracker.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.OpResult
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CategoryFilter(val label: String) {
    ALL("All"), INCOME("Income"), EXPENSE("Expense"), ARCHIVED("Archived")
}

/** A group header with the categories shown beneath it for the current filter. */
data class GroupSection(val group: CategoryGroup, val categories: List<Category>)

/**
 * Pure: derive the displayed sections for [filter] from all groups + categories, optionally narrowed
 * by a name [query].
 * - ALL: live groups (incl. empty) with their live categories.
 * - INCOME/EXPENSE: live groups having ≥1 live category of that kind, those categories only.
 * - ARCHIVED: archived categories grouped under their group, plus archived groups.
 *
 * A non-blank [query] (case-insensitive) keeps a group if its name matches (with all its categories)
 * or if any category name matches (those categories only).
 */
fun buildSections(
    groups: List<CategoryGroup>,
    categories: List<Category>,
    filter: CategoryFilter,
    query: String = "",
): List<GroupSection> {
    val byGroup = categories.groupBy { it.groupId }
    fun catsOf(groupId: Long) = (byGroup[groupId] ?: emptyList()).sortedBy { it.order }
    val sortedGroups = groups.sortedBy { it.order }

    val base = when (filter) {
        CategoryFilter.ALL ->
            sortedGroups.filter { !it.archived }
                .map { g -> GroupSection(g, catsOf(g.id).filter { !it.archived }) }

        CategoryFilter.INCOME, CategoryFilter.EXPENSE -> {
            val kind = if (filter == CategoryFilter.INCOME) Kind.INCOME else Kind.EXPENSE
            sortedGroups.filter { !it.archived }
                .map { g -> GroupSection(g, catsOf(g.id).filter { !it.archived && it.kind == kind }) }
                .filter { it.categories.isNotEmpty() }
        }

        CategoryFilter.ARCHIVED ->
            sortedGroups
                .map { g -> GroupSection(g, catsOf(g.id).filter { it.archived }) }
                .filter { it.group.archived || it.categories.isNotEmpty() }
    }

    val q = query.trim()
    if (q.isEmpty()) return base
    return base.mapNotNull { section ->
        val groupMatches = section.group.name.contains(q, ignoreCase = true)
        val cats = if (groupMatches) section.categories
        else section.categories.filter { it.name.contains(q, ignoreCase = true) }
        if (groupMatches || cats.isNotEmpty()) GroupSection(section.group, cats) else null
    }
}

class CategoriesViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _filter = MutableStateFlow(CategoryFilter.ALL)
    val filter: StateFlow<CategoryFilter> = _filter

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    val sections: StateFlow<List<GroupSection>> = combine(
        repository.observeGroups(includeArchived = true),
        repository.observeCategories(includeArchived = true),
        _filter,
        _query,
    ) { groups, categories, currentFilter, currentQuery ->
        buildSections(groups, categories, currentFilter, currentQuery)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Live groups for the category form's group picker (independent of the current filter). */
    val liveGroups: StateFlow<List<CategoryGroup>> = repository.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(filter: CategoryFilter) { _filter.value = filter }

    fun setQuery(value: String) { _query.value = value }

    fun consumeMessage() { _message.value = null }

    fun createGroup(name: String, color: String) = viewModelScope.launch {
        val order = repository.observeGroups().first().size
        report(repository.createGroup(name, color, order))
    }

    fun createCategory(groupId: Long, name: String, kind: Kind, color: String?) = viewModelScope.launch {
        val order = repository.observeCategories().first().count { it.groupId == groupId }
        report(repository.createCategory(groupId, name, kind, color, order))
    }

    fun reorderGroups(orderedIds: List<Long>) = viewModelScope.launch { repository.reorderGroups(orderedIds) }
    fun reorderCategories(orderedIds: List<Long>) = viewModelScope.launch { repository.reorderCategories(orderedIds) }

    fun updateGroup(group: CategoryGroup) = viewModelScope.launch { report(repository.updateGroup(group)) }
    fun updateCategory(category: Category) = viewModelScope.launch { report(repository.updateCategory(category)) }
    fun archiveGroup(groupId: Long) = viewModelScope.launch { report(repository.archiveGroup(groupId)) }
    fun archiveCategory(categoryId: Long) = viewModelScope.launch { report(repository.archiveCategory(categoryId)) }

    private fun report(result: OpResult) {
        if (result is OpResult.Failure) _message.value = result.reason
    }
}
