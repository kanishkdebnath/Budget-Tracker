# Standardized Light Theme Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans. Steps use checkbox (`- [ ]`). This is per-component color branching (visual) — verify on the emulator in **light theme**; no unit tests apply to the color logic.

**Goal:** Give every gradient component a light variant so light theme is fully light/airy and consistent (and fix the invisible FILLED button).

**Architecture:** One shared `BudgetTheme.isLight` check; parallel light brushes in `BudgetGradients`; each gradient component branches on `isLight` for brush + ink. Dark theme unchanged. Semantic green/red already theme-aware.

**Tech Stack:** Compose Material 3.

Spec: `docs/superpowers/specs/2026-06-05-light-theme-design.md`. Branched on `feat/fixed-navy-theme` (#33).

---

### Task 1: `BudgetTheme.isLight` + light brushes

**Files:** Modify `ui/theme/Theme.kt`, `ui/theme/Gradient.kt`

- [ ] **Step 1:** In `Theme.kt`, add to the `BudgetTheme` object (after `density`):
```kotlin
    /** True in light themes (background is bright). The single source for brand light/dark branching. */
    val isLight: Boolean
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.background.luminance() > 0.5f
```
Add import `androidx.compose.ui.graphics.luminance`.

- [ ] **Step 2:** In `Gradient.kt`, add light variants inside `object BudgetGradients` (after the existing dark brushes):
```kotlin
    // ---- Light-theme brand variants (design: fully-light light theme) ----
    /** Net band hero — light blue tint, dark text. */
    val NetBandLight = Brush.linearGradient(
        colors = listOf(Color(0xFFE9F2FA), Color(0xFFDCEBF6), Color(0xFFCFE2F0)),
        start = diagStart, end = diagEnd,
    )
    /** Filled (primary) button — solid navy in light (white text). */
    val FilledButtonLight = Brush.verticalGradient(listOf(Color(0xFF0D2736), Color(0xFF0D2736)))
    /** Tonal button — light navy tint (navy text). */
    val TonalButtonLight = Brush.verticalGradient(listOf(Color(0xFFD7E8F4), Color(0xFFC7DEEE)))
    /** Sticky bar — white card. */
    val StickyBarLight = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF6FAFC)))
    /** Info banner — light blue tint. */
    val BannerInfoLight = Brush.linearGradient(
        colors = listOf(Color(0xFFE2EEF8), Color(0xFFD6E8F4)), start = diagStart, end = diagEnd,
    )
    /** Amber banner — light amber tint. */
    val BannerAmberLight = Brush.linearGradient(
        colors = listOf(Color(0xFFFFEBD2), Color(0xFFFCE2C0)), start = diagStart, end = diagEnd,
    )
```

- [ ] **Step 3:** Compile: `./gradlew :app:compileDebugKotlin` — BUILD SUCCESSFUL.

### Task 2: NetBand light variant

**Files:** Modify `ui/components/NetBand.kt`

- [ ] **Step 1:** Add light inks beside the existing constants:
```kotlin
private val NetLabelLight = Color(0xFF4A5E6B)
private val NetInkLight = Color(0xFF0D2736)
```
- [ ] **Step 2:** In `NetBand`, choose brush by theme; replace `.background(BudgetGradients.NetBand)` with:
```kotlin
            .background(if (BudgetTheme.isLight) BudgetGradients.NetBandLight else BudgetGradients.NetBand)
```
Import `com.example.budgettracker.ui.theme.BudgetTheme`.
- [ ] **Step 3:** Pass theme-aware colors into the cells. Compute once in `NetBand`:
```kotlin
    val light = BudgetTheme.isLight
    val label = if (light) NetLabelLight else NetLabel
    val ink = if (light) NetInkLight else NetInk
    val income = BudgetTheme.semanticColors.income   // #74D9B5 dark, #1F7A5A light
    val overage = BudgetTheme.semanticColors.overage // #FFB4A8 dark, #B3261E light
```
Update the three `NetCell(...)` calls to pass `label`, and value colors `income` / `ink` / `if (net >= 0) income else overage`; update `NetCell` signature to take `labelColor: Color` and use it for the label `Text` + the `◎ Plan` sub-row (`tint`/`color = labelColor`). (Currently the cell uses module `NetLabel` directly — thread `labelColor` through instead.)
- [ ] **Step 4:** Compile + (verified on emulator in Task 7).

### Task 3: GradientButton light variants (the bug fix)

**Files:** Modify `ui/components/GradientButton.kt`

- [ ] **Step 1:** Add `private val TonalInkLight = Color(0xFF0D2736)`.
- [ ] **Step 2:** Replace the brush + content-color selection with theme-aware versions:
```kotlin
    val light = BudgetTheme.isLight
    val brush = when {
        tone == GradientButtonTone.FILLED -> if (light) BudgetGradients.FilledButtonLight else BudgetGradients.FilledButton
        else -> if (light) BudgetGradients.TonalButtonLight else BudgetGradients.TonalButton
    }
    val contentColor = if (!enabled) scheme.onSurfaceVariant else when {
        tone == GradientButtonTone.FILLED -> if (light) Color.White else FilledInk
        else -> if (light) TonalInkLight else TonalInk
    }
```
Import `com.example.budgettracker.ui.theme.BudgetTheme` (and `Color` already imported).
- [ ] **Step 3:** Compile.

### Task 4: GradientBanner light variants

**Files:** Modify `ui/components/GradientBanner.kt`

- [ ] **Step 1:** Replace the brush + content selection:
```kotlin
    val light = BudgetTheme.isLight
    val brush = when (tone) {
        BannerTone.INFO -> if (light) BudgetGradients.BannerInfoLight else BudgetGradients.BannerInfo
        BannerTone.AMBER -> if (light) BudgetGradients.BannerAmberLight else BudgetGradients.BannerAmber
    }
    val content = when (tone) {
        BannerTone.INFO -> if (light) Color(0xFF0D2736) else Color(0xFFCDE3F2)
        BannerTone.AMBER -> if (light) Color(0xFF6A3B16) else Color(0xFFFBE3CC)
    }
```
Import `com.example.budgettracker.ui.theme.BudgetTheme`.
- [ ] **Step 2:** Compile.

### Task 5: Plan sticky bar light variant

**Files:** Modify `ui/screens/plan/PlanComponents.kt`

- [ ] **Step 1:** In `PlanSaveBar`, compute theme-aware bar:
```kotlin
    val light = BudgetTheme.isLight
    val barBrush = if (light) BudgetGradients.StickyBarLight else BudgetGradients.StickyBar
    val barBorder = if (light) Color(0x140D2736) else Color.White.copy(alpha = 0.07f)
    val labelColor = if (light) Color(0xFF6B7A84) else StickyLabel
```
Use `barBrush` in `.background(...)`, `barBorder` in `.border(1.dp, barBorder, StickyShape)`, and `labelColor` for the count `Text`. (The Save `GradientButton` FILLED already goes navy in light via Task 3.) Import `com.example.budgettracker.ui.theme.BudgetTheme`.
- [ ] **Step 2:** Compile.

### Task 6: Bottom nav light pill + BudgetCard border (align `isLight`)

**Files:** Modify `ui/BudgetApp.kt`, `ui/components/BudgetCard.kt`

- [ ] **Step 1:** In `BudgetApp.BottomNav`, replace the light branch `NavigationBarItemDefaults.colors()` with a navy-tinted pill:
```kotlin
    } else {
        NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF0D2736),
            selectedTextColor = Color(0xFF0D2736),
            indicatorColor = Color(0xFFCFE3F1),
            unselectedIconColor = scheme.onSurfaceVariant,
            unselectedTextColor = scheme.onSurfaceVariant,
        )
    }
```
(Keep the existing `isDark` computation for the surface/gradient; this only changes the light item colors.)
- [ ] **Step 2:** In `BudgetCard`, in the light (`else`) branch, add a faint border so white cards read on the light canvas:
```kotlin
        Surface(
            modifier.fillMaxWidth(),
            shape = CardShape,
            color = scheme.surface,
            border = BorderStroke(1.dp, Color(0x140D2736)),
        ) { Column(content = content) }
```
Add imports `androidx.compose.foundation.BorderStroke`, `androidx.compose.ui.graphics.Color`.
- [ ] **Step 3:** Compile.

### Task 7: Verify on emulator, docs, commit

**Files:** Modify `CLAUDE.md`

- [ ] **Step 1:** Build + install: `./gradlew :app:assembleDebug`, install via mobile-mcp.
- [ ] **Step 2:** Settings → Theme → **Light**. Walk Log / Plan / Report / Categories / Recurring and confirm: NetBand hero is light, **FILLED buttons are visible (navy + white)**, tonal buttons are light tint, banners are light tints, the Plan sticky bar is a white card with a navy Save, the bottom-nav active pill is a navy tint, cards have a faint border, the FAB stays navy. Switch back to **Dark** and confirm it's visually unchanged.
- [ ] **Step 3:** Update `CLAUDE.md` design-essentials: note light theme is fully-light/airy with `BudgetTheme.isLight`-driven light brand variants (every gradient component has a light variant; FAB stays navy).
- [ ] **Step 4:** `./gradlew :app:testDebugUnitTest` (green); commit the spec + plan + mockup + change on `feat/light-theme`; push; open PR (note: stacked on #33 — merge #33 first).

---

## Self-review

- **Spec coverage:** isLight + light brushes → Task 1; NetBand → Task 2; GradientButton (FILLED fix) → Task 3; banners → Task 4; sticky bar → Task 5; bottom-nav pill + card border → Task 6; testing/docs → Task 7. FAB unchanged (not in any task — correct). Semantic colors unchanged (NetBand reuses `BudgetTheme.semanticColors`).
- **Placeholder scan:** none — concrete colors/code throughout.
- **Symbol consistency:** light brushes `NetBandLight`/`FilledButtonLight`/`TonalButtonLight`/`StickyBarLight`/`BannerInfoLight`/`BannerAmberLight` (defined Task 1) are referenced by exact name in Tasks 2–5; `BudgetTheme.isLight` (Task 1) used in Tasks 2–6; light inks `NetLabelLight`/`NetInkLight`/`TonalInkLight` defined where used.
