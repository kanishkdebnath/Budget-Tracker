package com.example.budgettracker.ui.screens.plan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.ui.screens.categories.ColorDot
import com.example.budgettracker.ui.screens.categories.parseHexColor

@Composable
fun PlanBanner(banner: PrefillBanner, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (container, content, text) = when (banner) {
        PrefillBanner.CARRIED_FORWARD ->
            Triple(scheme.primaryContainer, scheme.onPrimaryContainer, "Pre-filled from last month's targets — tweak and save.")
        PrefillBanner.FIRST_TIME ->
            Triple(scheme.tertiaryContainer, scheme.onTertiaryContainer, "No targets yet — set your first plan for this month.")
        PrefillBanner.NONE -> return
    }
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = container) {
        Text(text, Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium, color = content)
    }
}

@Composable
fun PlanGroupCard(
    group: PlanGroup,
    inputs: Map<Long, String>,
    currency: String,
    onInputChange: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorDot(parseHexColor(group.group.color))
                Spacer(Modifier.width(12.dp))
                Text(group.group.name, style = MaterialTheme.typography.titleMedium)
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

@Composable
fun PlanSaveBar(onSave: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Save targets")
        }
    }
}
