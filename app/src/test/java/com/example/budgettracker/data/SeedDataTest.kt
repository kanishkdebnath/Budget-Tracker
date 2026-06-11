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

    @Test
    fun seedCategoriesCarryIcons() {
        // Assert every seed icon key — these must stay in lock-step with MIGRATION_1_2's backfill,
        // so a typo in any one is a real (silent, runtime-only) bug worth catching here.
        val iconsByName = SeedData.GROUPS.flatMap { it.categories }.associate { it.name to it.icon }
        assertEquals(
            mapOf(
                "Salary" to "payments",
                "Rent" to "home",
                "Electricity" to "bolt",
                "Groceries" to "shopping_cart",
                "Transport" to "directions_car",
                "Dining" to "restaurant",
            ),
            iconsByName,
        )
    }
}
