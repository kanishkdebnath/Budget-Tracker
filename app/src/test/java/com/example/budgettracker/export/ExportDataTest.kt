package com.example.budgettracker.export

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.RecurringTemplate
import com.example.budgettracker.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportDataTest {

    private val groups = mapOf(2L to CategoryGroup(id = 2, name = "Bills", color = "#ef4444", order = 1, createdAt = 0, updatedAt = 0))
    private val cats = mapOf(11L to Category(id = 11, groupId = 2, name = "Rent", kind = Kind.EXPENSE, order = 0, createdAt = 0, updatedAt = 0))

    @Test fun txnRowsSortedByDateAscWithNames() {
        val txns = listOf(
            TransactionEntity(id = 1, date = 2000, categoryId = 11, amount = 100, createdAt = 0, updatedAt = 0),
            TransactionEntity(id = 2, date = 1000, categoryId = 11, amount = 200, createdAt = 0, updatedAt = 0),
        )
        val rows = buildExportTxnRows(txns, cats, groups)
        assertEquals(listOf(1000L, 2000L), rows.map { it.date })
        assertEquals("Bills", rows[0].group)
        assertEquals("Rent", rows[0].category)
    }

    @Test fun recurringRowsSortedByDayWithAppliedFlag() {
        val templates = listOf(
            RecurringTemplate(id = 1, label = "Rent", categoryId = 11, amount = 100, dayOfMonth = 5, lastRunMonth = "2026-06", active = true, createdAt = 0, updatedAt = 0),
            RecurringTemplate(id = 2, label = "Salary", categoryId = 11, amount = 200, dayOfMonth = 1, lastRunMonth = null, active = true, createdAt = 0, updatedAt = 0),
        )
        val rows = buildExportRecurringRows(templates, "2026-06")
        assertEquals(listOf(1, 5), rows.map { it.dayOfMonth })
        assertEquals("Salary", rows[0].label)
        assertFalse(rows[0].applied)
        assertTrue(rows[1].applied)
    }
}
