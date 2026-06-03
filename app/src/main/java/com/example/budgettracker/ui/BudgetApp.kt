package com.example.budgettracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budgettracker.domain.time.MonthUtils
import com.example.budgettracker.ui.components.BackTopBar
import com.example.budgettracker.ui.components.BudgetBackground
import com.example.budgettracker.ui.components.MonthNavTopBar
import com.example.budgettracker.ui.components.SectionTopBar
import com.example.budgettracker.ui.theme.BudgetGradients
import com.example.budgettracker.ui.navigation.SETTINGS_ROUTE
import com.example.budgettracker.ui.navigation.TopLevelDest
import com.example.budgettracker.ui.screens.categories.CategoriesScreen
import com.example.budgettracker.ui.screens.log.LogScreen
import com.example.budgettracker.ui.screens.plan.PlanScreen
import com.example.budgettracker.ui.screens.recurring.RecurringScreen
import com.example.budgettracker.ui.screens.report.ReportScreen
import com.example.budgettracker.ui.screens.settings.SettingsScreen
import com.example.budgettracker.ui.theme.BudgetTrackerTheme
import java.time.YearMonth
import java.time.ZoneId

private val MONTH_NAV_DESTS = setOf(TopLevelDest.LOG, TopLevelDest.PLAN, TopLevelDest.REPORT)

@Composable
fun BudgetApp() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentDest = TopLevelDest.fromRoute(currentRoute)

    // Shared month (YYYY-MM) for Log/Plan/Report; feature ViewModels take this over in later phases.
    var month by rememberSaveable {
        mutableStateOf(MonthUtils.monthOf(System.currentTimeMillis(), ZoneId.systemDefault()))
    }
    var categoriesSearchActive by rememberSaveable { mutableStateOf(false) }
    val goToSettings = { navController.navigate(SETTINGS_ROUTE) { launchSingleTop = true } }

    BudgetBackground {
        Scaffold(
            containerColor = Color.Transparent,
            // contentColorFor(Transparent) is Unspecified → text would render black; pin it.
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                when {
                    currentDest in MONTH_NAV_DESTS -> MonthNavTopBar(
                        monthLabel = MonthUtils.monthLabel(month),
                        onPreviousMonth = { month = YearMonth.parse(month).minusMonths(1).toString() },
                        onNextMonth = { month = YearMonth.parse(month).plusMonths(1).toString() },
                        onSettings = goToSettings,
                    )
                    currentDest == TopLevelDest.CATEGORIES -> SectionTopBar(
                    "Categories", goToSettings,
                    onSearch = { categoriesSearchActive = !categoriesSearchActive },
                )
                    currentDest == TopLevelDest.RECURRING -> SectionTopBar("Recurring", goToSettings)
                    currentRoute == SETTINGS_ROUTE -> BackTopBar("Settings", onBack = { navController.popBackStack() })
                }
            },
            bottomBar = {
                if (currentRoute in TopLevelDest.routes) BottomNav(currentDest, navController)
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = TopLevelDest.LOG.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(TopLevelDest.LOG.route) { LogScreen(month = month, onMonthChange = { month = it }) }
                composable(TopLevelDest.PLAN.route) { PlanScreen(month) }
                composable(TopLevelDest.REPORT.route) { ReportScreen(month) }
                composable(TopLevelDest.CATEGORIES.route) {
                CategoriesScreen(
                    searchActive = categoriesSearchActive,
                    onSearchClose = { categoriesSearchActive = false },
                )
            }
                composable(TopLevelDest.RECURRING.route) { RecurringScreen() }
                composable(SETTINGS_ROUTE) { SettingsScreen() }
            }
        }
    }
}

/**
 * Bottom navigation. In dark themes it carries the navy [BudgetGradients.BottomNav] surface with a
 * navy active-pill (design "bnav"); the M3 baseline `surfaceContainer` it would otherwise use reads
 * as a muddy brown in this navy scheme. Light themes keep the M3 default.
 */
@Composable
private fun BottomNav(currentDest: TopLevelDest?, navController: NavController) {
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
        TopLevelDest.entries.forEach { dest ->
            val selected = currentDest == dest
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        if (selected) dest.filledIcon else dest.outlinedIcon,
                        contentDescription = dest.label,
                    )
                },
                label = { Text(dest.navLabel) },
                colors = itemColors,
            )
        }
    }
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
