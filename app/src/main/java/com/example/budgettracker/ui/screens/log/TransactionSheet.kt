package com.example.budgettracker.ui.screens.log

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.ui.components.CategoryIconChip
import com.example.budgettracker.ui.components.GradientButton
import com.example.budgettracker.ui.screens.categories.parseHexColor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSheet(
    existing: TxnRow?,
    categories: List<Category>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (date: Long, categoryId: Long, amount: Long, note: String?) -> Unit,
    onDelete: () -> Unit,
) {
    var amountText by remember { mutableStateOf(existing?.let { minorToInput(it.amount) } ?: "") }
    var categoryId by remember { mutableStateOf(existing?.categoryId ?: categories.firstOrNull()?.id) }
    var dateMillis by remember { mutableStateOf(existing?.date ?: System.currentTimeMillis()) }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    val parsedAmount = Money.parseToMinor(amountText)

    // Default the category once the live list loads (the sheet may open before it arrives).
    LaunchedEffect(categories) {
        if (categoryId == null) categoryId = categories.firstOrNull()?.id
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding() // keep the focused field (e.g. Note) above the keyboard
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (existing == null) "New transaction" else "Edit transaction", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") },
                prefix = { Text(Money.symbolOf(currency)) },
                trailingIcon = {
                    IconButton(onClick = { showCalculator = true }) {
                        Icon(Icons.Filled.Calculate, contentDescription = "Calculator")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountText.isNotBlank() && parsedAmount == null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            CategoryDropdown(categories, categoryId) { categoryId = it }
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(formatDateLabel(dateMillis))
            }
            OutlinedTextField(
                note, { note = it }, label = { Text("Note (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (existing != null) TextButton(onClick = onDelete) { Text("Delete") }
                Spacer(Modifier.weight(1f))
                GradientButton(
                    "Save",
                    onClick = { categoryId?.let { id -> parsedAmount?.let { amt -> onSave(dateMillis, id, amt, note.ifBlank { null }) } } },
                    enabled = parsedAmount != null && categoryId != null,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { dateMillis = utcMillisToLocalNoon(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = dateState) }
    }

    if (showCalculator) {
        CalculatorDialog(
            initial = amountText,
            currency = currency,
            onDismiss = { showCalculator = false },
            onResult = { amountText = it },
        )
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
                DropdownMenuItem(
                    text = { Text(category.name) },
                    leadingIcon = {
                        CategoryIconChip(
                            category.icon,
                            category.color?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 28.dp,
                        )
                    },
                    onClick = { onSelect(category.id); expanded = false },
                )
            }
        }
    }
}

private val DATE_LABEL = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH)

private fun minorToInput(minor: Long): String =
    if (minor % 100 == 0L) (minor / 100).toString() else "${minor / 100}.${(minor % 100).toString().padStart(2, '0')}"

private fun formatDateLabel(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_LABEL)

/** The DatePicker reports UTC midnight of the picked day; store it as noon local of that date. */
private fun utcMillisToLocalNoon(utcMillis: Long): Long {
    val date = Instant.ofEpochMilli(utcMillis).atZone(ZoneId.of("UTC")).toLocalDate()
    return date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
