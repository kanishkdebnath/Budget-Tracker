package com.example.budgettracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.budgettracker.data.entity.Target
import kotlinx.coroutines.flow.Flow

@Dao
interface TargetDao {

    /** Upsert by the unique (month, categoryId) index. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(target: Target): Long

    @Query("SELECT * FROM target WHERE month = :month")
    fun observeByMonth(month: String): Flow<List<Target>>

    @Query("SELECT * FROM target WHERE month = :month")
    suspend fun getByMonth(month: String): List<Target>

    @Query("DELETE FROM target WHERE categoryId = :categoryId AND month = :month")
    suspend fun deleteByCategoryAndMonth(categoryId: Long, month: String)
}
