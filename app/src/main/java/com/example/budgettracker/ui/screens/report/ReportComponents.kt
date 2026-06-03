package com.example.budgettracker.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.domain.report.CategoryReportRow
import com.example.budgettracker.domain.report.GroupReport
import com.example.budgettracker.ui.screens.categories.ColorDot
import com.example.budgettracker.ui.screens.categories.KindChip
import com.example.budgettracker.ui.screens.categories.parseHexColor
import com.example.budgettracker.ui.theme.BudgetTheme
import com.example.budgettracker.ui.theme.money

@Composable
fun NarrativeBox(narrative: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Column(Modifier.padding(16.dp)) {
            Text("SUMMARY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(narrative, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun RecurringDueBanner(count: Int, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
        Text(
            "$count recurring ${if (count == 1) "entry" else "entries"} due this month — apply on the Recurring tab.",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
fun ReportGroupCard(report: GroupReport, currency: String, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorDot(parseHexColor(report.group.color))
                    Spacer(Modifier.width(12.dp))
                    Text(report.group.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    KindChip(report.kind)
                }
                Text(Money.format(report.actualSubtotal, currency), style = MaterialTheme.typography.money)
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(1f))
                ColumnLabel("Plan")
                ColumnLabel("Actual")
                ColumnLabel("Δ")
            }
            report.rows.forEach { row -> ReportRow(row, currency) }
        }
    }
}

@Composable
private fun ColumnLabel(text: String) {
    Text(
        text,
        modifier = Modifier.width(if (text == "Δ") 72.dp else 80.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End,
    )
}

@Composable
private fun ReportRow(row: CategoryReportRow, currency: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(row.category.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(
            Money.format(row.target, currency),
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
        Text(
            Money.format(row.actual, currency),
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.End,
        )
        DeltaText(row.delta, row.category.kind, currency)
    }
}

@Composable
private fun DeltaText(delta: Long, kind: Kind, currency: String) {
    val favorable = if (kind == Kind.EXPENSE) delta <= 0 else delta >= 0
    val color = when {
        delta == 0L -> MaterialTheme.colorScheme.onSurfaceVariant
        favorable -> BudgetTheme.semanticColors.income
        else -> BudgetTheme.semanticColors.overage
    }
    val text = if (delta > 0) "+${Money.format(delta, currency)}" else Money.format(delta, currency)
    Text(text, modifier = Modifier.width(72.dp), style = MaterialTheme.typography.labelLarge, color = color, textAlign = TextAlign.End)
}
