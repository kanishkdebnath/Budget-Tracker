# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state — read this first

This is an **offline-first personal budgeting Android app**, being built in phases against a complete spec. **The spec is the source of truth**; cite section numbers when reasoning about behavior.

- **`PRODUCT_SPEC.md`** — authoritative product/data spec (features F1–F8, the 5-entity Room model, business logic, export format, currency rules; e.g. "F5.4", "§7.1").
- **`docs/superpowers/specs/2026-06-03-budget-tracker-design-system-design.md`** — approved design system (tokens, type scale, shapes, gradients, per-screen composition; maps to Compose in `§7.1`).
- **`docs/design-system/{foundations,components,screens}.html`** — visual reference; CSS gradient recipes copy 1:1 into Compose `Brush`.
- **`docs/superpowers/plans/2026-06-03-implementation-roadmap.md`** — the 11-phase build roadmap. Each phase has its own `…/plans/2026-06-03-phase-N-*.md`.

**Progress (phases merged to `main`):**

- **Phase 1 ✅** — `domain/money/Money` (§9) and `domain/time/MonthUtils` (§7.1, incl. `instantForDay`). Pure Kotlin, fully unit-tested.
- **Phase 2 ✅** — navy Material 3 theme under `ui/theme/` (navy `ColorScheme` light+dark, Inter typography + tabular `money` style, shapes, `BudgetGradients`, `BudgetSemanticColors` via `CompositionLocal`, `BudgetTrackerTheme` with `dynamicColor` default **false**). Inter is bundled in `res/font/`.
- **Phase 3 ✅** — Room data layer under `data/`: 5 entities, DAOs (`Flow`), repositories (uniqueness/archive guards, atomic recurring apply + bulk target save), idempotent `DatabaseSeeder`, `PreferencesRepository` (DataStore), `AppContainer` (manual DI). `BudgetDatabase` v1, schema exported to `app/schemas/`.
- **Phase 4 ✅** — app shell under `ui/`: `BudgetApplication` (owns `AppContainer`, seeds on launch), `BudgetApp` (Scaffold + `NavigationBar` 5 tabs + `NavHost`), Settings via top-bar gear, shared month-nav top bar (`ui/components`), empty-state placeholder screens (`ui/screens/*`). `MainActivity` hosts `BudgetApp`. ViewModels are built by `ui/AppViewModelProvider` (reads `BudgetApplication.container`).
- **Phase 5 + 5b ✅** — Categories (F2): `ui/screens/categories/` — `CategoriesViewModel` (pure `buildSections` for the 4 filters), grouped list with filter chips, create/edit/archive sheets (archive surfaces the live-categories guard via snackbar), and drag-to-reorder groups + categories (`sh.calvin.reorderable`, persisted via atomic DAO `reorder()`).
- **Phase 6 + 6b ✅** — Log (F1 + F8): `ui/screens/log/` — `LogViewModel` (pure `buildLogState`: month Net-band totals + per-date sections), `NetBand` (shared `ui/components`), per-date transaction cards, add/edit/delete sheet (amount parse via `Money.parseToMinor`, category dropdown, M3 date picker, §F1.5 cross-month jump), and a calculator popover (`domain/calc/Calculator` + `CalculatorDialog`).
- **Phase 7 ✅** — Plan (F3): `ui/screens/plan/` — `PlanViewModel` (pure `computeTargetTotals`; carry-forward pre-fill §F3.4 with a banner), per-group target inputs, live target `NetBand`, sticky Save bar (atomic `TargetRepository.bulkSave`, §F3.3/§3.5; blank clears).
- **Phase 8 ✅** — Report (F4): pure `domain/report/` (`aggregateReport` §7.2 + deterministic `generateNarrative` §7.3); `ui/screens/report/` — narrative box, actuals `NetBand` + planned caption, per-group Plan/Actual/Δ tables with color-coded deltas + Inc/Exp chips, recurring-due banner.
- **Phase 9 ✅** — Recurring (F5): `ui/screens/recurring/` — `RecurringViewModel` (pure `buildRecurringRows`: applied/actionable/inactive classification + sort), Active/Inactive sections, 3-state cards (Apply / applied check-pill / inactive), create/edit sheet (scrollable) with active toggle, one-tap idempotent `apply` (§7.4) to the current month.
- **Phase 10 ✅** — Settings (F7): `ui/screens/settings/` — `SettingsViewModel`, currency picker (common ISO list + custom code; retroactive reformat since all screens observe `PreferencesRepository.currency`), theme mode (System/Light/Dark) + dynamic-color toggle wired through `MainActivity` → `BudgetTrackerTheme`, About tiles.
- **Phase 11 ✅** — Export (F6): `export/` — pure row builders, `ExcelExporter` (FastExcel, 3-sheet xlsx §8.1), `PdfExporter` (native `PdfDocument` §8.2), `ExportManager` (cache file + `FileProvider` share). Export buttons on the Report screen → Android share sheet (`budget-{month}-logs.xlsx` / `-report.pdf`). `ReportViewModel.exportBundle`.
- **Status: F1–F8 complete.** Remaining work is polish/deferred items: density mode (design §7.2), PDF "Page X of Y" + embedded Noto Sans (§F6.4), and the §13 deferred-scope features (auto-apply recurring, sync, app-lock, widgets, charts).

