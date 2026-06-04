package com.example.budgettracker.ui.screens.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.example.budgettracker.ui.components.GradientButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.RecurringTemplate
import com.example.budgettracker.domain.money.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringSheet(
    existing: RecurringTemplate?,
    categories: List<Category>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (label: String, categoryId: Long, amount: Long, dayOfMonth: Int, active: Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var categoryId by remember { mutableStateOf(existing?.categoryId ?: categories.firstOrNull()?.id) }
    var amountText by remember { mutableStateOf(existing?.let { Money.toMajorInput(it.amount) } ?: "") }
    var dayText by remember { mutableStateOf((existing?.dayOfMonth ?: 1).toString()) }
    var active by remember { mutableStateOf(existing?.active ?: true) }
    LaunchedEffect(categories) { if (categoryId == null) categoryId = categories.firstOrNull()?.id }

    val amount = Money.parseTargetToMinor(amountText)
    val day = dayText.toIntOrNull()
    val dayValid = day != null && day in 1..28
    val valid = label.isNotBlank() && categoryId != null && amount != null && dayValid

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (existing == null) "New recurring entry" else "Edit recurring entry", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(label, { label = it }, label = { Text("Label") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            CategoryDropdown(categories, categoryId) { categoryId = it }
            OutlinedTextField(
                amountText, { amountText = it },
                label = { Text("Amount") },
                prefix = { Text(Money.symbolOf(currency)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountText.isNotBlank() && amount == null,
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                dayText, { dayText = it },
                label = { Text("Day of month (1–28)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = dayText.isNotBlank() && !dayValid,
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Active", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = active, onCheckedChange = { active = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (existing != null) TextButton(onClick = onDelete) { Text("Delete") }
                Spacer(Modifier.weight(1f))
                GradientButton(
                    "Save",
                    onClick = { if (valid) onSave(label, categoryId!!, amount!!, day!!, active) },
                    enabled = valid,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(categories: List<Category>, selectedId: Long?, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.id == selectedId }?.name ?: "Select category"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                DropdownMenuItem(text = { Text(category.name) }, onClick = { onSelect(category.id); expanded = false })
            }
        }
    }
}
