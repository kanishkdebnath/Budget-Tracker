package com.example.budgettracker.ui.screens.plan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.budgettracker.domain.time.MonthUtils
import com.example.budgettracker.ui.components.EmptyState

@Composable
fun PlanScreen(month: String, modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.TrackChanges,
        title = "Set targets for ${MonthUtils.monthLabel(month)}",
        subtitle = "Plan how much to spend per category this month.",
        modifier = modifier,
    )
}
