package com.example.budgettracker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.budgettracker.domain.time.MonthUtils
import com.example.budgettracker.ui.components.BackTopBar
import com.example.budgettracker.ui.components.BudgetBackground
import com.example.budgettracker.ui.components.MonthNavTopBar
import com.example.budgettracker.ui.components.SectionTopBar
import com.example.budgettracker.ui.navigation.SETTINGS_ROUTE
import com.example.budgettracker.ui.navigation.TopLevelDest
import com.example.budgettracker.ui.screens.categories.CategoriesScreen
import com.example.budgettracker.ui.screens.log.LogScreen
import com.example.budgettracker.ui.screens.plan.PlanScreen
import com.example.budgettracker.ui.screens.recurring.RecurringScreen
import com.example.budgettracker.ui.screens.report.ReportScreen
import com.example.budgettracker.ui.screens.settings.SettingsScreen
import com.example.budgettracker.ui.theme.BudgetGradients
import com.example.budgettracker.ui.theme.BudgetTrackerTheme
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId

private const val MAIN_ROUTE = "main"
private val MONTH_NAV_DESTS = setOf(TopLevelDest.LOG, TopLevelDest.PLAN, TopLevelDest.REPORT)

@Composable
fun BudgetApp() {
    val navController = rememberNavController()
    BudgetBackground {
        NavHost(navController = navController, startDestination = MAIN_ROUTE) {
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

/** The 5 tabs as a swipeable [HorizontalPager], synced to the bottom nav (design: lively nav). */
@Composable
private fun MainPager(onOpenSettings: () -> Unit) {
    val pages = TopLevelDest.entries
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val current = pages[pagerState.currentPage]

    // Shared shell state for the month-nav screens and the per-screen top-bar actions.
    var month by rememberSaveable {
        mutableStateOf(MonthUtils.monthOf(System.currentTimeMillis(), ZoneId.systemDefault()))
    }
    var categoriesSearchActive by rememberSaveable { mutableStateOf(false) }
    var reportExportOpen by rememberSaveable { mutableStateOf(false) }

    // Back glides to Log; on Log it's disabled so the system default exits the app (WhatsApp-style).
    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        // contentColorFor(Transparent) is Unspecified → text would render black; pin it.
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
            beyondViewportPageCount = pages.size - 1, // keep all 5 tabs alive for instant, state-preserving swipes
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

/**
 * Bottom navigation, synced to the pager. In dark themes it carries the navy [BudgetGradients.BottomNav]
 * surface with a navy active-pill (the M3 baseline `surfaceContainer` reads as a muddy brown here);
 * light keeps the M3 default. The selected icon scales up and shakes when its tab becomes active.
 */
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
                            scaleX = scale
                            scaleY = scale
                            rotationZ = shake
                        },
                    )
                },
                label = { Text(dest.navLabel) },
                colors = itemColors,
            )
        }
    }
}

/** One-shot springy wiggle (rotation, deg) that replays each time [trigger] becomes true. */
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
                    -12f at 70
                    10f at 150
                    -6f at 230
                    3f at 320
                    0f at 420
                },
            )
        }
    }
    return rotation.value
}

@Preview(name = "App — Light")
@Composable
private fun BudgetAppLightPreview() {
    BudgetTrackerTheme(darkTheme = false) { BudgetApp() }
}

@Preview(name = "App — Dark")
@Composable
private fun BudgetAppDarkPreview() {
    BudgetTrackerTheme(darkTheme = true) { BudgetApp() }
}
