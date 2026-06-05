package com.example.budgettracker.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import com.example.budgettracker.ui.theme.BudgetGradients
import com.example.budgettracker.ui.theme.BudgetTheme

private val CardShape = RoundedCornerShape(16.dp)

/** 16dp card: subtle navy surface gradient + faint border in dark; white surface + faint navy border in light. */
@Composable
fun BudgetCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val scheme = MaterialTheme.colorScheme
    if (!BudgetTheme.isLight) {
        Column(
            modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(BudgetGradients.CardSurface)
                .border(1.dp, Color.White.copy(alpha = 0.05f), CardShape),
            content = content,
        )
    } else {
        Surface(
            modifier.fillMaxWidth(),
            shape = CardShape,
            color = scheme.surface,
            border = BorderStroke(1.dp, Color(0x140D2736)),
        ) {
            Column(content = content)
        }
    }
}
