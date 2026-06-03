package com.example.budgettracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.budgettracker.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(txn: TransactionEntity): Long

    @Update
    suspend fun update(txn: TransactionEntity)

    /** Hard delete (§6.3 — no archive for transactions). */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Transactions whose date falls in [startInclusive, endExclusive). */
    @Query(
        "SELECT * FROM transactions WHERE date >= :startInclusive AND date < :endExclusive " +
            "ORDER BY date DESC, createdAt DESC",
    )
    fun observeInRange(startInclusive: Long, endExclusive: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?
}
