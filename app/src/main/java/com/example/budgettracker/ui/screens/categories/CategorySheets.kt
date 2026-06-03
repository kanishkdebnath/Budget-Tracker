package com.example.budgettracker.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChooserSheet(onDismiss: () -> Unit, onNewCategory: () -> Unit, onNewGroup: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Add new", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onNewCategory, modifier = Modifier.fillMaxWidth()) { Text("New category") }
            OutlinedButton(onClick = onNewGroup, modifier = Modifier.fillMaxWidth()) { Text("New group") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFormSheet(
    existing: CategoryGroup?,
    onDismiss: () -> Unit,
    onSave: (name: String, color: String) -> Unit,
    onArchive: () -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var color by remember { mutableStateOf(existing?.color ?: CategoryPaletteHex.first()) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (existing == null) "New group" else "Edit group", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Color", style = MaterialTheme.typography.labelLarge)
            ColorPickerRow(selectedHex = color, allowNone = false, onSelect = { color = it ?: color })
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (existing != null) TextButton(onClick = onArchive) { Text("Archive") }
                Spacer(Modifier.weight(1f))
                Button(onClick = { onSave(name, color) }, enabled = name.isNotBlank()) { Text("Save") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFormSheet(
    existing: Category?,
    groups: List<CategoryGroup>,
    defaultGroupId: Long?,
    onDismiss: () -> Unit,
    onSave: (groupId: Long, name: String, kind: Kind, color: String?) -> Unit,
    onArchive: () -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var groupId by remember { mutableStateOf(existing?.groupId ?: defaultGroupId ?: groups.firstOrNull()?.id) }
    var kind by remember { mutableStateOf(existing?.kind ?: Kind.EXPENSE) }
    var color by remember { mutableStateOf(existing?.color) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (existing == null) "New category" else "Edit category", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            GroupDropdown(groups, groupId, onSelect = { groupId = it })
            Text("Kind", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Kind.entries.forEach { option ->
                    FilterChip(
                        selected = option == kind,
                        onClick = { kind = option },
                        label = { Text(if (option == Kind.INCOME) "Income" else "Expense") },
                    )
                }
            }
            Text("Color (optional)", style = MaterialTheme.typography.labelLarge)
            ColorPickerRow(selectedHex = color, allowNone = true, onSelect = { color = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (existing != null) TextButton(onClick = onArchive) { Text("Archive") }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { groupId?.let { onSave(it, name, kind, color) } },
                    enabled = name.isNotBlank() && groupId != null,
                ) { Text("Save") }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDropdown(groups: List<CategoryGroup>, selectedId: Long?, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = groups.firstOrNull { it.id == selectedId }?.name ?: "Select group"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Group") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            groups.forEach { group ->
                DropdownMenuItem(text = { Text(group.name) }, onClick = { onSelect(group.id); expanded = false })
            }
        }
    }
}

@Composable
private fun ColorPickerRow(selectedHex: String?, allowNone: Boolean, onSelect: (String?) -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (allowNone) {
            Box(
                Modifier.size(32.dp).clip(CircleShape)
                    .border(
                        if (selectedHex == null) 2.dp else 1.dp,
                        if (selectedHex == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        CircleShape,
                    )
                    .clickable { onSelect(null) },
                contentAlignment = Alignment.Center,
            ) { Text("—", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        CategoryPaletteHex.forEach { hex ->
            val selected = hex.equals(selectedHex, ignoreCase = true)
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(parseHexColor(hex))
                    .border(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    .clickable { onSelect(hex) },
            )
        }
    }
}
