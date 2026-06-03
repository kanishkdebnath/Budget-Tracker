# Phase 11 — Export (F6) — Implementation Plan

> REQUIRED SUB-SKILL: superpowers:executing-plans.

**Goal:** Export the Report month as a 3-sheet Excel (§8.1) + a PDF (§8.2) via the Android share sheet. **Completes F1–F8.**

**Decision:** Excel via **FastExcel** (`org.dhatim:fastexcel`) — its only runtime dep is `opczip` (pure-Java OPC zip), so it's Android-safe and tiny vs Apache POI's ~10 MB+. PDF via the native `android.graphics.pdf.PdfDocument` (no dependency).

**Architecture:** `export/` — pure `buildExportTxnRows`/`buildExportRecurringRows` + an `ExportBundle` (month/currency/narrative/report/transactions/recurring) produced by `ReportViewModel.exportBundle`. `ExcelExporter`/`PdfExporter` write to an `OutputStream`. `ExportManager` writes to `cacheDir/exports/` and returns a `FileProvider` Uri; the Report screen shares it off-main (`Dispatchers.IO`).

## Files
- `gradle/*` — `org.dhatim:fastexcel:0.20.1`.
- `export/ExportData.kt` — bundle + pure row builders.
- `export/ExcelExporter.kt` — 3 sheets (Transactions/Targets/Recurring), per-currency number formats, totals, frozen header.
- `export/PdfExporter.kt` — header, summary + planned, narrative (wrapped), per-group Plan/Actual/Δ tables + subtotals, per-page footer; pagination.
- `export/ExportManager.kt` — cache file + `FileProvider` Uri + share intent.
- `AndroidManifest.xml` + `res/xml/file_paths.xml` — FileProvider.
- `ui/screens/report/ReportScreen.kt` — Export card (Excel/PDF), disabled until data loads (§F6.5).
- `ReportViewModel.exportBundle`.
- tests: `ExportDataTest` (row builders), `ExcelExporterTest` (valid xlsx zip on the JVM — confirms FastExcel works).

## Verification (done)
- `./gradlew test` green (incl. `ExcelExporterTest` → valid `PK` zip); `:app:assembleDebug` green.
- **Emulator:** Export → **Excel** produced `budget-2026-06-logs.xlsx` and **PDF** produced `budget-2026-06-report.pdf`, each opening the Android share sheet with the correct filename — confirming FastExcel + PdfDocument both run on Android and FileProvider sharing works.

## Self-Review
- **Spec coverage:** F6.1–F6.3 (Excel + PDF, off-main, share sheet, §8.1 sheets + filenames §F6.3), F6.5 (disabled until loaded).
- **Deferred refinements:** PDF "Page X of Y" (currently "Page X") + embedded Noto Sans (§F6.4 — the system typeface already renders ₹/€/¥/£/Indic on-device and PdfDocument embeds the glyphs); §8.2 fine details.
