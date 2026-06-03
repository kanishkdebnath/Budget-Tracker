package com.example.budgettracker.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.domain.report.GroupReport
import com.example.budgettracker.domain.time.MonthUtils
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Draws the month report to a PDF via the native PdfDocument (PRODUCT_SPEC §8.2). */
object PdfExporter {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40f
    private const val BOTTOM = PAGE_H - 40f

    fun write(bundle: ExportBundle, zone: ZoneId, generatedAtMillis: Long, out: OutputStream) {
        val doc = PdfDocument()
        Renderer(doc, bundle, zone, generatedAtMillis).run()
        doc.writeTo(out)
        doc.close()
    }

    private class Renderer(
        val doc: PdfDocument,
        val bundle: ExportBundle,
        val zone: ZoneId,
        val generatedAt: Long,
    ) {
        private val title = paint(18f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        private val heading = paint(13f, Typeface.DEFAULT_BOLD)
        private val body = paint(11f, Typeface.DEFAULT)
        private val label = paint(9f, Typeface.DEFAULT).apply { color = GRAY }
        private val money = paint(11f, Typeface.DEFAULT).apply { textAlign = Paint.Align.RIGHT }
        private val moneyBold = paint(11f, Typeface.DEFAULT_BOLD).apply { textAlign = Paint.Align.RIGHT }

        private lateinit var page: PdfDocument.Page
        private lateinit var canvas: Canvas
        private var y = 0f
        private var pageNum = 0

        fun run() {
            newPage()
            summary()
            narrative()
            bundle.report.groups.forEach { groupTable(it) }
            footerAndFinish()
        }

        private fun newPage() {
            pageNum++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNum).create())
            canvas = page.canvas
            canvas.drawText("Budget Tracker", MARGIN, MARGIN + 14f, title)
            title.textAlign = Paint.Align.RIGHT
            canvas.drawText(MonthUtils.monthLabel(bundle.month), PAGE_W - MARGIN, MARGIN + 14f, title)
            title.textAlign = Paint.Align.LEFT
            canvas.drawLine(MARGIN, MARGIN + 26f, PAGE_W - MARGIN, MARGIN + 26f, label)
            y = MARGIN + 50f
        }

        private fun footerAndFinish() {
            val timestamp = Instant.ofEpochMilli(generatedAt).atZone(zone)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH))
            canvas.drawText("Generated $timestamp", MARGIN, BOTTOM + 16f, label)
            label.textAlign = Paint.Align.RIGHT
            canvas.drawText("Page $pageNum", PAGE_W - MARGIN, BOTTOM + 16f, label)
            label.textAlign = Paint.Align.LEFT
            doc.finishPage(page)
        }

        private fun ensure(space: Float) {
            if (y + space > BOTTOM) {
                footerAndFinish()
                newPage()
            }
        }

        private fun summary() {
            ensure(56f)
            val cellW = (PAGE_W - 2 * MARGIN) / 3
            val cells = listOf(
                "Income" to bundle.report.actuals.income,
                "Expense" to bundle.report.actuals.expense,
                "Net" to bundle.report.actuals.net,
            )
            val plans = listOf(bundle.report.targets.income, bundle.report.targets.expense, bundle.report.targets.net)
            cells.forEachIndexed { i, (lab, value) ->
                val x = MARGIN + i * cellW
                canvas.drawText(lab.uppercase(), x, y, label)
                canvas.drawText(Money.format(value, bundle.currency), x, y + 18f, heading)
                canvas.drawText("Plan ${Money.format(plans[i], bundle.currency)}", x, y + 32f, label)
            }
            y += 52f
        }

        private fun narrative() {
            ensure(30f)
            canvas.drawText("SUMMARY", MARGIN, y, label)
            y += 16f
            wrap(bundle.narrative, body, PAGE_W - 2 * MARGIN).forEach { line ->
                ensure(16f)
                canvas.drawText(line, MARGIN, y, body)
                y += 15f
            }
            y += 12f
        }

        private fun groupTable(group: GroupReport) {
            ensure(40f)
            canvas.drawText("${group.group.name}  (${kindLabel(group.kind)})", MARGIN, y, heading)
            y += 16f
            val deltaX = PAGE_W - MARGIN
            val actualX = deltaX - 95f
            val planX = actualX - 95f
            for (row in group.rows) {
                ensure(16f)
                canvas.drawText(row.category.name, MARGIN, y, body)
                canvas.drawText(Money.format(row.target, bundle.currency), planX, y, money)
                canvas.drawText(Money.format(row.actual, bundle.currency), actualX, y, money)
                val delta = if (row.delta > 0) "+${Money.format(row.delta, bundle.currency)}" else Money.format(row.delta, bundle.currency)
                canvas.drawText(delta, deltaX, y, money)
                y += 15f
            }
            ensure(16f)
            canvas.drawText("Subtotal", MARGIN, y, heading)
            canvas.drawText(Money.format(group.targetSubtotal, bundle.currency), planX, y, moneyBold)
            canvas.drawText(Money.format(group.actualSubtotal, bundle.currency), actualX, y, moneyBold)
            y += 24f
        }

        private fun kindLabel(kind: Kind) = if (kind == Kind.INCOME) "Income" else "Expense"

        private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
            val lines = mutableListOf<String>()
            var line = StringBuilder()
            for (word in text.split(" ")) {
                val candidate = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                    lines.add(line.toString())
                    line = StringBuilder(word)
                } else {
                    line = StringBuilder(candidate)
                }
            }
            if (line.isNotEmpty()) lines.add(line.toString())
            return lines
        }

        private fun paint(size: Float, tf: Typeface) =
            Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size; typeface = tf; color = INK }

        companion object {
            private const val INK = 0xFF0E141A.toInt()
            private const val GRAY = 0xFF6F7E87.toInt()
        }
    }
}
