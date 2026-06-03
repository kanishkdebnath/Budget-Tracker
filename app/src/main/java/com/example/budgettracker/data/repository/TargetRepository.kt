package com.example.budgettracker.data.repository

import androidx.room.withTransaction
import com.example.budgettracker.data.dao.TargetDao
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.Target
import kotlinx.coroutines.flow.Flow

/** Monthly targets (PRODUCT_SPEC F3). Bulk save is atomic. */
class TargetRepository(
    private val db: BudgetDatabase,
    private val targetDao: TargetDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

    fun observeMonth(month: String): Flow<List<Target>> = targetDao.observeByMonth(month)

    suspend fun getMonth(month: String): List<Target> = targetDao.getByMonth(month)

    /**
     * Bulk upsert all targets for [month] in one transaction (§F3.3). A null amount clears that
     * category's target (§F3.5).
     */
    suspend fun bulkSave(month: String, amountsByCategory: Map<Long, Long?>) {
        db.withTransaction {
            val t = now()
            amountsByCategory.forEach { (categoryId, amount) ->
                if (amount == null) {
                    targetDao.deleteByCategoryAndMonth(categoryId, month)
                } else {
                    targetDao.upsert(Target(categoryId = categoryId, month = month, amount = amount, createdAt = t, updatedAt = t))
                }
            }
        }
    }
}
