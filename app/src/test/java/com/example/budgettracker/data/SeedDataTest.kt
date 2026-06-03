package com.example.budgettracker.data

import com.example.budgettracker.data.db.SeedData
import com.example.budgettracker.data.entity.Kind
import org.junit.Assert.assertEquals
import org.junit.Test

class SeedDataTest {

    @Test fun seedsSevenGroupsInSpecOrder() {
        assertEquals(
            listOf("Income", "Bills", "Household", "Debt", "Leisure", "Savings", "Other"),
            SeedData.GROUPS.map { it.name },
        )
    }

    @Test fun seedsSixCategories() {
        assertEquals(6, SeedData.GROUPS.sumOf { it.categories.size })
    }

    @Test fun incomeGroupHasSalaryAsIncome() {
        val income = SeedData.GROUPS.single { it.name == "Income" }
        val salary = income.categories.single()
        assertEquals("Salary", salary.name)
        assertEquals(Kind.INCOME, salary.kind)
    }

    @Test fun allNonIncomeSeedCategoriesAreExpense() {
        val nonIncome = SeedData.GROUPS.filter { it.name != "Income" }.flatMap { it.categories }
        assertEquals(emptyList<Kind>(), nonIncome.map { it.kind }.filter { it != Kind.EXPENSE })
    }
}
