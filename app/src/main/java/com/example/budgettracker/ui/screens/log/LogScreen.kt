package com.example.budgettracker.ui.screens.log

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.budgettracker.domain.time.MonthUtils
import com.example.budgettracker.ui.components.EmptyState

@Composable
fun LogScreen(month: String, modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
        title = "No transactions in ${MonthUtils.monthLabel(month)}",
        subtitle = "Tap + to add your first income or expense.",
        modifier = modifier,
    )
}
