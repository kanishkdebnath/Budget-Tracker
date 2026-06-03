package com.example.budgettracker.ui.screens.plan

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanLogicTest {

    private fun cat(id: Long, kind: Kind) =
        Category(id = id, groupId = 1, name = "C$id", kind = kind, order = 0, createdAt = 0, updatedAt = 0)

    private val cats = mapOf(1L to cat(1, Kind.INCOME), 2L to cat(2, Kind.EXPENSE), 3L to cat(3, Kind.EXPENSE))

    @Test fun totalsSumByKind() {
        val totals = computeTargetTotals(mapOf(1L to "5000", 2L to "2000", 3L to "1,500"), cats)
        assertEquals(500_000L, totals.income)
        assertEquals(350_000L, totals.expense)
        assertEquals(150_000L, totals.net)
    }

    @Test fun blankAndInvalidInputsCountAsZero() {
        val totals = computeTargetTotals(mapOf(1L to "", 2L to "abc", 3L to "1000"), cats)
        assertEquals(0L, totals.income)
        assertEquals(100_000L, totals.expense)
    }

    @Test fun parseTargetAllowsZeroRejectsBlankAndNegative() {
        assertEquals(0L, Money.parseTargetToMinor("0"))
        assertEquals(150_000L, Money.parseTargetToMinor("1500"))
        assertEquals(null, Money.parseTargetToMinor(""))
        assertEquals(null, Money.parseTargetToMinor("-5"))
    }
}
