# Standardized Light Theme — Design

**Goal:** Make light theme *actually light and consistent*. Today it's half-adapted: surfaces/cards/nav flip to light, but the navy gradient chrome (NetBand hero, gradient buttons, banners, Plan sticky bar) stays fixed navy — so light theme reads as a dark theme with light patches. Give every gradient component a **light variant**, selected via one shared `isLight` check.

**Direction (approved):** fully light / airy. Navy lives in **text, icons, primary buttons (FAB), and active states**; the NetBand hero + banners + sticky bar become **light tints**. Mockup: `docs/design-system/screens-light.html`.

**Stacking note:** branched on `feat/fixed-navy-theme` (#33) — merge #33 first.

## Problem (audit)

- **Adapt to light** (have `luminance()`/`isDark` checks): `BudgetCard`, bottom nav, `BudgetBackground` top-glow.
- **Fixed navy** (no theme awareness): `NetBand`, `GradientFab`, `GradientButton`, `GradientBanner`, Plan sticky bar.
- **Real bug:** `GradientButton` FILLED is a light-blue gradient + dark ink → nearly invisible on a light background.

## Light tokens

Canvas `#F4F8FA` · cards `#FFFFFF` + hairline `rgba(13,39,54,.08)` · chips/inputs `#EAF0F4` · text `#0E141A` / muted `#6B7A84` · **primary navy `#0D2736`** · tint `#CFE3F1` (onTint `#0D2736`) · semantic **green `#1F7A5A` / red `#B3261E`** (the existing `IncomeLight`/`OverageLight`) · amber container `#FFE7C4` / onAmber `#6A3B16` · NetBand hero gradient `#E9F2FA → #DCEBF6 → #CFE2F0`.

## Per-component light treatment

| Component | Dark (today) | Light (new) |
|---|---|---|
| **NetBand** hero | navy gradient, light inks | hero gradient `#E9F2FA→#CFE2F0`; label `#4A5E6B`, value `#0D2736`, income `#1F7A5A`, overage `#B3261E` |
| **GradientButton FILLED** | light-blue gradient + dark ink | **solid navy `#0D2736` + white ink** (fixes the invisible-button bug) |
| **GradientButton TONAL** | navy gradient + light ink | tint `#CFE3F1` + navy ink `#0D2736` |
| **GradientBanner INFO** | navy gradient + light content | `#DCEBF6` + navy content `#0D2736` |
| **GradientBanner AMBER** | brown gradient + light content | `#FFE7C4` + `#6A3B16` |
| **Plan sticky bar** | navy gradient + light label | **white** card + hairline + shadow; label muted `#6B7A84`; Save = filled-navy |
| **GradientFab** | navy gradient + white | **unchanged** — stays navy (primary action accent) |
| **BottomNav** | navy surface + navy pill | light surface; **navy-tint pill `#CFE3F1` + navy icon** |
| **BudgetCard** | gradient + border | white surface + hairline border (already plain in light; add the faint border) |
| **BudgetBackground** | top-glow | no glow (already), light bg |
| delta pills / chips / dots | — | already use semantic/scheme tints (verify on light) |

## Implementation approach

1. **One shared light/dark check:** add `BudgetTheme.isLight` (`@Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background.luminance() > 0.5f`). All gradient components use it (replacing the scattered ad-hoc `luminance()` checks in `BudgetCard`/`BottomNav` for consistency).
2. **Light brand brushes** added alongside the dark ones in `BudgetGradients` (`NetBandLight`, `FilledButtonLight`, `TonalButtonLight`, `StickyBarLight`, `BannerInfoLight`, `BannerAmberLight`); FAB unchanged.
3. Each gradient component branches on `BudgetTheme.isLight` for its brush + ink colors (light inks defined alongside the existing private constants).
4. **Semantic** income/overage already theme-aware via `BudgetTheme.semanticColors` — unchanged.

This centralizes the "which theme" decision and gives one parallel light token set, so the chrome is consistent in both themes.

## Testing

- `:app:testDebugUnitTest` green; `assembleDebug` clean.
- Verify on emulator in **light theme** (Settings → Theme → Light): NetBand hero, buttons (FILLED visible!), banners, sticky bar, nav are all light/airy and consistent; dark theme unchanged; switch Light↔Dark repeatedly to confirm both are coherent.

## Non-goals

- No dynamic color (removed in #33). No change to dark theme's look. No change to semantic green/red or to the FAB.
