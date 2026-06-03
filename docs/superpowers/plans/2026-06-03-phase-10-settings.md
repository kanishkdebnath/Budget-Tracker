# Phase 10 — Settings (F7) — Implementation Plan

> REQUIRED SUB-SKILL: superpowers:executing-plans.

**Goal:** The Settings screen — single display currency (F7) with retroactive reformatting, plus appearance toggles (theme + dynamic color) wired to the real theme.

**Architecture:** `PreferencesRepository` gains `themeMode` + `dynamicColor`. `SettingsViewModel` exposes currency/theme/dynamic-color with setters. Currency reformat is **automatic** — money is stored currency-agnostic and every screen's ViewModel observes `preferences.currency`, so changing it re-emits everywhere. Theme is applied app-wide by `MainActivity` collecting `themeMode`/`dynamicColor` and passing them to `BudgetTrackerTheme`.

## Files
- `data/repository/PreferencesRepository.kt` — `themeMode` (string) + `dynamicColor` (bool).
- `ui/screens/settings/SettingsViewModel.kt` — `ThemeMode` (+ `resolveDark`/`fromStorage`), `CurrencyOption`/`COMMON_CURRENCIES`, `isValidCurrencyCode`, VM.
- `ui/screens/settings/CurrencyPickerSheet.kt` — common-currency list + custom-code field.
- `ui/screens/settings/SettingsScreen.kt` — grouped tiles (Money / Appearance / About) + theme dialog.
- `MainActivity.kt` — apply `themeMode`/`dynamicColor` to `BudgetTrackerTheme`.
- `ui/AppViewModelProvider.kt` — `SettingsViewModel` initializer.
- test: `SettingsLogicTest` (`ThemeMode.resolveDark`/`fromStorage`, `isValidCurrencyCode`).

## Verification (done)
- `./gradlew test` green; `:app:assembleDebug` green.
- Emulator: Settings tiles render; picking **USD** in the currency sheet retroactively reformatted every amount to `$` (Log Net band $6,500, transactions +$1,500/+$5,000); selecting **Dark** re-themed the whole app to the navy dark palette.

## Self-Review
- **Spec coverage:** F7.1 (single ISO-4217 currency, common list + any 3-letter code, default INR), F7.2 (retroactive reformat). Theme + dynamic color are design extras, wired to `BudgetTrackerTheme`.
- **Deferred:** Density (design §7.2) — stored as a pref but not yet applied across screens; Export (F6) → Phase 11.
