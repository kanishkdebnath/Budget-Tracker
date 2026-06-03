# Phase 8 — Report (F4) — Implementation Plan

> REQUIRED SUB-SKILL: superpowers:executing-plans.

**Goal:** The monthly report — target-vs-actual per group/category with color-coded deltas, totals, and the deterministic narrative.

**Architecture:** Pure `domain/report/`: `aggregateReport` (§7.2 — actual/target per category, deltas, inferred group kind, totals by kind; shows live categories + archived-with-activity) and `generateNarrative` (§7.3 — three cases, biggest overage/underspend). `ReportViewModel` combines month transactions + targets + categories + groups + recurring templates + currency into a `ReportUiState`. `ReportScreen` renders the narrative box, the actuals `NetBand` + planned caption, a recurring-due banner, and per-group Plan/Actual/Δ tables.

## Files
- `domain/report/ReportData.kt` — data classes + `aggregateReport` + `inferGroupKind`.
- `domain/report/Narrative.kt` — `generateNarrative`.
- `ui/screens/report/ReportViewModel.kt` — `ReportUiState` + month-scoped combine + recurring-due count.
- `ui/screens/report/ReportComponents.kt` — `NarrativeBox`, `RecurringDueBanner`, `ReportGroupCard` (Plan/Actual/Δ rows, sign+color-coded delta).
- `ui/screens/report/ReportScreen.kt` — assembled screen.
- `ui/AppViewModelProvider.kt` — `ReportViewModel` initializer.
- test: `ReportTest` (aggregation totals/deltas; narrative no-transactions / no-plan / over-by-percent incl. the §F4 "over by ₹500 (17%)").

## Verification (done)
- `./gradlew test` green; `:app:assembleDebug` green.
- Emulator (June): narrative "spent ₹0 against a planned ₹2,000 — under by ₹2,000 (100%). Biggest underspend: Rent…"; Net band actuals ₹1,500; per-group Plan/Actual/Δ with Rent −₹2,000 (green) and Salary +₹1,500 (green).

## Self-Review
- **Spec coverage:** F4.1–F4.5 (group/category target-vs-actual, signed color-coded deltas, totals, narrative), recurring-due banner (F4.6 — apply lives on the Recurring tab, Phase 9). Delta color: expense over = red, under = green; income gain = green.
- **Deferred:** Export actions (F6) → Phase 11; the report shows the data, export buttons arrive then.
