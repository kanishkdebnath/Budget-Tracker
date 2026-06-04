package com.example.budgettracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.ui.theme.BudgetGradients

// The Net band is a fixed navy gradient hero — its text is always light, independent of light/dark theme.
private val NetLabel = Color(0xFF9FB1BD)
private val NetInk = Color(0xFFE8EEF2)
private val NetIncome = Color(0xFF74D9B5)
private val NetOverage = Color(0xFFFFB4A8)
private val NetShape = RoundedCornerShape(16.dp)

/**
 * Hero band showing Income · Expense · Net in short notation (design §5.1). On a navy gradient.
 * When [plannedIncome]/[plannedExpense] are provided, a `◎ Plan` sub-row appears under each column
 * (the Report screen, §5.3).
 */
@Composable
fun NetBand(
    income: Long,
    expense: Long,
    net: Long,
    currency: String,
    modifier: Modifier = Modifier,
    plannedIncome: Long? = null,
    plannedExpense: Long? = null,
) {
    val plannedNet = if (plannedIncome != null && plannedExpense != null) plannedIncome - plannedExpense else null
    Box(
        modifier
            .fillMaxWidth()
            .shadow(8.dp, NetShape, clip = false)
            .clip(NetShape)
            .background(BudgetGradients.NetBand)
            .border(1.dp, Color.White.copy(alpha = 0.06f), NetShape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row {
            NetCell("Income", income, NetIncome, currency, plannedIncome, Modifier.weight(1f))
            NetCell("Expense", expense, NetInk, currency, plannedExpense, Modifier.weight(1f))
            NetCell("Net", net, if (net >= 0) NetIncome else NetOverage, currency, plannedNet, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NetCell(label: String, value: Long, valueColor: Color, currency: String, planned: Long?, modifier: Modifier) {
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = NetLabel)
        Spacer(Modifier.height(3.dp))
        // Calm number roll: the value slides up + fades when it changes (design "calm data").
        AnimatedContent(
            targetState = Money.formatShort(value, currency),
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
            },
            label = "netValue",
        ) { text ->
            Text(text, style = MaterialTheme.typography.titleLarge, color = valueColor, maxLines = 1)
        }
        if (planned != null) {
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.TrackChanges, contentDescription = null, modifier = Modifier.size(12.dp), tint = NetLabel)
                Spacer(Modifier.width(4.dp))
                Text("Plan ${Money.formatShort(planned, currency)}", style = MaterialTheme.typography.labelMedium, color = NetLabel, maxLines = 1)
            }
        }
    }
}
