package com.example.budgettracker.ui.screens.log

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class LogStateTest {

    private val utc = ZoneId.of("UTC")

    private fun cat(id: Long, kind: Kind) =
        Category(id = id, groupId = 1, name = "C$id", kind = kind, order = 0, createdAt = 0, updatedAt = 0)

    private fun txn(id: Long, categoryId: Long, amount: Long, dateIso: String, created: Long = 0) =
        TransactionEntity(id = id, date = Instant.parse(dateIso).toEpochMilli(), categoryId = categoryId, amount = amount, createdAt = created, updatedAt = 0)

    private val cats = mapOf(1L to cat(1, Kind.INCOME), 2L to cat(2, Kind.EXPENSE))
    private val groupColors = mapOf(1L to "#10b981")

    @Test fun totalsAndNetReflectFullMonth() {
        val txns = listOf(
            txn(1, 1, 500_000, "2026-06-01T10:00:00Z"),
            txn(2, 2, 200_000, "2026-06-02T10:00:00Z"),
            txn(3, 2, 100_000, "2026-06-02T11:00:00Z"),
        )
        val state = buildLogState(txns, cats, groupColors, TxnFilter.ALL, utc)
        assertEquals(500_000L, state.income)
        assertEquals(300_000L, state.expense)
        assertEquals(200_000L, state.net)
        assertEquals(1, state.incomeCount)
        assertEquals(2, state.expenseCount)
    }

    @Test fun sectionsGroupByDateDescendingWithDayNet() {
        val txns = listOf(
            txn(1, 1, 500_000, "2026-06-01T10:00:00Z"),
            txn(2, 2, 200_000, "2026-06-02T10:00:00Z"),
        )
        val state = buildLogState(txns, cats, groupColors, TxnFilter.ALL, utc)
        assertEquals(2, state.sections.size)
        assertEquals(-200_000L, state.sections[0].dayNet) // June 2 first
        assertEquals(500_000L, state.sections[1].dayNet)
    }

    @Test fun incomeFilterNarrowsSectionsButNotTotals() {
        val txns = listOf(
            txn(1, 1, 500_000, "2026-06-01T10:00:00Z"),
            txn(2, 2, 200_000, "2026-06-02T10:00:00Z"),
        )
        val state = buildLogState(txns, cats, groupColors, TxnFilter.INCOME, utc)
        assertEquals(500_000L, state.income)
        assertEquals(200_000L, state.expense)
        assertEquals(1, state.sections.size)
        assertEquals("C1", state.sections[0].rows.single().categoryName)
    }
}
