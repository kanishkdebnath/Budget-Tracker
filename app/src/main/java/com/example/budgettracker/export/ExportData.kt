package com.example.budgettracker.export

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.RecurringTemplate
import com.example.budgettracker.data.entity.TransactionEntity
import com.example.budgettracker.domain.report.ReportData

data class ExportTxnRow(
    val date: Long,
    val group: String,
    val category: String,
    val kind: Kind,
    val amount: Long,
    val description: String?,
)

data class ExportRecurringRow(val label: String, val dayOfMonth: Int, val amount: Long, val applied: Boolean)

/** Focused transaction-journal bundle for the Log export (Excel + PDF). */
data class LogExportBundle(
    val month: String,
    val currency: String,
    val transactions: List<ExportTxnRow>,
)

/** Everything the Excel/PDF exporters need for one month. */
data class ExportBundle(
    val month: String,
    val currency: String,
    val narrative: String,
    val report: ReportData,
    val transactions: List<ExportTxnRow>,
    val recurring: List<ExportRecurringRow>,
)

/** Transactions sorted date ASC for the Excel log sheet (§8.1 Sheet 1). */
fun buildExportTxnRows(
    transactions: List<TransactionEntity>,
    categoriesById: Map<Long, Category>,
    groupsById: Map<Long, CategoryGroup>,
): List<ExportTxnRow> = transactions
    .sortedBy { it.date }
    .mapNotNull { t ->
        val category = categoriesById[t.categoryId] ?: return@mapNotNull null
        ExportTxnRow(t.date, groupsById[category.groupId]?.name ?: "—", category.name, category.kind, t.amount, t.description)
    }

/** Recurring rows sorted by day ASC; applied = lastRunMonth == month (§8.1 Sheet 3). */
fun buildExportRecurringRows(templates: List<RecurringTemplate>, month: String): List<ExportRecurringRow> =
    templates.sortedBy { it.dayOfMonth }
        .map { ExportRecurringRow(it.label, it.dayOfMonth, it.amount, it.lastRunMonth == month) }
