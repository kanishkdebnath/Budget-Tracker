package com.example.budgettracker.ui.screens.log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.ui.components.BudgetCard
import com.example.budgettracker.ui.components.CategoryIconChip
import com.example.budgettracker.ui.components.chipPop
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
                TxnFilter.ALL -> "All · ${incomeCount + expenseCount}"
                TxnFilter.INCOME -> "Income · $incomeCount"
                TxnFilter.EXPENSE -> "Expense · $expenseCount"
            }
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(label) },
                modifier = Modifier.chipPop(filter == selected),
            )
        }
    }
}

@Composable
fun DateCard(section: DateSection, currency: String, onRowClick: (TxnRow) -> Unit, modifier: Modifier = Modifier) {
    BudgetCard(modifier) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(section.dayLabel, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    WeekdayPill(section.weekday)
                }
                Text(
                    Money.formatShort(section.dayNet, currency),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (section.dayNet >= 0) BudgetTheme.semanticColors.income else BudgetTheme.semanticColors.overage,
                )
            }
            section.rows.forEach { row -> TxnRowItem(row, currency) { onRowClick(row) } }
        }
    }
}

@Composable
private fun WeekdayPill(weekday: String) {
    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            weekday.uppercase(),
            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TxnRowItem(row: TxnRow, currency: String, onClick: () -> Unit) {
    // Description leads as the title when present; otherwise the category name does (design §5.1).
    val title = row.note?.takeIf { it.isNotBlank() } ?: row.categoryName
    val subtitle = if (row.note?.isNotBlank() == true) {
        if (row.groupName.isNotBlank()) "${row.groupName} · ${row.categoryName}" else row.categoryName
    } else {
        row.groupName.takeIf { it.isNotBlank() }
    }
    val density = BudgetTheme.density
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .heightIn(min = density.rowMinHeight)
            .padding(horizontal = 16.dp, vertical = density.rowPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIconChip(row.iconKey, parseHexColor(row.leadingColor), size = 32.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitle?.let {
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

/** Single chip showing the active category filter. Tap to open picker; tap × to clear. */
@Composable
fun CategoryChipRow(
    selectedCategoryId: Long?,
    categories: List<Category>,
    onChipClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedName = categories.firstOrNull { it.id == selectedCategoryId }?.name
    Row(modifier.padding(horizontal = 16.dp)) {
        FilterChip(
            selected = selectedCategoryId != null,
            onClick = onChipClick,
            label = { Text(selectedName ?: "Category") },
            trailingIcon = if (selectedCategoryId != null) {
                {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear category filter",
                        modifier = Modifier
                            .size(16.dp)
                            // detectTapGestures consumes the pointer event so the chip's
                            // onClick doesn't also fire when the × is tapped.
                            .pointerInput(onClear) { detectTapGestures { onClear() } },
                    )
                }
            } else null,
            modifier = Modifier.chipPop(selectedCategoryId != null),
        )
    }
}

/** Bottom sheet listing all live categories grouped under their group headers. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterSheet(
    categories: List<Category>,
    groups: List<CategoryGroup>,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val groupsById = remember(groups) { groups.associateBy { it.id } }
    val grouped = remember(categories, groups) {
        categories
            .groupBy { it.groupId }
            .entries
            .sortedBy { (gid, _) -> groupsById[gid]?.order ?: Int.MAX_VALUE }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Filter by Category",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
            grouped.forEach { (groupId, cats) ->
                val groupName = groupsById[groupId]?.name ?: return@forEach
                item(key = "group-$groupId") {
                    Text(
                        groupName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(cats, key = { it.id }) { cat ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(cat.id); onDismiss() }
                            .padding(horizontal = 16.dp)
                            .heightIn(min = BudgetTheme.density.rowMinHeight),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CategoryIconChip(
                            cat.icon,
                            cat.color?.let { parseHexColor(it) }
                                ?: parseHexColor(groupsById[cat.groupId]?.color ?: "#64748b"),
                            size = 26.dp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(cat.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
