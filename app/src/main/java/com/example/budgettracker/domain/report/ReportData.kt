package com.example.budgettracker.domain.report

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.Target
import com.example.budgettracker.data.entity.TransactionEntity

data class CategoryReportRow(val category: Category, val target: Long, val actual: Long) {
    val delta: Long get() = actual - target
}

data class GroupReport(val group: CategoryGroup, val kind: Kind, val rows: List<CategoryReportRow>) {
    val targetSubtotal: Long get() = rows.sumOf { it.target }
    val actualSubtotal: Long get() = rows.sumOf { it.actual }
}

data class ReportTotals(val income: Long, val expense: Long) {
    val net: Long get() = income - expense
}

data class ReportData(val groups: List<GroupReport>, val actuals: ReportTotals, val targets: ReportTotals)

/** §7.2 step 3: income if all categories are income, else expense (mixed/empty → expense). */
fun inferGroupKind(categories: List<Category>): Kind =
    if (categories.isNotEmpty() && categories.all { it.kind == Kind.INCOME }) Kind.INCOME else Kind.EXPENSE

/**
 * Pure month report aggregation (§7.2). Shows live categories plus archived categories that still
 * have activity (their past transactions remain visible, §F2.4). Totals are summed by inferred group
 * kind over live groups.
 */
fun aggregateReport(
    transactions: List<TransactionEntity>,
    targets: List<Target>,
    categories: List<Category>,
    groups: List<CategoryGroup>,
): ReportData {
    val actualByCat = transactions.groupBy { it.categoryId }.mapValues { (_, txns) -> txns.sumOf { it.amount } }
    val targetByCat = targets.associate { it.categoryId to it.amount }
    val catsByGroup = categories.groupBy { it.groupId }

    val groupReports = groups.filter { !it.archived }.sortedBy { it.order }.mapNotNull { group ->
        val groupCats = catsByGroup[group.id] ?: emptyList()
        val shown = groupCats
            .filter { !it.archived || it.id in actualByCat || it.id in targetByCat }
            .sortedBy { it.order }
        if (shown.isEmpty()) return@mapNotNull null
        val liveCats = groupCats.filter { !it.archived }
        val kind = inferGroupKind(liveCats.ifEmpty { shown })
        val rows = shown.map { CategoryReportRow(it, targetByCat[it.id] ?: 0L, actualByCat[it.id] ?: 0L) }
        GroupReport(group, kind, rows)
    }

    fun totals(selector: (GroupReport) -> Long) = ReportTotals(
        income = groupReports.filter { it.kind == Kind.INCOME }.sumOf(selector),
        expense = groupReports.filter { it.kind == Kind.EXPENSE }.sumOf(selector),
    )

    return ReportData(groupReports, totals { it.actualSubtotal }, totals { it.targetSubtotal })
}
