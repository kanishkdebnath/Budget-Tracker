package com.example.budgettracker.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.domain.time.MonthUtils
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Draws a date-section transaction journal to a PDF via the native PdfDocument. */
object LogPdfExporter {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40f
    private const val BOTTOM = PAGE_H - 40f

    fun write(bundle: LogExportBundle, zone: ZoneId, generatedAtMillis: Long, out: OutputStream) {
        val doc = PdfDocument()
        Renderer(doc, bundle, zone, generatedAtMillis).run()
        doc.writeTo(out)
        doc.close()
    }

    private class Renderer(
        val doc: PdfDocument,
        val bundle: LogExportBundle,
        val zone: ZoneId,
        val generatedAt: Long,
    ) {
        private val DAY_FMT = DateTimeFormatter.ofPattern("MMM d  ·  EEE", Locale.ENGLISH).withZone(zone)
        private val TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH)

        private val titlePaint = paint(18f, Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        private val headingPaint = paint(12f, Typeface.DEFAULT_BOLD)
        private val bodyPaint = paint(11f, Typeface.DEFAULT)
        private val subtitlePaint = paint(9f, Typeface.DEFAULT).apply { color = GRAY }
        private val labelPaint = paint(9f, Typeface.DEFAULT).apply { color = GRAY }
        private val amountPaint = paint(11f, Typeface.DEFAULT).apply { textAlign = Paint.Align.RIGHT }
        private val amountHeadingPaint = paint(12f, Typeface.DEFAULT_BOLD).apply { textAlign = Paint.Align.RIGHT }
        private val summaryHeadingPaint = paint(13f, Typeface.DEFAULT_BOLD)

        private lateinit var page: PdfDocument.Page
        private lateinit var canvas: Canvas
        private var y = 0f
        private var pageNum = 0
        private var summaryDrawn = false

        fun run() {
            newPage()
            drawSummary()
            val byDay = bundle.transactions
                .groupBy { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }
                .entries
                .sortedByDescending { it.key }
            for ((date, rows) in byDay) {
                drawDaySection(date.atStartOfDay(zone).toInstant().toEpochMilli(), rows)
            }
            footerAndFinish()
        }

        private fun newPage() {
            pageNum++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNum).create())
            canvas = page.canvas
            canvas.drawText("Budget Tracker", MARGIN, MARGIN + 14f, titlePaint)
            titlePaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Log · ${MonthUtils.monthLabel(bundle.month)}", PAGE_W - MARGIN, MARGIN + 14f, titlePaint)
            titlePaint.textAlign = Paint.Align.LEFT
            canvas.drawLine(MARGIN, MARGIN + 26f, PAGE_W - MARGIN, MARGIN + 26f, labelPaint)
            y = MARGIN + 50f
            summaryDrawn = false
        }

        private fun footerAndFinish() {
            val timestamp = Instant.ofEpochMilli(generatedAt).atZone(zone).format(TS_FMT)
            canvas.drawText("Generated $timestamp", MARGIN, BOTTOM + 16f, labelPaint)
            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Page $pageNum", PAGE_W - MARGIN, BOTTOM + 16f, labelPaint)
            labelPaint.textAlign = Paint.Align.LEFT
            doc.finishPage(page)
        }

        private fun ensure(space: Float) {
            if (y + space > BOTTOM) {
                footerAndFinish()
                newPage()
            }
        }

        private fun drawSummary() {
            ensure(56f)
            val income = bundle.transactions.filter { it.kind == Kind.INCOME }.sumOf { it.amount }
            val expense = bundle.transactions.filter { it.kind == Kind.EXPENSE }.sumOf { it.amount }
            val net = income - expense
            val cellW = (PAGE_W - 2 * MARGIN) / 3
            listOf("Income" to income, "Expense" to expense, "Net" to net).forEachIndexed { i, (lab, value) ->
                val x = MARGIN + i * cellW
                canvas.drawText(lab.uppercase(), x, y, labelPaint)
                canvas.drawText(Money.format(value, bundle.currency), x, y + 18f, summaryHeadingPaint)
            }
            y += 48f
            summaryDrawn = true
        }

        private fun drawDaySection(dayMillis: Long, rows: List<ExportTxnRow>) {
            ensure(32f)
            val dayLabel = DAY_FMT.format(Instant.ofEpochMilli(dayMillis))
            val dayNet = rows.sumOf { if (it.kind == Kind.INCOME) it.amount else -it.amount }

            // Day header: label left, net right.
            canvas.drawText(dayLabel, MARGIN, y, headingPaint)
            amountHeadingPaint.color = if (dayNet >= 0) GREEN else RED
            canvas.drawText(Money.format(dayNet, bundle.currency), PAGE_W - MARGIN, y, amountHeadingPaint)
            amountHeadingPaint.color = INK

            // Hairline rule below day label.
            val ruleY = y + 4f
            canvas.drawLine(MARGIN, ruleY, PAGE_W - MARGIN, ruleY, labelPaint)
            y += 22f

            for (row in rows) {
                ensure(26f)
                val title = if (!row.description.isNullOrBlank()) row.description else row.category
                canvas.drawText(title, MARGIN, y, bodyPaint)
                amountPaint.color = if (row.kind == Kind.INCOME) GREEN else RED
                canvas.drawText(Money.format(row.amount, bundle.currency), PAGE_W - MARGIN, y, amountPaint)
                amountPaint.color = INK
                y += 14f

                canvas.drawText("${row.group} · ${row.category}", MARGIN + 8f, y, subtitlePaint)
                y += 12f
            }
            y += 10f
        }

        private fun paint(size: Float, tf: Typeface) =
            Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size; typeface = tf; color = INK }

        companion object {
            private const val INK = 0xFF0E141A.toInt()
            private const val GRAY = 0xFF6F7E87.toInt()
            private const val GREEN = 0xFF2E7D32.toInt()
            private const val RED = 0xFFC62828.toInt()
        }
    }
}
