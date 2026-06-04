# Fixed Navy Theme (Remove Dynamic Color) — Design

**Goal:** Stop the half-themed look by removing Android-12 dynamic color (Material You) and committing to the fixed navy brand. Light/Dark theme stays.

## Problem

The UI has two color layers. The **M3 `colorScheme` layer** (background, surfaces, Settings cards, text, chips, dialogs, text fields) follows dynamic color. The **brand "gradient chrome" layer** — `BudgetGradients` (11 fixed-navy brushes: NetBand, FAB, `BudgetCard`, gradient buttons, banners, bottom-nav surface + pill, sticky save bar, top-glow) and its fixed text inks — is made of static constants that **cannot** read `colorScheme`, so it never changes. With dynamic color on, layer 1 repaints to the wallpaper while layer 2 stays navy → "background + some elements changed, the rest didn't."

The navy gradient is the product's identity (design spec, app icon, every hero surface). Material You structurally fights it. **Decision (this spec): drop dynamic color; the fixed navy scheme is the single, intentional look.** Income (green) / overage (red) semantic colors were already fixed and stay fixed.

## Behavior after

- The app always uses the fixed navy `ColorScheme`; **Theme: System / Light / Dark** still controls light vs dark.
- Settings → Appearance keeps **Theme** and **Density**; the **Dynamic color** row is removed.
- No data migration: any previously-stored `dynamic_color` value is simply ignored.

## Changes (one PR)

1. **`ui/theme/Theme.kt`** — remove the `dynamicColor` parameter and the Material-You branch; `colorScheme = if (darkTheme) DarkColors else LightColors`. Remove the now-unused imports: `dynamicDarkColorScheme`, `dynamicLightColorScheme`, `Build`, `LocalContext`.
2. **`MainActivity.kt`** — stop collecting `preferences.dynamicColor`; stop passing `dynamicColor` to `BudgetTrackerTheme`.
3. **`ui/screens/settings/SettingsScreen.kt`** — remove the Dynamic-color `SwitchTile`, the now-unused `SwitchTile` composable, and the unused `Switch` import.
4. **`ui/screens/settings/SettingsViewModel.kt`** — remove the `dynamicColor` `StateFlow` and `setDynamicColor`.
5. **`data/repository/PreferencesRepository.kt`** — remove the `dynamicColor` flow, `setDynamicColor`, the `DYNAMIC_COLOR` key, and the now-unused `booleanPreferencesKey` import.
6. **`CLAUDE.md`** — drop the "hybrid / Android-12 dynamic-color toggle" wording; record that we committed to the fixed navy brand (the gradient chrome can't follow Material You).

**Not touched:** `BudgetGradients`, NetBand / cards / buttons / nav, and the semantic colors — already the intended fixed-navy brand.

## Testing

- `:app:testDebugUnitTest` green; `assembleDebug` clean (no remaining references to the removed symbols).
- Verify on emulator: Settings → Appearance shows only Theme + Density; toggling Theme (Light/Dark) still works; the navy brand renders consistently with no wallpaper tint anywhere.

## Non-goals

- Making the gradients dynamic (Option B) — explicitly rejected; the brand is fixed navy.
- Changing any existing navy colors, gradients, or the semantic income/overage colors.
