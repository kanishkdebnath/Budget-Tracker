package com.example.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.example.budgettracker.ui.theme.BudgetGradients

private val CardShape = RoundedCornerShape(16.dp)

/** 16dp card with the subtle surface gradient + faint border in dark themes (design §3.6 card surface). */
@Composable
fun BudgetCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val scheme = MaterialTheme.colorScheme
    if (scheme.surface.luminance() < 0.5f) {
        Column(
            modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(BudgetGradients.CardSurface)
                .border(1.dp, Color.White.copy(alpha = 0.05f), CardShape),
            content = content,
        )
    } else {
        Surface(modifier.fillMaxWidth(), shape = CardShape, color = scheme.surface, tonalElevation = 1.dp) {
            Column(content = content)
        }
    }
}
