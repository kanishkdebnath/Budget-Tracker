package com.example.budgettracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector

const val SETTINGS_ROUTE = "settings"

/** The five bottom-nav destinations (design §2). Settings is reached from the top-bar gear, not a tab. */
enum class TopLevelDest(
    val route: String,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
) {
    LOG("log", "Log", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    PLAN("plan", "Plan", Icons.Filled.TrackChanges, Icons.Outlined.TrackChanges),
    REPORT("report", "Report", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    CATEGORIES("categories", "Categories", Icons.Filled.Category, Icons.Outlined.Category),
    RECURRING("recurring", "Recurring", Icons.Filled.Autorenew, Icons.Outlined.Autorenew),
    ;

    companion object {
        val routes: Set<String> = entries.map { it.route }.toSet()
        fun fromRoute(route: String?): TopLevelDest? = entries.firstOrNull { it.route == route }
    }
}
