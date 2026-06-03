package com.example.budgettracker.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.ui.AppViewModelProvider
import com.example.budgettracker.ui.components.EmptyState
import com.example.budgettracker.ui.components.GradientFab

private sealed interface CategoriesSheet {
    data object Chooser : CategoriesSheet
    data class GroupForm(val group: CategoryGroup?) : CategoriesSheet
    data class CategoryForm(val category: Category?, val defaultGroupId: Long?) : CategoriesSheet
}

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val sections by viewModel.sections.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val liveGroups by viewModel.liveGroups.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var sheet by remember { mutableStateOf<CategoriesSheet?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            GradientFab("New", onClick = { sheet = CategoriesSheet.Chooser })
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Spacer(Modifier.height(8.dp))
            CategoryFilterChips(filter, viewModel::setFilter)
            Spacer(Modifier.height(8.dp))
            if (sections.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Category,
                    title = "Nothing here",
                    subtitle = "Add a category or group with the New button.",
                )
            } else if (filter == CategoryFilter.ALL) {
                // Drag-to-reorder is only meaningful on the unfiltered list.
                ReorderableSections(
                    sections = sections,
                    onReorderGroups = viewModel::reorderGroups,
                    onReorderCategories = { _, ids -> viewModel.reorderCategories(ids) },
                    onGroupClick = { sheet = CategoriesSheet.GroupForm(it) },
                    onCategoryClick = { sheet = CategoriesSheet.CategoryForm(it, it.groupId) },
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(sections, key = { it.group.id }) { section ->
                        GroupCard(
                            section = section,
                            onGroupClick = { sheet = CategoriesSheet.GroupForm(section.group) },
                            onCategoryClick = { category -> sheet = CategoriesSheet.CategoryForm(category, category.groupId) },
                        )
                    }
                }
            }
        }
    }

    when (val current = sheet) {
        CategoriesSheet.Chooser -> NewChooserSheet(
            onDismiss = { sheet = null },
            onNewCategory = { sheet = CategoriesSheet.CategoryForm(null, liveGroups.firstOrNull()?.id) },
            onNewGroup = { sheet = CategoriesSheet.GroupForm(null) },
        )

        is CategoriesSheet.GroupForm -> GroupFormSheet(
            existing = current.group,
            onDismiss = { sheet = null },
            onSave = { name, color ->
                if (current.group == null) viewModel.createGroup(name, color)
                else viewModel.updateGroup(current.group.copy(name = name, color = color))
                sheet = null
            },
            onArchive = {
                current.group?.let { viewModel.archiveGroup(it.id) }
                sheet = null
            },
        )

        is CategoriesSheet.CategoryForm -> CategoryFormSheet(
            existing = current.category,
            groups = liveGroups,
            defaultGroupId = current.defaultGroupId,
            onDismiss = { sheet = null },
            onSave = { groupId, name, kind, color ->
                if (current.category == null) {
                    viewModel.createCategory(groupId, name, kind, color)
                } else {
                    viewModel.updateCategory(current.category.copy(groupId = groupId, name = name, kind = kind, color = color))
                }
                sheet = null
            },
            onArchive = {
                current.category?.let { viewModel.archiveCategory(it.id) }
                sheet = null
            },
        )

        null -> Unit
    }
}
