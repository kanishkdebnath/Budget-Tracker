package com.example.budgettracker.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.ui.theme.BudgetTheme

/** Seed group colors as hex (design §3.1 / PRODUCT_SPEC §6.7) — used by the color pickers. */
val CategoryPaletteHex = listOf("#10b981", "#ef4444", "#f59e0b", "#dc2626", "#8b5cf6", "#0ea5e9", "#64748b")

/** Parse "#RRGGBB" without an Android dependency (works in previews & host JVM). */
fun parseHexColor(hex: String): Color = try {
    val c = hex.removePrefix("#")
    Color(c.substring(0, 2).toInt(16), c.substring(2, 4).toInt(16), c.substring(4, 6).toInt(16))
} catch (_: Exception) {
    Color(0xFF64748B)
}

@Composable
fun ColorDot(color: Color, modifier: Modifier = Modifier, size: Dp = 12.dp) {
    Spacer(modifier.size(size).background(color, RoundedCornerShape(3.dp)))
}

/** Inc / Exp kind chip (design §3.1). */
@Composable
fun KindChip(kind: Kind) {
    val color = if (kind == Kind.INCOME) BudgetTheme.semanticColors.income else BudgetTheme.semanticColors.overage
    val alpha = if (kind == Kind.INCOME) 0.14f else 0.12f
    Text(
        text = if (kind == Kind.INCOME) "Inc" else "Exp",
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = alpha), CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
fun CategoryFilterChips(selected: CategoryFilter, onSelect: (CategoryFilter) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryFilter.entries.forEach { filter ->
            FilterChip(selected = filter == selected, onClick = { onSelect(filter) }, label = { Text(filter.label) })
        }
    }
}

@Composable
fun GroupCard(
    section: GroupSection,
    onGroupClick: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupColor = parseHexColor(section.group.color)
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onGroupClick).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorDot(groupColor)
                Spacer(Modifier.width(12.dp))
                Text(section.group.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (section.group.archived) {
                    Text("Archived", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            section.categories.forEach { category ->
                CategoryRow(category, groupColor) { onCategoryClick(category) }
            }
            if (section.categories.isEmpty()) {
                Text(
                    "No categories yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 40.dp, bottom = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(category: Category, groupColor: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ColorDot(category.color?.let { parseHexColor(it) } ?: groupColor, size = 10.dp)
        Spacer(Modifier.width(12.dp))
        Text(category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        KindChip(category.kind)
    }
}
