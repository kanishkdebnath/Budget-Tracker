package com.example.budgettracker.export

import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Writes the 3-sheet workbook (PRODUCT_SPEC §8.1) with FastExcel. Pure JVM — Android-safe. */
object ExcelExporter {

    private val DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

    fun write(bundle: ExportBundle, zone: ZoneId, out: OutputStream) {
        val workbook = Workbook(out, "Budget Tracker", "1.0")
        writeTransactions(workbook, bundle, zone)
        writeTargets(workbook, bundle)
        writeRecurring(workbook, bundle)
        workbook.finish()
    }

    private fun writeTransactions(workbook: Workbook, bundle: ExportBundle, zone: ZoneId) {
        val sheet = workbook.newWorksheet("Transactions")
        header(sheet, listOf("Date", "Group", "Category", "Kind", "Amount", "Description"))
        var r = 1
        for (row in bundle.transactions) {
            sheet.value(r, 0, Instant.ofEpochMilli(row.date).atZone(zone).toLocalDate().format(DATE))
            sheet.value(r, 1, row.group)
            sheet.value(r, 2, row.category)
            sheet.value(r, 3, kindLabel(row.kind))
            amount(sheet, r, 4, row.amount, bundle.currency)
            sheet.value(r, 5, row.description ?: "")
            r++
        }
        val income = bundle.transactions.filter { it.kind == Kind.INCOME }.sumOf { it.amount }
        val expense = bundle.transactions.filter { it.kind == Kind.EXPENSE }.sumOf { it.amount }
        r++
        boldLabel(sheet, r, 1, "Income"); amount(sheet, r, 4, income, bundle.currency); r++
        boldLabel(sheet, r, 1, "Expense"); amount(sheet, r, 4, expense, bundle.currency); r++
        boldLabel(sheet, r, 1, "Net"); amount(sheet, r, 4, income - expense, bundle.currency)
        sheet.freezePane(0, 1)
    }

    private fun writeTargets(workbook: Workbook, bundle: ExportBundle) {
        val sheet = workbook.newWorksheet("Targets")
        header(sheet, listOf("", "Group", "Category", "Kind", "Target", "Actual", "Delta"))
        var r = 1
        for (group in bundle.report.groups) {
            for (row in group.rows.filter { it.target != 0L || it.actual != 0L }) {
                sheet.value(r, 1, group.group.name)
                sheet.value(r, 2, row.category.name)
                sheet.value(r, 3, kindLabel(row.category.kind))
                amount(sheet, r, 4, row.target, bundle.currency)
                amount(sheet, r, 5, row.actual, bundle.currency)
                amount(sheet, r, 6, row.delta, bundle.currency)
                r++
            }
        }
        r++
        boldLabel(sheet, r, 1, "Income totals")
        amount(sheet, r, 4, bundle.report.targets.income, bundle.currency)
        amount(sheet, r, 5, bundle.report.actuals.income, bundle.currency); r++
        boldLabel(sheet, r, 1, "Expense totals")
        amount(sheet, r, 4, bundle.report.targets.expense, bundle.currency)
        amount(sheet, r, 5, bundle.report.actuals.expense, bundle.currency)
    }

    private fun writeRecurring(workbook: Workbook, bundle: ExportBundle) {
        val sheet = workbook.newWorksheet("Recurring")
        header(sheet, listOf("Label", "Day of Month", "Amount", "Applied?"))
        var r = 1
        for (row in bundle.recurring) {
            sheet.value(r, 0, row.label)
            sheet.value(r, 1, row.dayOfMonth)
            amount(sheet, r, 2, row.amount, bundle.currency)
            sheet.value(r, 3, if (row.applied) "Yes" else "No")
            r++
        }
    }

    private fun header(sheet: Worksheet, titles: List<String>) {
        titles.forEachIndexed { c, title ->
            sheet.value(0, c, title)
            sheet.style(0, c).bold().set()
        }
    }

    private fun boldLabel(sheet: Worksheet, r: Int, c: Int, text: String) {
        sheet.value(r, c, text)
        sheet.style(r, c).bold().set()
    }

    private fun amount(sheet: Worksheet, r: Int, c: Int, minor: Long, currency: String) {
        sheet.value(r, c, minor / 100.0)
        sheet.style(r, c).format(excelFormat(currency)).set()
    }

    private fun kindLabel(kind: Kind) = if (kind == Kind.INCOME) "Income" else "Expense"

    private fun excelFormat(currency: String): String {
        val symbol = Money.symbolOf(currency).trim()
        return when (currency) {
            "INR" -> "\"$symbol\"#,##,##0.00"
            "JPY" -> "\"$symbol\"#,##0"
            else -> "\"$symbol\"#,##0.00"
        }
    }
}
