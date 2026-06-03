package com.example.budgettracker.ui.screens.categories

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoriesSectionsTest {

    private fun group(id: Long, name: String, order: Int, archived: Boolean = false) =
        CategoryGroup(id = id, name = name, color = "#000000", order = order, archived = archived, createdAt = 0, updatedAt = 0)

    private fun cat(id: Long, groupId: Long, name: String, kind: Kind, order: Int, archived: Boolean = false) =
        Category(id = id, groupId = groupId, name = name, kind = kind, order = order, archived = archived, createdAt = 0, updatedAt = 0)

    private val groups = listOf(group(1, "Income", 0), group(2, "Bills", 1), group(3, "Old", 2, archived = true))
    private val cats = listOf(
        cat(10, 1, "Salary", Kind.INCOME, 0),
        cat(11, 2, "Rent", Kind.EXPENSE, 0),
        cat(12, 2, "Gone", Kind.EXPENSE, 1, archived = true),
    )

    @Test fun allShowsLiveGroupsWithLiveCategories() {
        val sections = buildSections(groups, cats, CategoryFilter.ALL)
        assertEquals(listOf("Income", "Bills"), sections.map { it.group.name })
        assertEquals(listOf("Salary"), sections[0].categories.map { it.name })
        assertEquals(listOf("Rent"), sections[1].categories.map { it.name }) // archived "Gone" excluded
    }

    @Test fun incomeFilterKeepsOnlyIncomeCategoriesAndNonEmptyGroups() {
        val sections = buildSections(groups, cats, CategoryFilter.INCOME)
        assertEquals(listOf("Income"), sections.map { it.group.name })
        assertEquals(listOf("Salary"), sections[0].categories.map { it.name })
    }

    @Test fun archivedFilterShowsArchivedCategoriesAndArchivedGroups() {
        val sections = buildSections(groups, cats, CategoryFilter.ARCHIVED)
        assertEquals(listOf("Bills", "Old"), sections.map { it.group.name })
        assertEquals(listOf("Gone"), sections[0].categories.map { it.name })
        assertEquals(emptyList<String>(), sections[1].categories.map { it.name })
    }
}
