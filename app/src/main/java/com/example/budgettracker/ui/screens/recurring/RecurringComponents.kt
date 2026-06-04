package com.example.budgettracker.ui.screens.recurring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.ui.components.BannerTone
import com.example.budgettracker.ui.components.GradientBanner
import com.example.budgettracker.ui.components.GradientButton
import com.example.budgettracker.ui.components.GradientButtonTone
import com.example.budgettracker.ui.theme.BudgetTheme

@Composable
fun RecurringDueBanner(count: Int, modifier: Modifier = Modifier) {
    GradientBanner(
        "$count recurring ${if (count == 1) "entry is" else "entries are"} due this month.",
        BannerTone.AMBER,
        modifier,
        leadingIcon = Icons.Outlined.Schedule,
    )
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier.padding(start = 4.dp, top = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun RecurringCard(
    row: RecurringRow,
    currency: String,
    onClick: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val applied = row.state == RecurringState.APPLIED
    val containerColor =
        if (applied) BudgetTheme.semanticColors.income.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier.weight(1f).alpha(if (row.state == RecurringState.INACTIVE) 0.5f else 1f),
            ) {
                Text(row.template.label, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.size(2.dp))
                Text(
                    "${row.categoryName} · Day ${row.template.dayOfMonth} · ${Money.format(row.template.amount, currency)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            when (row.state) {
                RecurringState.APPLIED -> AppliedPill()
                RecurringState.ACTIONABLE -> GradientButton("Apply", onClick = onApply, tone = GradientButtonTone.TONAL)
                RecurringState.INACTIVE -> StatePill("Inactive", MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AppliedPill() {
    val color = BudgetTheme.semanticColors.income
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.16f)) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
            Spacer(Modifier.width(4.dp))
            Text("Applied", style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Composable
private fun StatePill(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, color = color)
    }
}
