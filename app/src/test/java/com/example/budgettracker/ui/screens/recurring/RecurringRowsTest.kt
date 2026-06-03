package com.example.budgettracker.ui.screens.recurring

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.RecurringTemplate
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurringRowsTest {

    private fun tmpl(id: Long, active: Boolean, lastRun: String?, created: Long) =
        RecurringTemplate(
            id = id, label = "T$id", categoryId = 1, amount = 1000, dayOfMonth = 1,
            lastRunMonth = lastRun, active = active, createdAt = created, updatedAt = 0,
        )

    private val cats = mapOf(1L to Category(id = 1, groupId = 1, name = "Salary", kind = Kind.INCOME, order = 0, createdAt = 0, updatedAt = 0))

    @Test fun classifiesAndSortsActiveAppliedFirstThenInactive() {
        val templates = listOf(
            tmpl(1, active = true, lastRun = "2026-06", created = 1),  // APPLIED
            tmpl(2, active = true, lastRun = null, created = 2),       // ACTIONABLE
            tmpl(3, active = false, lastRun = null, created = 3),      // INACTIVE
            tmpl(4, active = true, lastRun = "2026-05", created = 4),  // ACTIONABLE (stale lastRun)
        )
        val sections = buildRecurringRows(templates, cats, "2026-06")

        assertEquals(listOf(1L, 2L, 4L), sections.active.map { it.template.id })
        assertEquals(RecurringState.APPLIED, sections.active[0].state)
        assertEquals(RecurringState.ACTIONABLE, sections.active[1].state)
        assertEquals(listOf(3L), sections.inactive.map { it.template.id })
        assertEquals(2, sections.dueCount)
        assertEquals("Salary", sections.active[0].categoryName)
    }
}