## Build & test

Uses the Gradle wrapper (Gradle 9.4.1). Single module `:app`.

> **No system JDK is installed** — every Gradle invocation needs `JAVA_HOME` pointed at Android Studio's bundled JBR:
> `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` (JBR 21). Without it, `gradlew` fails with "Unable to locate a Java Runtime."

```bash
./gradlew assembleDebug              # build debug APK
./gradlew installDebug               # build + install on a connected device/emulator
./gradlew :app:compileDebugKotlin    # fast compile-only check (no APK)
./gradlew lint                       # Android lint
./gradlew test                       # JVM unit tests (src/test)
./gradlew connectedAndroidTest       # instrumented tests on a device (src/androidTest)
```

Run a single unit test (wildcards allowed). The `--tests` filter needs the concrete task `:app:testDebugUnitTest` — the `test` aggregate rejects `--tests`:

```bash
./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.domain.money.MoneyTest"
./gradlew :app:testDebugUnitTest --tests "*.someTestMethod"
```

Pure-logic code (money/month/narrative/report aggregation per `PRODUCT_SPEC §7`, `§9`) should be JVM-unit-testable in `src/test` without an emulator — keep it free of Android framework deps so it stays there.

> **DAO/Room tests run on the JVM via Robolectric** (`@RunWith(RobolectricTestRunner::class)` + `@Config(sdk = [34])`), using `Room.inMemoryDatabaseBuilder` + `runTest`. So `./gradlew test` covers the data layer too — **no emulator needed** for persistence logic. Only true UI/instrumented checks (screenshots, Espresso) need a device.

## Toolchain

