package com.example.budgettracker.export

import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.report.ReportData
import com.example.budgettracker.domain.report.ReportTotals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.ZoneId

class ExcelExporterTest {

    @Test fun producesValidXlsxZip() {
        val bundle = ExportBundle(
            month = "2026-06",
            currency = "INR",
            narrative = "test",
            report = ReportData(emptyList(), ReportTotals(0, 0), ReportTotals(0, 0)),
            transactions = listOf(ExportTxnRow(0L, "Bills", "Rent", Kind.EXPENSE, 50_000, "note")),
            recurring = listOf(ExportRecurringRow("Salary", 1, 500_000, applied = true)),
        )
        val out = ByteArrayOutputStream()
        ExcelExporter.write(bundle, ZoneId.of("UTC"), out)
        val bytes = out.toByteArray()

        assertTrue("xlsx should be non-trivial", bytes.size > 1000)
        // .xlsx is a zip archive → magic bytes "PK"
        assertEquals('P'.code.toByte(), bytes[0])
        assertEquals('K'.code.toByte(), bytes[1])
    }
}
