package com.example.budgettracker.ui.screens.recurring

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.budgettracker.ui.components.EmptyState

@Composable
fun RecurringScreen(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.Autorenew,
        title = "No recurring entries",
        subtitle = "Add templates like Salary or Rent to apply each month.",
        modifier = modifier,
    )
}
