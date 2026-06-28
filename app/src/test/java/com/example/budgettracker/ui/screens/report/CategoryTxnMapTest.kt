package com.example.budgettracker.ui.screens.report

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryTxnMapTest {

    private fun cat(id: Long, color: String? = "#ff0000") = Category(
        id = id, groupId = 10, name = "Cat$id", kind = Kind.EXPENSE,
        color = color, order = 0, createdAt = 0, updatedAt = 0,
    )
    private fun group(color: String = "#0000ff") = CategoryGroup(
        id = 10, name = "Bills", color = color, order = 0, createdAt = 0, updatedAt = 0,
    )
    private fun txn(id: Long, categoryId: Long, date: Long, description: String? = "note") =
        TransactionEntity(id = id, categoryId = categoryId, amount = 100_000, description = description, date = date, createdAt = 0, updatedAt = 0)

    @Test fun groupsTransactionsByCategoryId() {
        val result = buildCategoryTxnMap(
            listOf(txn(1, 1, 1000), txn(2, 1, 2000), txn(3, 2, 3000)),
            mapOf(1L to cat(1), 2L to cat(2)),
            mapOf(10L to group()),
        )
        assertEquals(2, result.size)
        assertEquals(2, result[1L]!!.size)
        assertEquals(1, result[2L]!!.size)
    }

    @Test fun rowsWithinCategoryAreSortedDescendingByDate() {
        val result = buildCategoryTxnMap(
            listOf(txn(1, 1, 1000L), txn(2, 1, 3000L), txn(3, 1, 2000L)),
            mapOf(1L to cat(1)),
            mapOf(10L to group()),
        )
        val dates = result[1L]!!.map { it.date }
        assertEquals(listOf(3000L, 2000L, 1000L), dates)
    }

    @Test fun skipsTransactionsWithUnknownCategory() {
        val result = buildCategoryTxnMap(listOf(txn(1, 99, 1000L)), emptyMap(), emptyMap())
        assertTrue(result.isEmpty())
    }

    @Test fun usesGroupColorFallbackWhenCategoryColorIsNull() {
        val result = buildCategoryTxnMap(
            listOf(txn(1, 1, 1000L)),
            mapOf(1L to cat(1, color = null)),
            mapOf(10L to group(color = "#abcdef")),
        )
        assertEquals("#abcdef", result[1L]!!.single().leadingColor)
    }

    @Test fun usesDefaultColorWhenBothCategoryAndGroupColorAreNull() {
        val catNoColor = Category(id = 1, groupId = 10, name = "Cat1", kind = Kind.EXPENSE, color = null, order = 0, createdAt = 0, updatedAt = 0)
        // ponytail: CategoryGroup.color is non-null String; simulate absent group color by omitting the group from the map
        // — group resolves to null, so cat.color ?: group?.color ?: "#64748b" falls through to the default
        val result = buildCategoryTxnMap(listOf(txn(1, 1, 1000L)), mapOf(1L to catNoColor), emptyMap())
        assertEquals("#64748b", result[1L]!!.single().leadingColor)
    }
}
