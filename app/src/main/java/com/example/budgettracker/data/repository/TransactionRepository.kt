package com.example.budgettracker.data.repository

import com.example.budgettracker.data.dao.TransactionDao
import com.example.budgettracker.data.entity.TransactionEntity
import com.example.budgettracker.domain.time.MonthUtils
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

/** Transactions (PRODUCT_SPEC F1). Month scoping uses local-TZ ranges from MonthUtils. */
class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

    fun observeMonth(month: String, zone: ZoneId): Flow<List<TransactionEntity>> {
        val range = MonthUtils.monthRange(month, zone)
        return transactionDao.observeInRange(range.startInclusive.toEpochMilli(), range.endExclusive.toEpochMilli())
    }

    suspend fun add(date: Long, categoryId: Long, amount: Long, description: String? = null): Long {
        val t = now()
        return transactionDao.insert(
            TransactionEntity(date = date, categoryId = categoryId, amount = amount, description = description, createdAt = t, updatedAt = t),
        )
    }

    suspend fun update(transaction: TransactionEntity) =
        transactionDao.update(transaction.copy(updatedAt = now()))

    /** Hard delete (§F1.4). */
    suspend fun delete(id: Long) = transactionDao.deleteById(id)
}
