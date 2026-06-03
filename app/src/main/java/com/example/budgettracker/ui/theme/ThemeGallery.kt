package com.example.budgettracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
private fun Swatch(name: String, color: Color, onColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(color, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(name, color = onColor, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ThemeGalleryContent() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val cs = MaterialTheme.colorScheme
            Text("Type scale", style = MaterialTheme.typography.headlineMedium, color = cs.onBackground)
            Text("Net −₹6,800", style = MaterialTheme.typography.money, color = BudgetTheme.semanticColors.income)
            Swatch("primary", cs.primary, cs.onPrimary)
            Swatch("primaryContainer", cs.primaryContainer, cs.onPrimaryContainer)
            Swatch("tertiary", cs.tertiary, cs.onTertiary)
            Swatch("surfaceVariant", cs.surfaceVariant, cs.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(SeedIncome, SeedBills, SeedHousehold, SeedDebt, SeedLeisure, SeedSavings, SeedOther)
                    .forEach { Surface(Modifier.size(20.dp), shape = RoundedCornerShape(4.dp), color = it) {} }
            }
        }
    }
}

@Preview(name = "Theme — Light", showBackground = true)
@Composable
private fun ThemeGalleryLightPreview() {
    BudgetTrackerTheme(darkTheme = false) { ThemeGalleryContent() }
}

@Preview(name = "Theme — Dark", showBackground = true)
@Composable
private fun ThemeGalleryDarkPreview() {
    BudgetTrackerTheme(darkTheme = true) { ThemeGalleryContent() }
}
