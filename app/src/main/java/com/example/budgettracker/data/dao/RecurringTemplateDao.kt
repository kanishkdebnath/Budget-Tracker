package com.example.budgettracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.budgettracker.data.entity.RecurringTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTemplateDao {

    @Insert
    suspend fun insert(template: RecurringTemplate): Long

    @Update
    suspend fun update(template: RecurringTemplate)

    @Query("DELETE FROM recurring_template WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Active first, then oldest first (§F5.2). */
    @Query("SELECT * FROM recurring_template ORDER BY active DESC, createdAt ASC")
    fun observeAll(): Flow<List<RecurringTemplate>>

    @Query("SELECT * FROM recurring_template WHERE id = :id")
    suspend fun getById(id: Long): RecurringTemplate?

    @Query("UPDATE recurring_template SET lastRunMonth = :month, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setLastRunMonth(id: Long, month: String, updatedAt: Long)
}
