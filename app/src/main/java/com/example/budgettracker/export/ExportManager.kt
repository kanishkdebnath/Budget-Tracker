package com.example.budgettracker.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.time.ZoneId

enum class ExportFormat(val mimeType: String) {
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PDF("application/pdf"),
}

/** Generates an export file in the cache and returns a shareable FileProvider Uri (§F6.2/§F6.3). */
object ExportManager {

    fun export(
        context: Context,
        bundle: ExportBundle,
        format: ExportFormat,
        now: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Uri {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = when (format) {
            ExportFormat.EXCEL -> File(dir, "budget-${bundle.month}-logs.xlsx").also { f ->
                f.outputStream().use { ExcelExporter.write(bundle, zone, it) }
            }
            ExportFormat.PDF -> File(dir, "budget-${bundle.month}-report.pdf").also { f ->
                f.outputStream().use { PdfExporter.write(bundle, zone, now, it) }
            }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Generates a focused transaction-journal file (Log export) and returns a shareable URI. */
    fun export(
        context: Context,
        bundle: LogExportBundle,
        format: ExportFormat,
        now: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Uri {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = when (format) {
            ExportFormat.EXCEL -> File(dir, "budget-${bundle.month}-log.xlsx").also { f ->
                f.outputStream().use { LogExcelExporter.write(bundle, zone, it) }
            }
            ExportFormat.PDF -> File(dir, "budget-${bundle.month}-log.pdf").also { f ->
                f.outputStream().use { LogPdfExporter.write(bundle, zone, now, it) }
            }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun shareIntent(uri: Uri, format: ExportFormat): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
}
