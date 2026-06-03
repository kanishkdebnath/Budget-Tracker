package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.dao.TargetDao
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.Target
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
class TargetDaoTest {

    private lateinit var db: BudgetDatabase
    private lateinit var dao: TargetDao
    private var categoryId: Long = 0

    private fun target(month: String, amount: Long) =
        Target(categoryId = categoryId, month = month, amount = amount, createdAt = 1L, updatedAt = 1L)

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        val gid = db.categoryGroupDao().insert(CategoryGroup(name = "Bills", color = "#EF4444", order = 0, createdAt = 1L, updatedAt = 1L))
        categoryId = db.categoryDao().insert(Category(groupId = gid, name = "Rent", kind = Kind.EXPENSE, order = 0, createdAt = 1L, updatedAt = 1L))
        dao = db.targetDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun upsertReplacesOnUniqueMonthCategory() = runTest {
        dao.upsert(target("2026-06", 100_000))
        dao.upsert(target("2026-06", 250_000)) // same (month, category) -> replace
        val targets = dao.getByMonth("2026-06")
        assertEquals(1, targets.size)
        assertEquals(250_000L, targets[0].amount)
    }

    @Test
    fun deleteByCategoryAndMonthRemovesIt() = runTest {
        dao.upsert(target("2026-06", 100_000))
        dao.deleteByCategoryAndMonth(categoryId, "2026-06")
        assertEquals(emptyList<Target>(), dao.getByMonth("2026-06"))
    }
}
