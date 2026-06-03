package com.example.budgettracker.ui.screens.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.budgettracker.ui.components.EmptyState

@Composable
fun CategoriesScreen(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.Category,
        title = "Categories",
        subtitle = "Your groups and categories will appear here.",
        modifier = modifier,
    )
}
