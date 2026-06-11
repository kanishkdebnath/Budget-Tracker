package com.example.budgettracker.data.db

import androidx.room.withTransaction
import com.example.budgettracker.data.dao.CategoryDao
import com.example.budgettracker.data.dao.CategoryGroupDao
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup

/** Seeds default groups/categories on first launch (§6.7). */
class DatabaseSeeder(
    private val db: BudgetDatabase,
    private val groupDao: CategoryGroupDao,
    private val categoryDao: CategoryDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

    /** Idempotent (§F2.7): seeds only when no groups exist. Atomic. */
    suspend fun seedIfEmpty() {
        db.withTransaction {
            if (groupDao.count() > 0) return@withTransaction
            val t = now()
            SeedData.GROUPS.forEachIndexed { groupOrder, group ->
                val groupId = groupDao.insert(
                    CategoryGroup(name = group.name, color = group.color, order = groupOrder, createdAt = t, updatedAt = t),
                )
                group.categories.forEachIndexed { categoryOrder, category ->
                    categoryDao.insert(
                        Category(
                            groupId = groupId, name = category.name, kind = category.kind, icon = category.icon,
                            order = categoryOrder, createdAt = t, updatedAt = t,
                        ),
                    )
                }
            }
        }
    }
}
