package com.example.budgettracker.ui.screens.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.domain.report.CategoryReportRow
import com.example.budgettracker.domain.report.GroupReport
import com.example.budgettracker.ui.components.BannerTone
import com.example.budgettracker.ui.components.BudgetCard
import com.example.budgettracker.ui.components.GradientBanner
import com.example.budgettracker.ui.components.CategoryIconChip
import com.example.budgettracker.ui.screens.categories.ColorDot
import com.example.budgettracker.ui.screens.categories.KindChip
import com.example.budgettracker.ui.screens.categories.parseHexColor
import com.example.budgettracker.ui.screens.log.TxnRow
import com.example.budgettracker.ui.theme.BudgetTheme
import com.example.budgettracker.ui.theme.money
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The "over by … (n%)" / "under by … (n%)" clause in the generated narrative (§7.3), for emphasis. */
private val NARRATIVE_EMPHASIS = Regex("""\b(over|under) by [^()]+\(\d+%\)""")

// Fixed numeric-column widths so Plan / Actual / Δ line up vertically across every row. Plan and
// Actual are left-aligned (◎ glyphs and number left edges align row-to-row); the Δ column hugs the
// right edge (pills align by their right edge). Fixed widths stop a variable-width pill shifting the
// block.
private val PLAN_COL = 84.dp
private val ACTUAL_COL = 72.dp
private val DELTA_COL = 88.dp
private val EXPANDED_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

@Composable
fun NarrativeBox(narrative: String, monthLabel: String, modifier: Modifier = Modifier) {
    val income = BudgetTheme.semanticColors.income
    val overage = BudgetTheme.semanticColors.overage
    val text = remember(narrative, income, overage) {
        buildAnnotatedString {
            append(narrative)
            NARRATIVE_EMPHASIS.find(narrative)?.let { match ->
                val color = if (match.value.startsWith("over")) overage else income
                addStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
            }
        }
    }
    BudgetCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Summary · $monthLabel".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun RecurringDueBanner(count: Int, modifier: Modifier = Modifier) {
    GradientBanner(
        "$count recurring ${if (count == 1) "entry is" else "entries are"} due this month — apply on the Recurring tab.",
        BannerTone.AMBER,
        modifier,
        leadingIcon = Icons.Outlined.Schedule,
    )
}

@Composable
fun ReportGroupCard(
    report: GroupReport,
    currency: String,
    transactionsByCategoryId: Map<Long, List<TxnRow>>,
    expandedCategoryId: Long?,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BudgetCard(modifier) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    ColorDot(parseHexColor(report.group.color))
                    Spacer(Modifier.width(12.dp))
                    Text(report.group.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(8.dp))
                    KindChip(report.kind)
                }
                Spacer(Modifier.width(8.dp))
                // Group total Plan→Actual delta pill (design "grp-head" right element), aligned to the
                // same Δ column as the rows below.
                Box(Modifier.width(DELTA_COL), contentAlignment = Alignment.CenterEnd) {
                    DeltaPill(report.actualSubtotal - report.targetSubtotal, report.kind, currency)
                }
            }
            report.rows.forEach { row ->
                ReportRow(
                    row = row,
                    currency = currency,
                    groupColor = report.group.color,
                    transactions = transactionsByCategoryId[row.category.id].orEmpty(),
                    isExpanded = expandedCategoryId == row.category.id,
                    onToggle = { onToggle(row.category.id) },
                )
            }
        }
    }
}

@Composable
private fun ReportRow(
    row: CategoryReportRow,
    currency: String,
    groupColor: String,
    transactions: List<TxnRow>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    val density = BudgetTheme.density
    val expandable = transactions.isNotEmpty()
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (expandable) Modifier.clickable(onClick = onToggle) else Modifier)
                .heightIn(min = density.rowMinHeight)
                .padding(horizontal = 16.dp, vertical = density.rowPaddingVertical),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIconChip(
                row.category.icon,
                row.category.color?.let { parseHexColor(it) } ?: parseHexColor(groupColor),
                size = 26.dp,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                row.category.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(6.dp))
            // Plan: muted, prefixed with the ◎ target glyph (design "ta-row .num.target").
            Row(Modifier.width(PLAN_COL), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.TrackChanges,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    Money.format(row.target, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                Money.format(row.actual, currency),
                modifier = Modifier.width(ACTUAL_COL),
                style = MaterialTheme.typography.money,
                textAlign = TextAlign.Start,
            )
            Box(Modifier.width(DELTA_COL), contentAlignment = Alignment.CenterEnd) {
                if (expandable) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse transactions" else "Expand transactions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    DeltaPill(row.delta, row.category.kind, currency)
                }
            }
        }
        if (isExpanded && expandable) {
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            transactions.forEach { txn -> TxnExpandedRow(txn, currency) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TxnExpandedRow(txn: TxnRow, currency: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            Instant.ofEpochMilli(txn.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(EXPANDED_DATE_FORMAT),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp),
        )
        Text(
            txn.note?.takeIf { it.isNotBlank() } ?: txn.categoryName,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            Money.formatShort(txn.amount, currency),
            style = MaterialTheme.typography.money,
            color = if (txn.kind == Kind.INCOME)
                BudgetTheme.semanticColors.income
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Signed, colored delta pill. Favorable = green, unfavorable = red, no change = flat muted. */
@Composable
private fun DeltaPill(delta: Long, kind: Kind, currency: String, modifier: Modifier = Modifier) {
    val favorable = if (kind == Kind.EXPENSE) delta <= 0 else delta >= 0
    val container: Color
    val content: Color
    when {
        delta == 0L -> {
            container = MaterialTheme.colorScheme.surfaceVariant
            content = MaterialTheme.colorScheme.onSurfaceVariant
        }
        favorable -> {
            container = BudgetTheme.semanticColors.income.copy(alpha = 0.14f)
            content = BudgetTheme.semanticColors.income
        }
        else -> {
            container = BudgetTheme.semanticColors.overage.copy(alpha = 0.14f)
            content = BudgetTheme.semanticColors.overage
        }
    }
    val label = when {
        delta == 0L -> "0"
        delta > 0 -> "+${Money.format(delta, currency)}"
        else -> Money.format(delta, currency) // already carries the minus sign
    }
    Surface(modifier, shape = RoundedCornerShape(50), color = container) {
        Text(
            label,
            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            color = content,
        )
    }
}
