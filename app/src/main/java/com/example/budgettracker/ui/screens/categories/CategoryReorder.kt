package com.example.budgettracker.ui.screens.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.budgettracker.ui.components.BudgetCard
import com.example.budgettracker.ui.theme.BudgetTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Drag-to-reorder list (§F2.6), shown only on the "All" filter. Groups reorder in the outer
 * lazy list; categories reorder within their group card. Local state animates the drag; the new
 * order is persisted on drop and re-synced from the repository Flow.
 */
@Composable
fun ReorderableSections(
    sections: List<GroupSection>,
    onReorderGroups: (List<Long>) -> Unit,
    onReorderCategories: (groupId: Long, orderedIds: List<Long>) -> Unit,
    onGroupClick: (CategoryGroup) -> Unit,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    var groups by remember { mutableStateOf(sections) }
    LaunchedEffect(sections) { groups = sections }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        groups = groups.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(groups, key = { it.group.id }) { section ->
            ReorderableItem(reorderState, key = section.group.id) { _ ->
                ReorderableGroupCard(
                    section = section,
                    dragHandle = {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.draggableHandle(
                                onDragStopped = { onReorderGroups(groups.map { it.group.id }) },
                            ),
                        ) { Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder group") }
                    },
                    onGroupClick = { onGroupClick(section.group) },
                    onCategoryClick = onCategoryClick,
                    onReorderCategories = { ids -> onReorderCategories(section.group.id, ids) },
                )
            }
        }
    }
}

@Composable
private fun ReorderableGroupCard(
    section: GroupSection,
    dragHandle: @Composable () -> Unit,
    onGroupClick: () -> Unit,
    onCategoryClick: (Category) -> Unit,
    onReorderCategories: (List<Long>) -> Unit,
) {
    val groupColor = parseHexColor(section.group.color)
    var cats by remember(section.group.id) { mutableStateOf(section.categories) }
    LaunchedEffect(section.categories) { cats = section.categories }

    BudgetCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp)) {
            GroupHeaderRow(
                section,
                onClick = onGroupClick,
                trailing = {
                    dragHandle()
                    Spacer(Modifier.width(8.dp))
                },
            )

            if (cats.isEmpty()) {
                Text(
                    "No categories yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 40.dp, bottom = 12.dp),
                )
            } else {
                ReorderableColumn(
                    list = cats,
                    onSettle = { from, to ->
                        cats = cats.toMutableList().apply { add(to, removeAt(from)) }
                        onReorderCategories(cats.map { it.id })
                    },
                ) { _, category, _ ->
                    key(category.id) {
                        ReorderableItem {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Row(
                                    Modifier.weight(1f).clickable { onCategoryClick(category) }
                                        .heightIn(min = BudgetTheme.density.rowMinHeight)
                                        .padding(horizontal = 16.dp, vertical = BudgetTheme.density.rowPaddingVertical),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    ColorDot(category.color?.let { parseHexColor(it) } ?: groupColor, size = 10.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                }
                                IconButton(onClick = {}, modifier = Modifier.draggableHandle()) {
                                    Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder category")
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
