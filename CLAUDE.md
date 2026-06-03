# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state — read this first

This is an **offline-first personal budgeting Android app**, and it is at the **pre-implementation** stage:

- **The spec is the source of truth, not the code.** `app/src/main/.../MainActivity.kt` is still the Android Studio template ("Hello Android!" `Greeting`), and `ui/theme/Color.kt` / `Theme.kt` still hold the template's **Purple** palette with `dynamicColor = true`. None of the product features exist yet. When implementing, build against the specs below — and **replace** the template theme, don't extend it.
- **`PRODUCT_SPEC.md`** is the authoritative product/data spec (features F1–F8, the 5-entity Room model, business logic, export format, currency rules). Cite section numbers (e.g. "F5.4", "§7.1") when reasoning about behavior.
- **`docs/superpowers/specs/2026-06-03-budget-tracker-design-system-design.md`** is the approved design system (color tokens, type scale, shapes, gradients, per-screen composition). It maps tokens → Compose (`§7.1`).
- **`docs/design-system/{foundations,components,screens}.html`** are the visual reference. Open in a browser; the CSS gradient recipes copy 1:1 into Compose `Brush` calls.

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

## Toolchain

Kotlin 2.2.10 · AGP 9.2.1 · Compose BOM 2026.02.01 · Material 3 · `minSdk 24`, `target/compileSdk 36`, JVM target 11. Dependencies are managed via the version catalog `gradle/libs.versions.toml` — **add libraries there**, then reference `libs.*` in `app/build.gradle.kts` (don't hardcode coordinates).

> **`java.time` + minSdk 24:** all month math uses `java.time` (`YearMonth`/`ZoneId`), which is API 26+. The build targets `minSdk 24`, so **core-library desugaring is enabled** (`isCoreLibraryDesugaringEnabled = true` + `coreLibraryDesugaring(libs.desugar.jdk.libs)` in `app/build.gradle.kts`). Keep it on; `java.time` is safe to use throughout.

## Planned architecture (from `PRODUCT_SPEC §11`)

Target package layout under `com.example.budgettracker`:

- **`data`** — Room entities, DAOs (return `Flow`), repositories, DataStore (currency + density preference).
- **`domain`** — report aggregation, deterministic narrative, money & month utilities. Plain Kotlin, unit-testable.
- **`ui`** — Compose screens + per-screen `ViewModel` (`StateFlow`); `ui/theme` holds the design system.
- **`export`** — Excel (Apache POI or a lighter XLSX writer) and PDF (`PdfDocument`/pdfbox-android) builders, run off the main thread.

Reactive flow: DAOs expose `Flow` → ViewModel `StateFlow` → Compose. No manual refresh; edits reflect immediately (the local equivalent of query-cache invalidation).

## Invariants that bite if ignored

These are easy to get subtly wrong and are baked into the spec:

- **Money is `Long` minor units** (paise/cents), range `0..1_000_000_000_000`. Never `Int` (overflows ~₹21M), never floating point — all arithmetic in `Long`. Currency is a single user preference, **not** stored per amount. Display via `formatMoney(minor, currency)` (`§9`): INR uses `en-IN` lakh/crore grouping, JPY has 0 decimals.
- **Months are derived in the device's local timezone**, not UTC (`§7.1`) — a deliberate divergence from the pathforge web origin. Use `monthOf(epochMillis, zone)` and `monthRange("YYYY-MM", zone)` with `java.time`.
- **The transaction table must be named `transactions`** (or `budget_transaction`) — `TRANSACTION` is a SQLite keyword (`§6` Room note).
- **Recurring apply is manual and idempotent** (`§7.4`): reject if `!active` or `lastRunMonth == currentMonth`; the insert + `lastRunMonth` update happen in **one atomic Room `@Transaction`**. Bulk target save is likewise atomic. `dayOfMonth` is constrained to 1–28.
- **Seed data runs once on first launch** (`§6.7`), guarded by "no groups exist" — idempotent.
- **Color is never the only signal:** deltas always carry a `+`/`−` sign prefix and qualifier text. The semantic `income` (green) / `overage` (red) colors stay **fixed even when dynamic color is on** — they're data, not chrome (design `§7.3`).

## Design system essentials

- **Brand color `#0d2736` (deep navy)**, Material 3 Expressive, theme follows the OS. Color approach is **hybrid**: fixed navy palette by default, with a Settings toggle for Android-12+ dynamic color. (The template's current `dynamicColor = true` default contradicts this — fix when wiring the theme.)
- **Navigation:** bottom nav with **5 tabs** (Log · Plan · Report · Categories · Recurring) + a **Settings gear** in the top app bar. Six screens total.
- **Font** Inter with **tabular numerals mandatory anywhere money appears**. Signature shapes: pill (FAB/CTA/active-nav) and 16dp card. Gradients are used **only** on surfaces/CTAs, **never on data**.
- Full token tables (light + dark color schemes, type scale, spacing/radius, gradient recipes) live in the design spec `§3` and the `foundations.html` reference. Mirror them into `MaterialTheme.colorScheme`/`Typography`/`Shapes`; keep semantic `income`/`overage`/`expense` shortcuts in a sibling object via `CompositionLocalProvider` (design `§7.1`).

## Conventions

Commits follow **Conventional Commits** (`chore:`, `docs:`, `feat:`, …) — see `git log`. The design system was built iteratively as versioned HTML mockups (`v1`→`v15`); the `v*.png` screenshots and the `.superpowers/` / `.playwright-mcp/` working dirs are gitignored scratch, not deliverables.
