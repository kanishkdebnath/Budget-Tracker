package com.example.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.budgettracker.ui.theme.BudgetGradients
import com.example.budgettracker.ui.theme.PillShape

enum class GradientButtonTone { FILLED, TONAL }

// Fixed inks: the gradients are theme-independent (light-blue / navy), so the text/icon color is too,
// staying legible in both light and dark (design §3.6 buttons — filled = on-primary, tonal = on-primary-container).
private val FilledInk = Color(0xFF003547)
private val TonalInk = Color(0xFFC5E5F9)

/**
 * Pill button with the brand gradient (design `.btn`). FILLED is the light-blue primary CTA; TONAL is
 * the navy secondary. Disabled falls back to a flat muted surface (`.btn.disabled`). Pass
 * `Modifier.fillMaxWidth()` for the full-width sticky-bar variant.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: GradientButtonTone = GradientButtonTone.FILLED,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val brush = if (tone == GradientButtonTone.FILLED) BudgetGradients.FilledButton else BudgetGradients.TonalButton
    val contentColor = if (enabled) {
        if (tone == GradientButtonTone.FILLED) FilledInk else TonalInk
    } else {
        scheme.onSurfaceVariant
    }
    val background = if (enabled) Modifier.background(brush) else Modifier.background(scheme.surfaceVariant)
    Row(
        modifier
            .clip(PillShape)
            .then(background)
            .clickable(enabled = enabled, onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp), tint = contentColor) }
        Text(text, style = MaterialTheme.typography.labelLarge, color = contentColor)
    }
}
