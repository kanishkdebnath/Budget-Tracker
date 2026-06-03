# Budget Tracker — Implementation Roadmap

**Date:** 2026-06-03
**Drives:** [`PRODUCT_SPEC.md`](../../../PRODUCT_SPEC.md) · [Design System](../specs/2026-06-03-budget-tracker-design-system-design.md)

This roadmap sequences the build into **shippable phases**. Each phase produces working, independently-testable software and gets its own detailed TDD plan in this directory when we reach it. We build bottom-up: pure logic first (emulator-free, fully unit-tested), then the theme, then persistence, then features that depend on both.

---

## Locked foundational decisions

These assumptions hold across all phases unless a phase plan overrides them:

| Decision | Choice | Rationale |
|---|---|---|
| **`java.time` support** | Keep `minSdk 24` + enable **core-library desugaring** | Preserves wider device support; spec lists it as a valid path (§11). One-line build change in Phase 1. |
| **Dependency injection** | **Manual** (constructor wiring via a small `AppContainer`) | Spec calls repositories "simple enough to wire manually" (§11). Avoids Hilt/KSP-processor overhead. Revisit only if wiring gets painful. |
| **Inter font** | Bundle the **variable TTF** in `res/font/` | Offline-first app must not depend on Downloadable Fonts at runtime. |
| **Reactive stack** | Room DAOs return `Flow` → `ViewModel` `StateFlow` → Compose `collectAsStateWithLifecycle` | Native equivalent of pathforge's query invalidation (§10). |
| **Excel library** | **Deferred to Phase 11** | POI is heavy (APK size); decide POI vs FastExcel/dhatim when we build export, not before. |

---

## Phases

Dependencies in brackets. A phase may start once its dependencies are green.

### Phase 1 — Foundations: Money & Time core  ▶ *detailed plan written*
**Goal:** Build supports `java.time` on minSdk 24, and the two trickiest pure-logic utilities are implemented and exhaustively unit-tested.
**Builds:** core-library desugaring; `domain/money/Money.format(minor, currency)` (§9); `domain/time/MonthUtils` — `monthOf`, `monthRange`, `monthLabel` (§7.1).
**Shippable proof:** `./gradlew test` green; `./gradlew :app:compileDebugKotlin` green. No emulator needed.
**Plan:** [`2026-06-03-phase-1-foundations.md`](2026-06-03-phase-1-foundations.md)

### Phase 2 — Design-system theme  [needs: 1]
**Goal:** Replace the Android Studio template's Purple theme with the approved navy Material 3 design system.
**Builds:** `ui/theme/Color.kt` (light + dark navy `ColorScheme` per design §3.1); `Type.kt` (Inter + `money` style, tabular nums); `Shape.kt` (4/8/16/20/pill); `SemanticColors` (`income`/`overage`/`expense`) via `CompositionLocalProvider`; gradient `Brush` helpers (design §3.6); `BudgetTrackerTheme` with `dynamicColor` default **false**.
**Shippable proof:** `@Preview` swatch/token gallery renders the navy palette; app launches with navy chrome instead of purple.

### Phase 3 — Room data layer  [needs: 1]
**Goal:** Persistence with reactive reads, first-launch seeding, and atomic writes.
**Builds:** 5 entities (`category_group`, `category`, `transactions`, `target`, `recurring_template` — note table names, §6); DAOs returning `Flow`; `@Transaction` atomic ops (recurring apply, bulk target upsert); `BudgetDatabase`; repositories; DataStore prefs (`currency`, `density`); idempotent seeding (§6.7); `AppContainer`.
**Shippable proof:** Instrumented DAO tests (`connectedAndroidTest`) cover CRUD, unique constraints, seeding idempotency, and atomic recurring-apply.

### Phase 4 — App shell & navigation  [needs: 2, 3]
**Goal:** A navigable app skeleton with the real chrome.
**Builds:** `Scaffold` + bottom nav (5 tabs: Log · Plan · Report · Categories · Recurring) + Settings gear in top app bar (design §2); Navigation-Compose graph; empty-state screens; month-nav top bar component shared by Log/Plan/Report.
**Shippable proof:** App launches, all 5 tabs + Settings navigate, theme applied, empty states show.

