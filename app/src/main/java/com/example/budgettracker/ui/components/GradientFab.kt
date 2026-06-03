package com.example.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.budgettracker.ui.theme.BudgetGradients

private val FabShape = RoundedCornerShape(18.dp)

/** Extended FAB on the navy gradient with a light label (design §3.6 / FAB). */
@Composable
fun GradientFab(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector = Icons.Filled.Add) {
    Surface(onClick = onClick, modifier = modifier, shape = FabShape, color = Color.Transparent, shadowElevation = 8.dp) {
        Row(
            Modifier.background(BudgetGradients.Fab, FabShape).padding(start = 20.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}
