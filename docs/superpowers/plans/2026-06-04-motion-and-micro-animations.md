# Motion & Micro-animations Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. This is UI/motion work — most verification is **on the emulator** (mobile-mcp), not unit tests; unit-test only pure helpers.

**Goal:** Swipe between the 5 main tabs, make the nav bar react when a tab becomes active, and add a curated micro-animation set — lively on navigation/chrome, calm on data.

**Architecture:** Replace the 5-tab `NavHost` with a `HorizontalPager` inside a two-route shell (`main` + `settings`). Animate the nav (shake + indicator), press feedback (FAB/chips), and data (NetBand roll, card stagger) with Compose animation primitives. No new dependencies.

**Tech Stack:** Compose `HorizontalPager` (foundation), `rememberPagerState`, `Animatable`/`animateFloatAsState`/`AnimatedContent`, `graphicsLayer`, `BackHandler` (activity-compose), M3 `NavigationBar`.

Spec: `docs/superpowers/specs/2026-06-04-motion-and-micro-animations-design.md`.

---

## Phase 1 — Swipe nav + nav-icon shake + animated indicator (PR 1)

The headline. Restructures `BudgetApp` so the 5 tabs become pager pages, syncs the bottom nav, and animates the nav.

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/BudgetApp.kt` (the nav shell — pager, sync, back handler, animated bottom nav)
- Reference (no change): `app/src/main/java/com/example/budgettracker/ui/navigation/Destinations.kt` (`TopLevelDest.entries` is the page order, index 0 = LOG), `ui/components/BudgetTopBars.kt`

### Task 1.1 — Restructure the shell into `main` + `settings` with a HorizontalPager

- [ ] **Step 1: Rewrite `BudgetApp` to a two-route NavHost.** The `main` route hosts the pager shell; `settings` stays a pushed route. Hoisted state (`month`, `categoriesSearchActive`, `reportExportOpen`) moves into the `MainPager` composable.

```kotlin
@Composable
fun BudgetApp() {
    val navController = rememberNavController()
    BudgetBackground {
        NavHost(navController, startDestination = MAIN_ROUTE) {
            composable(MAIN_ROUTE) {
                MainPager(onOpenSettings = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } })
            }
            composable(SETTINGS_ROUTE) {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    topBar = { BackTopBar("Settings", onBack = { navController.popBackStack() }) },
                ) { padding -> SettingsScreen(Modifier.padding(padding)) }
            }
        }
    }
}
```
Add `private const val MAIN_ROUTE = "main"` near the top of the file.

- [ ] **Step 2: Add the `MainPager` composable** with the pager, hoisted state, top bar by current page, animated bottom nav, and the glide-to-Log back handler.

```kotlin
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainPager(onOpenSettings: () -> Unit) {
    val pages = TopLevelDest.entries
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val current = pages[pagerState.currentPage]

    var month by rememberSaveable {
        mutableStateOf(MonthUtils.monthOf(System.currentTimeMillis(), ZoneId.systemDefault()))
    }
    var categoriesSearchActive by rememberSaveable { mutableStateOf(false) }
    var reportExportOpen by rememberSaveable { mutableStateOf(false) }

    // Back glides to Log; on Log it's disabled so the system default exits the app.
    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            when (current) {
                in MONTH_NAV_DESTS -> MonthNavTopBar(
                    monthLabel = MonthUtils.monthLabel(month),
                    onPreviousMonth = { month = YearMonth.parse(month).minusMonths(1).toString() },
                    onNextMonth = { month = YearMonth.parse(month).plusMonths(1).toString() },
                    onSettings = onOpenSettings,
                    onExport = if (current == TopLevelDest.REPORT) ({ reportExportOpen = true }) else null,
                )
                TopLevelDest.CATEGORIES -> SectionTopBar(
                    "Categories", onOpenSettings,
                    onSearch = { categoriesSearchActive = !categoriesSearchActive },
                )
                TopLevelDest.RECURRING -> SectionTopBar("Recurring", onOpenSettings)
                else -> Unit
            }
        },
        bottomBar = {
            BottomNav(
                selectedIndex = pagerState.currentPage,
                settledIndex = pagerState.settledPage,
                onTabClick = { i -> scope.launch { pagerState.animateScrollToPage(i) } },
            )
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            beyondViewportPageCount = pages.size - 1, // keep all 5 alive
            key = { pages[it].route },
        ) { page ->
            when (pages[page]) {
                TopLevelDest.LOG -> LogScreen(month = month, onMonthChange = { month = it })
                TopLevelDest.PLAN -> PlanScreen(month)
                TopLevelDest.REPORT -> ReportScreen(
                    month,
                    exportSheetOpen = reportExportOpen,
                    onExportSheetClose = { reportExportOpen = false },
                )
                TopLevelDest.CATEGORIES -> CategoriesScreen(
                    searchActive = categoriesSearchActive,
                    onSearchClose = { categoriesSearchActive = false },
                )
                TopLevelDest.RECURRING -> RecurringScreen()
            }
        }
    }
}
```
Imports to add: `androidx.activity.compose.BackHandler`, `androidx.compose.foundation.ExperimentalFoundationApi`, `androidx.compose.foundation.pager.HorizontalPager`, `androidx.compose.foundation.pager.rememberPagerState`, `androidx.compose.foundation.layout.fillMaxSize`, `androidx.compose.material3.BackTopBar` is from our components, keep existing `BackTopBar`/`MonthNavTopBar`/`SectionTopBar` imports. Remove the old `currentRoute`/`currentDest`/`fromRoute` and per-screen `composable(...)` wiring.

- [ ] **Step 3: Compile.** Run: `./gradlew :app:compileDebugKotlin` — expect BUILD SUCCESSFUL (it won't yet — `BottomNav` signature changes in Task 1.2).

### Task 1.2 — Animated bottom nav (shake + scale + synced indicator)

- [ ] **Step 1: Rewrite `BottomNav`** to take `selectedIndex` (highlight + indicator follow this), `settledIndex` (shake trigger), and `onTabClick`. Each item shakes (`rotationZ` keyframes) when it becomes the settled page, and the selected icon scales to 1.1.

```kotlin
@Composable
private fun BottomNav(selectedIndex: Int, settledIndex: Int, onTabClick: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f
    val itemColors = if (isDark) {
        NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFFC5E5F9),
            selectedTextColor = scheme.onSurface,
            indicatorColor = Color(0xFF1B4561),
            unselectedIconColor = scheme.onSurfaceVariant,
            unselectedTextColor = scheme.onSurfaceVariant,
        )
    } else {
        NavigationBarItemDefaults.colors()
    }
    NavigationBar(
        containerColor = if (isDark) Color.Transparent else scheme.surface,
        tonalElevation = if (isDark) 0.dp else NavigationBarDefaults.Elevation,
        modifier = if (isDark) Modifier.background(BudgetGradients.BottomNav) else Modifier,
    ) {
        TopLevelDest.entries.forEachIndexed { index, dest ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onTabClick(index) },
                icon = {
                    val scale by animateFloatAsState(if (selected) 1.1f else 1f, label = "navScale")
                    val shake = rememberNavShake(trigger = settledIndex == index)
                    Icon(
                        if (selected) dest.filledIcon else dest.outlinedIcon,
                        contentDescription = dest.label,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale; scaleY = scale; rotationZ = shake
                        },
                    )
                },
                label = { Text(dest.navLabel) },
                colors = itemColors,
            )
        }
    }
}
```

- [ ] **Step 2: Add the `rememberNavShake` helper** — a one-shot rotation wiggle that replays each time `trigger` becomes true.

```kotlin
@Composable
private fun rememberNavShake(trigger: Boolean): Float {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger) {
            rotation.snapTo(0f)
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 420
                    -12f at 70; 10f at 150; -6f at 230; 3f at 320; 0f at 420
                },
            )
        }
    }
    return rotation.value
}
```
Imports to add: `androidx.compose.animation.core.Animatable`, `androidx.compose.animation.core.animateFloatAsState`, `androidx.compose.animation.core.keyframes`, `androidx.compose.ui.graphics.graphicsLayer`, `androidx.compose.runtime.LaunchedEffect`, `androidx.compose.runtime.remember`, `androidx.compose.runtime.getValue`.

- [ ] **Step 3: Compile.** Run: `./gradlew :app:compileDebugKotlin` — expect BUILD SUCCESSFUL.

### Task 1.3 — Verify on emulator & commit

- [ ] **Step 1: Build + install.** `./gradlew :app:assembleDebug` then install via mobile-mcp.
- [ ] **Step 2: Verify** — swipe Log↔Plan↔Report↔Categories↔Recurring both directions (smooth, follows finger); on arrival the tab's icon shakes and the indicator/scale animate; tap nav items still works (animates across); top bar switches correctly (month-nav vs section, export icon only on Report, search only on Categories); filter chips still scroll horizontally and Categories drag-to-reorder still works; **back** on a non-Log tab glides to Log, back on Log exits; Settings still opens via gear and back returns to the pager.
- [ ] **Step 3: Tests + commit.** `./gradlew :app:testDebugUnitTest` (green), then commit the spec + plan + Phase 1 on `feat/motion-swipe-nav`, push, open PR 1.

---

## Phase 2 — Press feedback: FAB + chips (PR 2)

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/components/GradientFab.kt` (press scale)
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/log/LogComponents.kt` (`LogFilterChips` pop) and `app/src/main/java/com/example/budgettracker/ui/screens/categories/CategoryComponents.kt` (`CategoryFilterChips` pop)

### Task 2.1 — FAB press scale

- [ ] **Step 1: Add a pressed-scale to `GradientFab`.** Collect the press interaction and scale the whole pill to 0.96 with a spring.

```kotlin
val interactionSource = remember { MutableInteractionSource() }
val pressed by interactionSource.collectIsPressedAsState()
val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "fabScale")
// on the root Surface/Box modifier chain: .graphicsLayer { scaleX = scale; scaleY = scale }
// pass interactionSource to the clickable: .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
```
Imports: `androidx.compose.foundation.interaction.MutableInteractionSource`, `androidx.compose.foundation.interaction.collectIsPressedAsState`, `androidx.compose.animation.core.animateFloatAsState`, `androidx.compose.animation.core.spring`, `androidx.compose.animation.core.Spring`, `androidx.compose.ui.graphics.graphicsLayer`, `androidx.compose.foundation.LocalIndication`.

- [ ] **Step 2: Compile + verify** the FAB dips on press and springs back (Log/Recurring/Categories FABs). Adjust stiffness if too subtle/strong.

### Task 2.2 — Filter chip select "pop"

- [ ] **Step 1: Wrap each `FilterChip` in a one-shot scale bump on becoming selected.** Add a small helper used by both `LogFilterChips` and `CategoryFilterChips`:

```kotlin
@Composable
fun Modifier.chipPop(selected: Boolean): Modifier {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(selected) {
        if (selected) {
            scale.snapTo(1f)
            scale.animateTo(1f, keyframes { durationMillis = 220; 1.06f at 90; 1f at 220 })
        }
    }
    return this.graphicsLayer { scaleX = scale.value; scaleY = scale.value }
}
```
Place this in `ui/components/` (e.g. a new `AnimationModifiers.kt`) so both chip rows share it. Apply `Modifier.chipPop(filter == selected)` to each `FilterChip`'s modifier.

- [ ] **Step 2: Compile, verify on emulator** (tapping a chip pops it), tests green, commit + PR 2 on `feat/motion-press-feedback`.

---

## Phase 3 — NetBand number roll (PR 3)

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/components/NetBand.kt` (wrap each value in `AnimatedContent`)

