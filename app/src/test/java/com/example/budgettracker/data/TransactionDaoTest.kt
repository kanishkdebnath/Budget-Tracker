package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.dao.TransactionDao
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionDaoTest {

    private lateinit var db: BudgetDatabase
    private lateinit var dao: TransactionDao
    private var categoryId: Long = 0

    private fun txn(date: Long, amount: Long = 10_000L) =
        TransactionEntity(date = date, categoryId = categoryId, amount = amount, createdAt = date, updatedAt = date)

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        val gid = db.categoryGroupDao().insert(CategoryGroup(name = "Bills", color = "#EF4444", order = 0, createdAt = 1L, updatedAt = 1L))
        categoryId = db.categoryDao().insert(Category(groupId = gid, name = "Rent", kind = Kind.EXPENSE, order = 0, createdAt = 1L, updatedAt = 1L))
        dao = db.transactionDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun observeInRangeFiltersHalfOpenAndOrdersDescending() = runTest {
        dao.insert(txn(date = 500))   // before range
        dao.insert(txn(date = 1000))  // start inclusive
        dao.insert(txn(date = 1500))  // inside
        dao.insert(txn(date = 2000))  // end exclusive -> excluded
        val inRange = dao.observeInRange(1000, 2000).first()
        assertEquals(listOf(1500L, 1000L), inRange.map { it.date })
    }

    @Test
    fun deleteByIdHardDeletes() = runTest {
        val id = dao.insert(txn(date = 1200))
        dao.deleteById(id)
        assertEquals(emptyList<Long>(), dao.observeInRange(0, Long.MAX_VALUE).first().map { it.id })
    }
}
