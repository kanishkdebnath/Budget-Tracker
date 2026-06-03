package com.example.budgettracker.domain.report

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.Target
import com.example.budgettracker.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportTest {

    private val groups = listOf(
        CategoryGroup(id = 1, name = "Income", color = "#10b981", order = 0, createdAt = 0, updatedAt = 0),
        CategoryGroup(id = 2, name = "Bills", color = "#ef4444", order = 1, createdAt = 0, updatedAt = 0),
    )
    private val cats = listOf(
        Category(id = 10, groupId = 1, name = "Salary", kind = Kind.INCOME, order = 0, createdAt = 0, updatedAt = 0),
        Category(id = 11, groupId = 2, name = "Rent", kind = Kind.EXPENSE, order = 0, createdAt = 0, updatedAt = 0),
        Category(id = 12, groupId = 2, name = "Electricity", kind = Kind.EXPENSE, order = 1, createdAt = 0, updatedAt = 0),
    )
    private val targets = listOf(
        Target(id = 1, categoryId = 10, month = "2026-06", amount = 500_000, createdAt = 0, updatedAt = 0),
        Target(id = 2, categoryId = 11, month = "2026-06", amount = 300_000, createdAt = 0, updatedAt = 0),
    )
    private fun txn(id: Long, categoryId: Long, amount: Long) =
        TransactionEntity(id = id, date = 0, categoryId = categoryId, amount = amount, createdAt = 0, updatedAt = 0)
    private val txns = listOf(txn(1, 10, 500_000), txn(2, 11, 350_000))

    @Test fun aggregatesActualsTargetsDeltasAndTotals() {
        val report = aggregateReport(txns, targets, cats, groups)
        assertEquals(2, report.groups.size)

        val income = report.groups.first { it.group.name == "Income" }
        assertEquals(Kind.INCOME, income.kind)
        assertEquals(500_000L, income.actualSubtotal)

        val bills = report.groups.first { it.group.name == "Bills" }
        assertEquals(Kind.EXPENSE, bills.kind)
        assertEquals(350_000L, bills.actualSubtotal)
        assertEquals(300_000L, bills.targetSubtotal)
        assertEquals(50_000L, bills.rows.first { it.category.name == "Rent" }.delta)

        assertEquals(500_000L, report.actuals.income)
        assertEquals(350_000L, report.actuals.expense)
        assertEquals(150_000L, report.actuals.net)
        assertEquals(300_000L, report.targets.expense)
    }

    @Test fun narrativeReportsOverByPercentAndBiggestOverage() {
        val report = aggregateReport(txns, targets, cats, groups)
        val narrative = generateNarrative("2026-06", report, "INR", hasTransactions = true)
        assertTrue(narrative, narrative.contains("over by ₹500 (17%)"))
        assertTrue(narrative, narrative.contains("Biggest overage: Rent (₹3,500 vs ₹3,000)"))
    }

    @Test fun narrativeWithNoTransactions() {
        val report = aggregateReport(emptyList(), emptyList(), cats, groups)
        assertEquals("In June 2026, no transactions were logged.", generateNarrative("2026-06", report, "INR", false))
    }

    @Test fun narrativeWithNoPlan() {
        val report = aggregateReport(txns, emptyList(), cats, groups)
        assertEquals(
            "In June 2026, you spent ₹3,500 — no plan was set for this month.",
            generateNarrative("2026-06", report, "INR", true),
        )
    }
}
