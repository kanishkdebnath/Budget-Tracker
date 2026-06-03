package com.example.budgettracker.data.repository

import androidx.room.withTransaction
import com.example.budgettracker.data.OpResult
import com.example.budgettracker.data.dao.RecurringTemplateDao
import com.example.budgettracker.data.dao.TransactionDao
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.RecurringTemplate
import com.example.budgettracker.data.entity.TransactionEntity
import com.example.budgettracker.domain.time.MonthUtils
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

/** Recurring templates (PRODUCT_SPEC F5). Apply is manual, idempotent, and atomic (§7.4). */
class RecurringRepository(
    private val db: BudgetDatabase,
    private val recurringDao: RecurringTemplateDao,
    private val transactionDao: TransactionDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

    fun observeAll(): Flow<List<RecurringTemplate>> = recurringDao.observeAll()

    suspend fun create(label: String, categoryId: Long, amount: Long, dayOfMonth: Int, active: Boolean = true): Long {
        val t = now()
        return recurringDao.insert(
            RecurringTemplate(
                label = label.trim(), categoryId = categoryId, amount = amount, dayOfMonth = dayOfMonth,
                active = active, createdAt = t, updatedAt = t,
            ),
        )
    }

    suspend fun delete(id: Long) = recurringDao.deleteById(id)

    /**
     * Apply template [templateId] to [month] (§7.4): reject if inactive or already applied; otherwise
     * insert the dated transaction and stamp lastRunMonth in one atomic transaction.
     */
    suspend fun apply(templateId: Long, month: String, zone: ZoneId): OpResult {
        val template = recurringDao.getById(templateId) ?: return OpResult.Failure("Template not found")
        if (!template.active) return OpResult.Failure("Template is inactive")
        if (template.lastRunMonth == month) return OpResult.Failure("Already applied this month")

        db.withTransaction {
            val t = now()
            val date = MonthUtils.instantForDay(month, template.dayOfMonth, zone)
            transactionDao.insert(
                TransactionEntity(
                    date = date, categoryId = template.categoryId, amount = template.amount,
                    recurringTemplateId = template.id, createdAt = t, updatedAt = t,
                ),
            )
            recurringDao.setLastRunMonth(template.id, month, t)
        }
        return OpResult.Success(templateId)
    }

    suspend fun update(id: Long, label: String, categoryId: Long, amount: Long, dayOfMonth: Int, active: Boolean) {
        val existing = recurringDao.getById(id) ?: return
        recurringDao.update(
            existing.copy(
                label = label.trim(), categoryId = categoryId, amount = amount,
                dayOfMonth = dayOfMonth, active = active, updatedAt = now(),
            ),
        )
    }

    suspend fun setActive(id: Long, active: Boolean) {
        val existing = recurringDao.getById(id) ?: return
        recurringDao.update(existing.copy(active = active, updatedAt = now()))
    }
}
