package com.example.budgettracker.data.db

import com.example.budgettracker.data.entity.Kind

/** First-launch seed content (PRODUCT_SPEC §6.7). Group/category order follows list order. */
object SeedData {

    data class SeedCategory(val name: String, val kind: Kind)
    data class SeedGroup(val name: String, val color: String, val categories: List<SeedCategory>)

    val GROUPS: List<SeedGroup> = listOf(
        SeedGroup("Income", "#10b981", listOf(SeedCategory("Salary", Kind.INCOME))),
        SeedGroup(
            "Bills", "#ef4444",
            listOf(SeedCategory("Rent", Kind.EXPENSE), SeedCategory("Electricity", Kind.EXPENSE)),
        ),
        SeedGroup(
            "Household", "#f59e0b",
            listOf(SeedCategory("Groceries", Kind.EXPENSE), SeedCategory("Transport", Kind.EXPENSE)),
        ),
        SeedGroup("Debt", "#dc2626", emptyList()),
        SeedGroup("Leisure", "#8b5cf6", listOf(SeedCategory("Dining", Kind.EXPENSE))),
        SeedGroup("Savings", "#0ea5e9", emptyList()),
        SeedGroup("Other", "#64748b", emptyList()),
    )
}
