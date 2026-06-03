package com.example.budgettracker.ui.screens.log

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.ui.screens.categories.parseHexColor
import com.example.budgettracker.ui.theme.BudgetTheme
import com.example.budgettracker.ui.theme.money

/** All / Income / Expense filter chips with month counts (design §5.1). */
@Composable
fun LogFilterChips(
    selected: TxnFilter,
    incomeCount: Int,
    expenseCount: Int,
    onSelect: (TxnFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TxnFilter.entries.forEach { filter ->
            val label = when (filter) {
                TxnFilter.ALL -> "All"
                TxnFilter.INCOME -> "Income ($incomeCount)"
                TxnFilter.EXPENSE -> "Expense ($expenseCount)"
            }
            FilterChip(selected = filter == selected, onClick = { onSelect(filter) }, label = { Text(label) })
        }
    }
}

@Composable
fun DateCard(section: DateSection, currency: String, onRowClick: (TxnRow) -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(section.dateLabel, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Net ${Money.format(section.dayNet, currency)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (section.dayNet >= 0) BudgetTheme.semanticColors.income else BudgetTheme.semanticColors.overage,
                )
            }
            section.rows.forEach { row -> TxnRowItem(row, currency) { onRowClick(row) } }
        }
    }
}

@Composable
private fun TxnRowItem(row: TxnRow, currency: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(3.dp).height(36.dp).background(parseHexColor(row.leadingColor), RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(row.categoryName, style = MaterialTheme.typography.titleMedium)
            row.note?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        val amountText =
            if (row.kind == Kind.INCOME) "+${Money.format(row.amount, currency)}" else Money.format(row.amount, currency)
        Text(
            amountText,
            style = MaterialTheme.typography.money,
            color = if (row.kind == Kind.INCOME) BudgetTheme.semanticColors.income else MaterialTheme.colorScheme.onSurface,
        )
    }
}
