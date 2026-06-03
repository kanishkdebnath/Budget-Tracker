# Phase 4 — App Shell & Navigation — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax.

**Goal:** A navigable app skeleton with the real navy chrome — `BudgetApplication` wiring `AppContainer` + first-launch seeding, a 5-tab bottom nav with a Settings gear, the shared month-nav top bar, and empty-state placeholder screens.

**Architecture:** `ui/` gains a `BudgetApp` root composable hosting a Material 3 `Scaffold` (top bar + `NavigationBar`) around a Navigation-Compose `NavHost`. Top-level destinations (Log · Plan · Report · Categories · Recurring) are an enum; Settings is reached from the gear (not a tab). Log/Plan/Report share a single month-nav top bar driven by app-level month state; Categories/Recurring show a titled bar; Settings shows a back bar. Screens are empty-state placeholders that feature phases (5–10) replace.

**Tech stack added:** navigation-compose 2.9.8, lifecycle-viewmodel-compose & lifecycle-runtime-compose 2.10.0, material-icons-extended.

**Verification:** `:app:compileDebugKotlin` + `:app:assembleDebug` (proves the shell builds & packages), `./gradlew test` stays green (46), and `@Preview`s for the screens/top bars. Launch + screenshot needs a device (no emulator in the build env) — manual.

---

## Files

| File | Responsibility |
|---|---|
| `BudgetApplication.kt` | Holds `AppContainer`; runs `seeder.seedIfEmpty()` on a background scope at launch |
| `AndroidManifest.xml` | `android:name=".BudgetApplication"` |
| `MainActivity.kt` | Hosts `BudgetApp()` in `BudgetTrackerTheme` (replaces the template `Greeting`) |
| `ui/navigation/Destinations.kt` | `TopLevelDest` enum (route/label/icons) + `SETTINGS_ROUTE` |
| `ui/BudgetApp.kt` | Scaffold + top bar selection + `NavigationBar` + `NavHost`; app-level month state |
| `ui/components/BudgetTopBars.kt` | `MonthNavTopBar`, `SectionTopBar`, `BackTopBar` |
| `ui/components/EmptyState.kt` | Reusable centered empty state |
| `ui/screens/{log,plan,report,categories,recurring,settings}/*Screen.kt` | Empty-state placeholders |

## Tasks

1. **Dependencies** (done above): nav, lifecycle-compose, icons in the catalog + `app/build.gradle.kts`.
2. **`BudgetApplication` + manifest + `MainActivity`** — instantiate `AppContainer`, seed on launch, host `BudgetApp`.
3. **Destinations + top bars + empty state** components.
4. **Placeholder screens** (6) using `EmptyState`.
5. **`BudgetApp`** — wire Scaffold + nav graph + month state + previews.
6. **Verify** — compile, assemble, test green; commit; PR.

Key behaviors:
- Month state: `rememberSaveable` `"YYYY-MM"` seeded from `MonthUtils.monthOf(System.currentTimeMillis(), ZoneId.systemDefault())`; chevrons do `YearMonth.parse(month) ± 1`. Shared by Log/Plan/Report.
- Bottom nav hidden on the Settings route; gear navigates to Settings with `launchSingleTop`.
- Active tab uses the filled icon, inactive the outlined (M3 standard).

## Phase exit criteria
- `:app:assembleDebug` + `./gradlew test` green.
- App launches into Log, all 5 tabs + Settings navigate, navy theme applied, month chevrons change the label, empty states render. (Build-verified; visual check manual on a device.)
- `BudgetApplication` seeds on first launch (seeder logic already tested in Phase 3).

## Self-Review notes
- **Spec/design coverage:** design §2 (5 tabs + Settings gear), §5 per-screen top-bar shapes (month-nav vs title vs back). Wires the Phase 3 seeder into launch (the deferred item).
- **Deferred:** real screen content + ViewModels (Phases 5–10); month state moves into feature ViewModels then. Search field on Categories and the FABs are placeholders here.
