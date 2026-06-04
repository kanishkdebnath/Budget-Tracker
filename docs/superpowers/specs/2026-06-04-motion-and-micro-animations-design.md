# Motion & Micro-animations — Design

**Goal:** Make the UI feel alive without losing the calm "trusted-bank navy" personality — swipe between the main tabs, react in the nav bar when a tab becomes active, and add a curated set of micro-animations. Motion philosophy: **lively on navigation/chrome, calm on data.**

**Tech:** Jetpack Compose `HorizontalPager` (foundation), `Animatable` / `animateFloatAsState` / `AnimatedContent`, M3 `NavigationBar`. No new dependencies.

---

## 1. Navigation architecture (enables the swipe)

Today the 5 tabs are separate `NavHost` destinations, so they can't be swiped between. We restructure the shell:

- The top-level `NavHost` keeps **two routes**: `main` (the pager shell) and `settings`. The top-bar gear pushes `settings`; **back from settings** returns to `main`. Settings is intentionally **not** a swipe tab.
- The `main` composable owns the `Scaffold` (transparent, over `BudgetBackground`) with a **`HorizontalPager` of the 5 tabs** (Log · Plan · Report · Categories · Recurring) as its content, plus the bottom `NavigationBar`.
- **Pager ↔ nav sync:** the selected tab follows `pagerState.settledPage`; tapping a nav item calls `animateScrollToPage(index)`; swiping updates the selected tab. The **top bar switches by current page** (month-nav for Log/Plan/Report, `SectionTopBar` for Categories/Recurring).
- **All 5 pages kept alive** via `beyondViewportPageCount` (it's only 5 light screens) so swiping is instant and each tab preserves scroll position / open search / etc. Hoisted shell state (`month`, `categoriesSearchActive`, `reportExportOpen`) stays in `main` and is passed to the pages.
- **Back behavior (approved):** a `BackHandler` glides to Log (`animateScrollToPage(0)`) when on any other tab; on Log it is disabled so the system default exits the app. (WhatsApp-style.)
- **Gesture coexistence:** the horizontal-scroll filter chips (Log/Categories) and drag-to-reorder (Categories, long-press) must keep working alongside the pager — inner horizontal scroll consumes the drag, reorder is gated behind long-press. **Verify on-device.**
- *Rejected alternative:* keep `NavHost` and trigger `navigate()` from a swipe gesture — it can't follow the finger, so it feels like a button, not a swipe.

## 2. Animation specs

**Lively (navigation / chrome):**

- **Tab swipe** — the `HorizontalPager` drag itself (follows the finger, settles to a page).
- **Nav-icon shake** — when a tab *becomes* selected (swipe or tap), its icon plays a one-shot springy wiggle: a `rotationZ` keyframe roughly `0 → -12° → 10° → -6° → 0` over ~420ms, via a per-item `Animatable` triggered by `LaunchedEffect(selected)`. Applied with `Modifier.graphicsLayer`.
- **Animated nav indicator** — the selected tab updates on the pager's *settled* page, so M3's built-in indicator animates (slides/fades) to the new tab; the selected icon also scales to ~1.1 (`animateFloatAsState`). *Finger-following indicator (driven by `currentPageOffsetFraction`) is a deliberate non-goal for v1 — settle-based animation only.*
- **Press feedback** — `GradientFab` scales to ~0.96 while pressed and springs back (`interactionSource` + `animateFloatAsState`, spring). Filter chips give a small one-shot scale "pop" (~1.0 → 1.06 → 1.0) when they become selected.

**Calm (data):**

- **NetBand number roll** — each cell's formatted value (`Money.formatShort`) is wrapped in `AnimatedContent`; on change it slides + fades (old up/out, new in) over ~300ms. Applied in the shared `NetBand`, so Log/Plan/Report all benefit. Labels and the `◎ Plan` sub-row are unaffected.
- **Card entrance stagger** — list cards (Log `DateCard`s, group cards) animate in on first appearance (and re-run on month change): `alpha 0 → 1` + `translationY ~16dp → 0`, staggered by item index (~40ms steps, capped). Keyed by month so changing months replays it.

## 3. Rollout (one concern per PR)

1. **Swipe nav + nav-icon shake + animated indicator** — the nav shell (`BudgetApp`, `BudgetTopBars`, navigation). The headline; folds in this spec + the plan.
2. **Press feedback** — `GradientFab` + the filter chips (`LogComponents`, `CategoryComponents`).
3. **NetBand number roll** — the shared `NetBand` component.
4. **Card entrance stagger** — Log `DateCard`, Categories/Report/Plan group cards.

## 4. Testing & verification

- Pure/unit-testable logic is minimal here (it's UI motion). Keep any extracted helpers (e.g. a `tabForPage` mapping) pure and unit-test them.
- Each PR is **verified on the emulator** (mobile-mcp): swipe between all tabs both directions, confirm the nav icon shakes on arrival, the indicator animates, gestures (chips scroll, reorder) still work, back glides to Log then exits, and the data animations read as calm (no jank).
- `:app:testDebugUnitTest` stays green; `assembleDebug` clean per PR.

## 5. Non-goals (v1)

- Finger-following nav indicator (settle-based only).
- Shared-element / hero transitions between screens.
- Animating money on the *data tables* (Report rows, Plan inputs) — only the `NetBand` hero rolls; tables stay still (calm-data principle).
- Settings as a swipe tab (stays a pushed route).