### Task 3.1 — Animate value changes

- [ ] **Step 1: Replace each value `Text` with `AnimatedContent`** keyed on the formatted string, sliding+fading old→new (~300ms). Example for one cell:

```kotlin
AnimatedContent(
    targetState = Money.formatShort(net, currency),
    transitionSpec = {
        (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
    },
    label = "netValue",
) { text -> Text(text, style = ..., color = ...) }
```
Apply to Income, Expense, Net values. Leave labels and the `◎ Plan` sub-row static. Imports: `androidx.compose.animation.AnimatedContent`, `androidx.compose.animation.fadeIn/fadeOut`, `androidx.compose.animation.slideInVertically/slideOutVertically`, `androidx.compose.animation.togetherWith`.

- [ ] **Step 2: Compile, verify** — change the month (Log/Plan/Report) or edit a Plan target and watch the NetBand numbers slide/fade (calm, no jank). Tests green, commit + PR 3 on `feat/motion-netband-roll`.

---

## Phase 4 — Card entrance stagger (PR 4)

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/components/BudgetCard.kt` (optional `index`/`appearKey` entrance) **or** apply an entrance modifier at each list call site (Log `DateCard`, Report/Categories/Plan group cards).

### Task 4.1 — One-shot entrance

- [ ] **Step 1: Add a reusable entrance modifier** in `ui/components/AnimationModifiers.kt`: fade + slide-up on first composition, staggered by index, replayed when `appearKey` changes (e.g. the month).

```kotlin
@Composable
fun Modifier.cardEntrance(index: Int, appearKey: Any?): Modifier {
    val anim = remember(appearKey) { Animatable(0f) }
    LaunchedEffect(appearKey) {
        anim.snapTo(0f)
        delay((index.coerceAtMost(6) * 40).toLong())
        anim.animateTo(1f, tween(durationMillis = 260))
    }
    return this.graphicsLayer {
        alpha = anim.value
        translationY = (1f - anim.value) * 16.dp.toPx()
    }
}
```
Imports: `androidx.compose.animation.core.tween`, `kotlinx.coroutines.delay`.

- [ ] **Step 2: Apply at the list call sites** — Log `DateCard` (key = month), Report/Categories/Plan group cards (key = month or filter). Pass the item index from the `items { }` / `forEachIndexed`.
- [ ] **Step 3: Compile, verify** — load a screen / change month and watch cards fade+slide in with a slight stagger (subtle, not janky). Tests green, commit + PR 4 on `feat/motion-card-entrance`.

---

## Self-review notes

- **Spec coverage:** §1 nav architecture → Phase 1 (Tasks 1.1–1.2); back-glide → 1.1 Step 2; gesture coexistence → 1.3 Step 2 (verify). §2 shake/indicator → 1.2; press feedback → Phase 2; NetBand roll → Phase 3; card stagger → Phase 4. §4 testing → each phase's verify step. §5 non-goals respected (settle-based indicator, NetBand-only data roll, Settings pushed).
- **Risk:** pager vs horizontal-scroll chips / drag-reorder — explicitly verified in 1.3; if a conflict appears, gate the pager with `nestedScroll` or constrain chip rows. Tune shake keyframes / scales on-device.
