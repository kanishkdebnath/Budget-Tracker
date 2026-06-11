package com.example.budgettracker.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.budgettracker.ui.icons.CATEGORY_ICON_SECTIONS
import com.example.budgettracker.ui.icons.searchIcons

/**
 * Dedicated icon picker. Shows a search box, a "None" option (clears the icon → dot fallback), and
 * the sectioned grid. While browsing, icons render neutral; selecting one calls [onSelect] and the
 * caller closes the sheet. [tint] previews the currently selected category color on the chosen chip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerSheet(
    selectedKey: String?,
    tint: Color,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text("Choose icon", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                placeholder = { Text("Search icons") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
            )
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp).clip(CircleShape)
                    .clickable { onSelect(null) }
                    .background(
                        if (selectedKey == null) tint.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape,
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("None", style = MaterialTheme.typography.labelLarge)
                Text("  ·  use color dot", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val filtering = query.isNotBlank()
            val sections = if (filtering) listOf("Results" to searchIcons(query)) else CATEGORY_ICON_SECTIONS.map { it.title to it.icons }
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sections.forEach { (title, icons) ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                        )
                    }
                    items(icons, key = { it.key }) { icon ->
                        val selected = icon.key == selectedKey
                        Box(
                            Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (selected) tint.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { onSelect(icon.key) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                icon.vector,
                                contentDescription = icon.label,
                                tint = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.padding(bottom = 16.dp)) {}
                }
            }
        }
    }
}