### Phase 5 — Categories  [needs: 3, 4]
**Goal:** F2 — groups & categories CRUD, archive, drag-reorder.
**Builds:** Categories screen, group/category cards, create/edit bottom sheet, archive confirm (block group archive with live categories, §F2.5), drag-to-reorder persistence (§F2.6), `CategoriesViewModel`.
**Shippable proof:** Create/edit/archive/reorder persists across restart; seeded defaults appear on fresh install.

### Phase 6 — Log & calculator  [needs: 3, 4, 5]
**Goal:** F1 + F8 — transaction CRUD, month view, Net band, calculator amount entry.
**Builds:** Log screen (per-date group cards, filter chips), Net band, add/edit transaction sheet, `BudgetAmountInput` parsing (§F1.6), calculator popover (§F8), cross-month navigation toast (§F1.5), `LogViewModel`.
**Shippable proof:** Add ₹500 expense updates the column + Net band immediately; delete reverts; calculator feeds parsed minor units.

### Phase 7 — Plan  [needs: 3, 4, 5]
**Goal:** F3 — monthly targets with carry-forward pre-fill.
**Builds:** Plan screen (per-group target inputs), target Net band, carry-forward banner + previous-month pre-fill (§F3.4), bulk-upsert save (§F3.3, atomic), clearing-removes-target (§F3.5), `PlanViewModel`.
**Shippable proof:** Set June targets → open empty July → form pre-filled from June, editable before save.

### Phase 8 — Report & narrative  [needs: 3, 5, 6, 7]
**Goal:** F4 — target-vs-actual report with deterministic narrative.
**Builds:** report aggregation (§7.2, pure + unit-tested), deterministic narrative generator (§7.3, pure + unit-tested), Report screen (narrative box, Plan/Actual/Δ table, totals, recurring-due banner), color-coded signed deltas, `ReportViewModel`.
**Shippable proof:** ₹3,500 actual vs ₹3,000 planned → red +₹500 delta + "over by ₹500 (17%)" narrative; aggregation unit tests green.

### Phase 9 — Recurring  [needs: 3, 4, 5, 6]
**Goal:** F5 — recurring templates with manual, idempotent apply.
**Builds:** Recurring screen (Active/Inactive sections, three card states), create/edit sheet, active toggle, one-tap Apply with idempotency guard (§7.4, atomic), `RecurringViewModel`.
**Shippable proof:** Apply "Salary (day 1)" in June creates one June-1 income txn + disables the button for June; re-tap is a no-op.

### Phase 10 — Settings  [needs: 2, 3, 4]
**Goal:** F7 — currency, theme, density, dynamic-color toggle.
**Builds:** Settings screen (grouped tiles), ISO-4217 currency picker sheet (retroactive reformat, §F7.2), density toggle (Comfortable/Compact, design §7.2), dynamic-color toggle (design §7.3).
**Shippable proof:** Change currency → all amounts reformat instantly; density flips row heights; dynamic-color toggles palette while keeping semantic income/overage fixed.

### Phase 11 — Export  [needs: 6, 7, 8, 9]
**Goal:** F6 — Excel + PDF export via the Android share sheet.
**Builds:** decide Excel lib (POI vs FastExcel); 3-sheet xlsx (§8.1); PDF report with embedded Noto Sans (§8.2); off-main-thread generation; `FileProvider` + share sheet.
**Shippable proof:** Export June → 3-sheet xlsx whose totals match the report + a PDF whose ₹ glyphs render.

---

## Build order rationale

- **Pure logic before UI** (Phases 1, and the aggregation/narrative in 8): these are the highest-bug-density areas and fully unit-testable without an emulator, so TDD pays off most here.
- **Categories before Log/Plan** (Phase 5 before 6/7): transactions and targets reference categories; the picker needs live categories to exist.
- **Report after Log + Plan** (Phase 8 after 6/7): aggregation needs real transactions and targets to report on.
- **Export last** (Phase 11): it serializes everything the other features produce.
