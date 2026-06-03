package com.example.budgettracker.ui.screens.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.budgettracker.domain.time.MonthUtils
import com.example.budgettracker.ui.components.EmptyState

@Composable
fun ReportScreen(month: String, modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.BarChart,
        title = "Nothing to report for ${MonthUtils.monthLabel(month)}",
        subtitle = "Add transactions and targets to see your monthly breakdown.",
        modifier = modifier,
    )
}
