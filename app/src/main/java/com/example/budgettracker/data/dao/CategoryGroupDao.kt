package com.example.budgettracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.budgettracker.data.entity.CategoryGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryGroupDao {

    @Insert
    suspend fun insert(group: CategoryGroup): Long

    @Update
    suspend fun update(group: CategoryGroup)

    /** Live (non-archived) groups in display order. */
    @Query("SELECT * FROM category_group WHERE archived = 0 ORDER BY `order` ASC")
    fun observeLive(): Flow<List<CategoryGroup>>

    /** All groups incl. archived, in display order (Categories screen "Archived" filter). */
    @Query("SELECT * FROM category_group ORDER BY `order` ASC")
    fun observeAll(): Flow<List<CategoryGroup>>

    @Query("SELECT * FROM category_group WHERE id = :id")
    suspend fun getById(id: Long): CategoryGroup?

    /** Case-insensitive lookup among live groups, for uniqueness enforcement. */
    @Query("SELECT * FROM category_group WHERE archived = 0 AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findLiveByName(name: String): CategoryGroup?

    @Query("SELECT COUNT(*) FROM category_group")
    suspend fun count(): Int

    @Query("UPDATE category_group SET `order` = :order, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setOrder(id: Long, order: Int, updatedAt: Long)

    /** Persist a new global group order, atomically. */
    @Transaction
    suspend fun reorder(orderedIds: List<Long>, updatedAt: Long) {
        orderedIds.forEachIndexed { index, id -> setOrder(id, index, updatedAt) }
    }
}
