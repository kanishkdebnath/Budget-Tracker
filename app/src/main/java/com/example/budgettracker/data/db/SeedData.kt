package com.example.budgettracker.data.db

import com.example.budgettracker.data.entity.Kind

/** First-launch seed content (PRODUCT_SPEC §6.7). Group/category order follows list order. */
object SeedData {

    data class SeedCategory(val name: String, val kind: Kind, val icon: String? = null)
    data class SeedGroup(val name: String, val color: String, val categories: List<SeedCategory>)

    val GROUPS: List<SeedGroup> = listOf(
        SeedGroup("Income", "#10b981", listOf(SeedCategory("Salary", Kind.INCOME, "payments"))),
        SeedGroup(
            "Bills", "#ef4444",
            listOf(
                SeedCategory("Rent", Kind.EXPENSE, "home"),
                SeedCategory("Electricity", Kind.EXPENSE, "bolt"),
            ),
        ),
        SeedGroup(
            "Household", "#f59e0b",
            listOf(
                SeedCategory("Groceries", Kind.EXPENSE, "shopping_cart"),
                SeedCategory("Transport", Kind.EXPENSE, "directions_car"),
            ),
        ),
        SeedGroup("Debt", "#dc2626", emptyList()),
        SeedGroup("Leisure", "#8b5cf6", listOf(SeedCategory("Dining", Kind.EXPENSE, "restaurant"))),
        SeedGroup("Savings", "#0ea5e9", emptyList()),
        SeedGroup("Other", "#64748b", emptyList()),
    )
}
