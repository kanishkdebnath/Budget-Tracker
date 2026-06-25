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

/** Single-sheet Excel transaction journal for the Log export. */
object LogExcelExporter {

    private val DATE_COL = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    private val DAY_HEADER_FMT = DateTimeFormatter.ofPattern("MMM d — EEE", Locale.ENGLISH)

    // Hex color strings (no #) for FastExcel fill/font color methods.
    private const val NAVY = "0D2736"
    private const val WHITE = "FFFFFF"
    private const val DAY_BG = "E8EDF0"
    private const val INCOME_BG = "E8F5E9"
    private const val EXPENSE_BG = "FEECEC"
    private const val GREEN_FONT = "2E7D32"
    private const val RED_FONT = "C62828"

    fun write(bundle: LogExportBundle, zone: ZoneId, out: OutputStream) {
        val workbook = Workbook(out, "Budget Tracker", "1.0")
        val sheet = workbook.newWorksheet("Log")
        writeHeader(sheet)

        val byDay = bundle.transactions
            .groupBy { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }
            .entries
            .sortedBy { it.key }

        var r = 1
        var totalIncome = 0L
        var totalExpense = 0L

        for ((date, rows) in byDay) {
            val dayNet = rows.sumOf { if (it.kind == Kind.INCOME) it.amount else -it.amount }
            writeDayHeader(sheet, r, date.format(DAY_HEADER_FMT), dayNet, bundle.currency)
            r++
            for (row in rows) {
                writeRow(sheet, r, row, zone, bundle.currency)
                if (row.kind == Kind.INCOME) totalIncome += row.amount else totalExpense += row.amount
                r++
            }
        }

        writeSummary(sheet, r + 2, totalIncome, totalExpense, bundle.currency)
        sheet.freezePane(0, 1)
        workbook.finish()
    }

    private fun writeHeader(sheet: Worksheet) {
        listOf("Date", "Group", "Category", "Kind", "Amount", "Description").forEachIndexed { c, title ->
            sheet.value(0, c, title)
            sheet.style(0, c).bold().fontColor(WHITE).fillColor(NAVY).set()
        }
    }

    private fun writeDayHeader(sheet: Worksheet, r: Int, label: String, dayNet: Long, currency: String) {
        sheet.value(r, 0, label)
        sheet.style(r, 0).bold().fillColor(DAY_BG).set()
        for (c in 1..3) sheet.style(r, c).fillColor(DAY_BG).set()
        sheet.value(r, 4, dayNet / 100.0)
        val netFont = if (dayNet >= 0) GREEN_FONT else RED_FONT
        sheet.style(r, 4).bold().fontColor(netFont).fillColor(DAY_BG).format(excelFormat(currency)).set()
        sheet.style(r, 5).fillColor(DAY_BG).set()
    }

    private fun writeRow(sheet: Worksheet, r: Int, row: ExportTxnRow, zone: ZoneId, currency: String) {
        val bg = if (row.kind == Kind.INCOME) INCOME_BG else EXPENSE_BG
        sheet.value(r, 0, Instant.ofEpochMilli(row.date).atZone(zone).toLocalDate().format(DATE_COL))
        sheet.value(r, 1, row.group)
        sheet.value(r, 2, row.category)
        sheet.value(r, 3, if (row.kind == Kind.INCOME) "Income" else "Expense")
        sheet.value(r, 4, row.amount / 100.0)
        sheet.value(r, 5, row.description ?: "")
        for (c in 0..3) sheet.style(r, c).fillColor(bg).set()
        sheet.style(r, 4).fillColor(bg).format(excelFormat(currency)).set()
        sheet.style(r, 5).fillColor(bg).set()
    }

    private fun writeSummary(sheet: Worksheet, r: Int, income: Long, expense: Long, currency: String) {
        val net = income - expense
        sheet.value(r, 1, "Income")
        sheet.style(r, 1).bold().fontColor(GREEN_FONT).set()
        sheet.value(r, 4, income / 100.0)
        sheet.style(r, 4).bold().fontColor(GREEN_FONT).format(excelFormat(currency)).set()

        sheet.value(r + 1, 1, "Expense")
        sheet.style(r + 1, 1).bold().fontColor(RED_FONT).set()
        sheet.value(r + 1, 4, expense / 100.0)
        sheet.style(r + 1, 4).bold().fontColor(RED_FONT).format(excelFormat(currency)).set()

        sheet.value(r + 2, 1, "Net")
        sheet.style(r + 2, 1).bold().set()
        sheet.value(r + 2, 4, net / 100.0)
        sheet.style(r + 2, 4).bold().format(excelFormat(currency)).set()
    }

    private fun excelFormat(currency: String): String {
        val symbol = Money.symbolOf(currency).trim()
        return when (currency) {
            "INR" -> "\"$symbol\"#,##,##0.00"
            "JPY" -> "\"$symbol\"#,##0"
            else -> "\"$symbol\"#,##0.00"
        }
    }
}