Kotlin 2.2.10 · AGP 9.2.1 · Compose BOM 2026.02.01 · Material 3 · Room 2.8.4 (KSP) · DataStore · coroutines · `minSdk 24`, `target/compileSdk 36`, JVM target 11. Dependencies are managed via the version catalog `gradle/libs.versions.toml` — **add libraries there**, then reference `libs.*` in `app/build.gradle.kts` (don't hardcode coordinates).

> **AGP 9 built-in Kotlin + KSP:** AGP 9 bundles Kotlin and, by default, blocks third-party plugins (KSP/Room) from registering generated source sets via the `kotlin.sourceSets` DSL. `gradle.properties` sets `android.disallowKotlinSourceSets=false` to allow it — keep it.

> **`java.time` + minSdk 24:** all month math uses `java.time` (`YearMonth`/`ZoneId`), which is API 26+. The build targets `minSdk 24`, so **core-library desugaring is enabled** (`isCoreLibraryDesugaringEnabled = true` + `coreLibraryDesugaring(libs.desugar.jdk.libs)` in `app/build.gradle.kts`). Keep it on; `java.time` is safe to use throughout.

## Architecture (from `PRODUCT_SPEC §11`)

Package layout under `com.example.budgettracker`:

- **`data`** ✅ — Room entities (`data/entity`), DAOs returning `Flow` (`data/dao`), `BudgetDatabase` + converters + seeding (`data/db`), repositories (`data/repository`), `AppContainer` (manual DI), `OpResult` (guarded-write result).
- **`domain`** ✅ (partial) — `money/Money`, `time/MonthUtils`. Report aggregation + deterministic narrative arrive in Phase 8. Plain Kotlin, unit-testable.
- **`ui`** — `ui/theme` ✅ holds the design system; screens + per-screen `ViewModel` (`StateFlow`) arrive Phase 4+.
- **`export`** — Excel + PDF builders, off the main thread (Phase 11).

Reactive flow: DAOs expose `Flow` → repository → ViewModel `StateFlow` → Compose. No manual refresh; edits reflect immediately. **Repositories take a `now: () -> Long` provider** (default `System::currentTimeMillis`) so tests pin timestamps. **Name uniqueness (case-insensitive, among live items) is enforced in `CategoryRepository`, not a DB unique index** — Room can't express partial/case-insensitive unique indices, and a global one would wrongly block reusing an archived name. Multi-DAO writes use `RoomDatabase.withTransaction { }`.

## Invariants that bite if ignored

These are easy to get subtly wrong and are baked into the spec:

- **Money is `Long` minor units** (paise/cents), range `0..1_000_000_000_000`. Never `Int` (overflows ~₹21M), never floating point — all arithmetic in `Long`. Currency is a single user preference, **not** stored per amount. Display via `formatMoney(minor, currency)` (`§9`): INR uses `en-IN` lakh/crore grouping, JPY has 0 decimals.
- **Months are derived in the device's local timezone**, not UTC (`§7.1`) — a deliberate divergence from the pathforge web origin. Use `monthOf(epochMillis, zone)` and `monthRange("YYYY-MM", zone)` with `java.time`.
- **The transaction table is named `transactions`** (`TRANSACTION` is a SQLite keyword, `§6`); the entity class is `TransactionEntity` to avoid clashing with Room's `@Transaction`.
- **Recurring apply is manual and idempotent** (`§7.4`, `RecurringRepository.apply`): reject if `!active` or `lastRunMonth == currentMonth`; the insert + `lastRunMonth` update run in one `withTransaction`. Bulk target save (`TargetRepository.bulkSave`) is likewise atomic. `dayOfMonth` is constrained to 1–28.
- **Seed data runs once on first launch** (`§6.7`, `DatabaseSeeder.seedIfEmpty`), guarded by "no groups exist" — idempotent.
- **Color is never the only signal:** deltas always carry a `+`/`−` sign prefix and qualifier text. The semantic `income` (green) / `overage` (red) colors stay **fixed even when dynamic color is on** — they're data, not chrome (design `§7.3`).

## Design system essentials

- **Brand color `#0d2736` (deep navy)**, Material 3 Expressive, theme follows the OS. Color approach is **hybrid**: fixed navy palette by default (`BudgetTrackerTheme(dynamicColor = false)`), with a Settings toggle for Android-12+ dynamic color (wired in Phase 10).
- **Navigation:** bottom nav with **5 tabs** (Log · Plan · Report · Categories · Recurring) + a **Settings gear** in the top app bar. Six screens total.
- **Font** Inter with **tabular numerals mandatory anywhere money appears**. Signature shapes: pill (FAB/CTA/active-nav) and 16dp card. Gradients are used **only** on surfaces/CTAs, **never on data**.
- **Shapes:** the pill is its own token (`PillShape` / `CircleShape`), applied explicitly to FAB/CTA/chips — **never mapped onto `Shapes.extraLarge`**. M3 reads `extraLarge` for `ModalBottomSheet`, `DatePickerDialog`, and the large FAB, so a `percent = 50` corner there renders those surfaces as content-clipping **ellipses** (`extraLarge` is 28dp).
- **No "brown":** the navy `ColorScheme` sets the full `surfaceContainer*` ramp (+ `surfaceBright`/`surfaceDim`) explicitly — see `Color.kt`. M3's baseline defaults for those roles are warm browns, which leak into anything that reads them (sheets, dialogs, menus, `Card`s). With the ramp set, those surfaces stay navy.
- **Dark-first** to match the mockups: `DEFAULT_THEME_MODE = "dark"`. The `BudgetGradients` are applied via shared components: `NetBand` (navy-gradient hero with light text + `Money.formatShort` notation + optional `◎ Plan` sub-row), `GradientFab`, `GradientBanner` (info/amber), `BudgetCard` (subtle surface gradient + faint border in dark, plain `Surface` in light), and `BudgetBackground` (brand-tinted radial **top-glow** behind every screen — the Scaffold + all top bars are transparent so the glow shows through, dark-only). The **bottom nav** uses the navy `BudgetGradients.BottomNav` surface + a navy active-pill in dark themes; M3's baseline `surfaceContainer`/indicator reads as a muddy **brown** in this navy scheme, so it's overridden (light keeps the M3 default). The reference mockups live in `docs/design-system/*.html`; serve them over HTTP (`file://` is blocked in the browser tool) to compare.
- **Finer-detail design pass (one screen per PR):** ✅ **Log** — `DateCard` uses `BudgetCard`; date header is `dayLabel` ("Jun 4") + a gray `WeekdayPill` ("THU") + colored short day-net (no "Net" prefix); rows lead with the **description as title** and show `"group · category"` as the subtitle (category-as-title + group-as-subtitle when no description). ✅ **Report** — `NarrativeBox`/group cards/export card use `BudgetCard`; narrative gets a `"Summary · {month}"` label + the over/under clause colored (red overage / green underspend) via `AnnotatedString`; group-card header right is a **group delta pill** (not the actual subtotal); rows show **◎ (`TrackChanges`) on the Plan number** + bold Actual + a `DeltaPill` (favorable green / unfavorable red / flat `0`), replacing the old column-label row + plain delta text. **Remaining:** gradient primary buttons, Report **top-bar export icon** (needs shared-top-bar plumbing — export still lives in the on-screen card), Categories search, density mode.
- Full token tables (light + dark color schemes, type scale, spacing/radius, gradient recipes) live in the design spec `§3` and the `foundations.html` reference. Mirror them into `MaterialTheme.colorScheme`/`Typography`/`Shapes`; keep semantic `income`/`overage`/`expense` shortcuts in a sibling object via `CompositionLocalProvider` (design `§7.1`).

## Conventions

Commits follow **Conventional Commits** (`chore:`, `docs:`, `feat:`, …) — see `git log`. The design system was built iteratively as versioned HTML mockups (`v1`→`v15`); the `v*.png` screenshots and the `.superpowers/` / `.playwright-mcp/` working dirs are gitignored scratch, not deliverables.

## Git workflow

The GitHub remote is `origin` → `https://github.com/kanishkdebnath/Budget-Tracker`. `main` is the integration branch and must stay green.

**All changes land via pull request — do not commit feature work directly to `main`:**

1. Branch off `main` (`feat/...`, `fix/...`, `docs/...`, or `feat/phase-N-...` for a roadmap phase).
2. Commit in bite-sized Conventional Commits; keep tests passing per commit.
3. Push and open a PR: `gh pr create` (verify `./gradlew test` is green first).
4. **The human reviews and merges** the PR on GitHub — Claude opens PRs and waits, and does **not** self-merge. After merge, delete the branch. One roadmap phase = one branch = one PR.

**CI:** `.github/workflows/ci.yml` (GitHub Actions) runs `./gradlew testDebugUnitTest` on every PR to `main` and on pushes to `main` (JDK 21 + Android SDK). Keep it green.
