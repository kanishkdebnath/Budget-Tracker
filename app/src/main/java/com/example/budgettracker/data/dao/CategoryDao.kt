package com.example.budgettracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.budgettracker.data.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    /** Live categories, grouped by group then order. */
    @Query("SELECT * FROM category WHERE archived = 0 ORDER BY groupId ASC, `order` ASC")
    fun observeLive(): Flow<List<Category>>

    @Query("SELECT * FROM category ORDER BY groupId ASC, `order` ASC")
    fun observeAll(): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getById(id: Long): Category?

    /** Case-insensitive lookup among live categories, for uniqueness enforcement. */
    @Query("SELECT * FROM category WHERE archived = 0 AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findLiveByName(name: String): Category?

    /** Live category count in a group — gates group archiving (§F2.5). */
    @Query("SELECT COUNT(*) FROM category WHERE groupId = :groupId AND archived = 0")
    suspend fun countLiveInGroup(groupId: Long): Int

    @Query("UPDATE category SET `order` = :order, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setOrder(id: Long, order: Int, updatedAt: Long)

    /** Persist a new category order, atomically. */
    @Transaction
    suspend fun reorder(orderedIds: List<Long>, updatedAt: Long) {
        orderedIds.forEachIndexed { index, id -> setOrder(id, index, updatedAt) }
    }
}
