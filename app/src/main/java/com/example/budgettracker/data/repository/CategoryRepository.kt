package com.example.budgettracker.data.repository

import com.example.budgettracker.data.OpResult
import com.example.budgettracker.data.dao.CategoryDao
import com.example.budgettracker.data.dao.CategoryGroupDao
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import kotlinx.coroutines.flow.Flow

/**
 * Groups & categories (PRODUCT_SPEC F2). Enforces case-insensitive name uniqueness among live
 * items (plan decision #1) and the "no live categories" guard before archiving a group (§F2.5).
 */
class CategoryRepository(
    private val groupDao: CategoryGroupDao,
    private val categoryDao: CategoryDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

    fun observeGroups(includeArchived: Boolean = false): Flow<List<CategoryGroup>> =
        if (includeArchived) groupDao.observeAll() else groupDao.observeLive()

    fun observeCategories(includeArchived: Boolean = false): Flow<List<Category>> =
        if (includeArchived) categoryDao.observeAll() else categoryDao.observeLive()

    suspend fun createGroup(name: String, color: String, order: Int): OpResult {
        val trimmed = name.trim()
        if (groupDao.findLiveByName(trimmed) != null) {
            return OpResult.Failure("A group named \"$trimmed\" already exists")
        }
        val t = now()
        val id = groupDao.insert(CategoryGroup(name = trimmed, color = color, order = order, createdAt = t, updatedAt = t))
        return OpResult.Success(id)
    }

    suspend fun createCategory(groupId: Long, name: String, kind: Kind, color: String?, order: Int, icon: String? = null): OpResult {
        val trimmed = name.trim()
        if (categoryDao.findLiveByName(trimmed) != null) {
            return OpResult.Failure("A category named \"$trimmed\" already exists")
        }
        val t = now()
        val id = categoryDao.insert(
            Category(groupId = groupId, name = trimmed, kind = kind, color = color, icon = icon, order = order, createdAt = t, updatedAt = t),
        )
        return OpResult.Success(id)
    }

    /** §F2.5: a group can only be archived when it has no live categories. */
    suspend fun archiveGroup(groupId: Long): OpResult {
        if (categoryDao.countLiveInGroup(groupId) > 0) {
            return OpResult.Failure("Archive or move this group's categories first")
        }
        val group = groupDao.getById(groupId) ?: return OpResult.Failure("Group not found")
        groupDao.update(group.copy(archived = true, updatedAt = now()))
        return OpResult.Success(groupId)
    }

    suspend fun archiveCategory(categoryId: Long): OpResult {
        val category = categoryDao.getById(categoryId) ?: return OpResult.Failure("Category not found")
        categoryDao.update(category.copy(archived = true, updatedAt = now()))
        return OpResult.Success(categoryId)
    }

    /** Edit a group (rename/recolor). Rejects a rename onto another live group's name. */
    suspend fun updateGroup(group: CategoryGroup): OpResult {
        val trimmed = group.name.trim()
        val existing = groupDao.findLiveByName(trimmed)
        if (existing != null && existing.id != group.id) {
            return OpResult.Failure("A group named \"$trimmed\" already exists")
        }
        groupDao.update(group.copy(name = trimmed, updatedAt = now()))
        return OpResult.Success(group.id)
    }

    /** Edit a category (rename/recolor/kind/move group). Rejects a rename onto another live category's name. */
    suspend fun updateCategory(category: Category): OpResult {
        val trimmed = category.name.trim()
        val existing = categoryDao.findLiveByName(trimmed)
        if (existing != null && existing.id != category.id) {
            return OpResult.Failure("A category named \"$trimmed\" already exists")
        }
        categoryDao.update(category.copy(name = trimmed, updatedAt = now()))
        return OpResult.Success(category.id)
    }

    /** Persist a new global group order (§F2.6). */
    suspend fun reorderGroups(orderedGroupIds: List<Long>) = groupDao.reorder(orderedGroupIds, now())

    /** Persist a new order for categories within a group (§F2.6). */
    suspend fun reorderCategories(orderedCategoryIds: List<Long>) = categoryDao.reorder(orderedCategoryIds, now())
}
