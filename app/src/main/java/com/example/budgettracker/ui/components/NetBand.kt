package com.example.budgettracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.ui.theme.BudgetTheme
import com.example.budgettracker.ui.theme.money

/** Hero band showing month-to-date Income · Expense · Net (design §5.1 / Net band). */
@Composable
fun NetBand(income: Long, expense: Long, net: Long, currency: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            NetCell("Income", Money.format(income, currency), BudgetTheme.semanticColors.income, Modifier.weight(1f))
            NetCell("Expense", Money.format(expense, currency), MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f))
            NetCell(
                "Net",
                Money.format(net, currency),
                if (net >= 0) BudgetTheme.semanticColors.income else BudgetTheme.semanticColors.overage,
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NetCell(label: String, value: String, valueColor: Color, modifier: Modifier) {
    Column(modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.money, color = valueColor, maxLines = 1)
    }
}
