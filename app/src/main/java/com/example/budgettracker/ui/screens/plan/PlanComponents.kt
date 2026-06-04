package com.example.budgettracker.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.domain.report.inferGroupKind
import com.example.budgettracker.ui.components.BannerTone
import com.example.budgettracker.ui.components.BudgetCard
import com.example.budgettracker.ui.components.GradientBanner
import com.example.budgettracker.ui.components.GradientButton
import com.example.budgettracker.ui.screens.categories.ColorDot
import com.example.budgettracker.ui.screens.categories.KindChip
import com.example.budgettracker.ui.screens.categories.parseHexColor
import com.example.budgettracker.ui.theme.BudgetGradients

@Composable
fun PlanBanner(banner: PrefillBanner, modifier: Modifier = Modifier) {
    when (banner) {
        PrefillBanner.CARRIED_FORWARD ->
            GradientBanner("Pre-filled from last month's targets — tweak and save.", BannerTone.INFO, modifier)
        PrefillBanner.FIRST_TIME ->
            GradientBanner("No targets yet — set your first plan for this month.", BannerTone.AMBER, modifier)
        PrefillBanner.NONE -> Unit
    }
}

/** Section label above the Income / Expense group lists (design "label-m"). */
@Composable
fun PlanSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier.padding(start = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun PlanGroupCard(
    group: PlanGroup,
    inputs: Map<Long, String>,
    currency: String,
    onInputChange: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtotal = group.categories.sumOf { Money.parseTargetToMinor(inputs[it.id].orEmpty()) ?: 0L }
    BudgetCard(modifier) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorDot(parseHexColor(group.group.color))
                Spacer(Modifier.width(12.dp))
                Text(
                    group.group.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                KindChip(inferGroupKind(group.categories))
                Spacer(Modifier.width(10.dp))
                // Group target subtotal, marked with the ◎ target glyph (design "grp-sub").
                Icon(
                    Icons.Outlined.TrackChanges,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    Money.formatShort(subtotal, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            group.categories.forEach { category ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(category.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = inputs[category.id].orEmpty(),
                        onValueChange = { onInputChange(category.id, it) },
                        prefix = { Text(Money.symbolOf(currency)) },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.width(150.dp),
                    )
                }
            }
        }
    }
}

private val StickyShape = RoundedCornerShape(16.dp)
private val StickyLabel = Color(0xFF9FB1BD) // fixed light: the bar is navy in both themes

/** Floating Save bar — a rounded navy card that lifts off the content scrolling beneath it. */
@Composable
fun PlanSaveBar(targetCount: Int, onSave: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .shadow(16.dp, StickyShape, clip = false)
            .clip(StickyShape)
            .background(BudgetGradients.StickyBar)
            .border(1.dp, Color.White.copy(alpha = 0.07f), StickyShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$targetCount ${if (targetCount == 1) "target" else "targets"}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = StickyLabel,
        )
        GradientButton("Save targets", onClick = onSave)
    }
}
