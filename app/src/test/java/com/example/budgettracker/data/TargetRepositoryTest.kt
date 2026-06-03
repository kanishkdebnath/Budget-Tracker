package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.repository.TargetRepository
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
class TargetRepositoryTest {

    private lateinit var db: BudgetDatabase
    private lateinit var repo: TargetRepository
    private var cat1: Long = 0
    private var cat2: Long = 0

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        val gid = db.categoryGroupDao().insert(CategoryGroup(name = "Bills", color = "#EF4444", order = 0, createdAt = 1L, updatedAt = 1L))
        cat1 = db.categoryDao().insert(Category(groupId = gid, name = "Rent", kind = Kind.EXPENSE, order = 0, createdAt = 1L, updatedAt = 1L))
        cat2 = db.categoryDao().insert(Category(groupId = gid, name = "Electricity", kind = Kind.EXPENSE, order = 1, createdAt = 1L, updatedAt = 1L))
        repo = TargetRepository(db, db.targetDao(), now = { 1L })
    }

    @After
    fun teardown() = db.close()

    @Test
    fun bulkSaveUpsertsAmountsAndClearsNulls() = runTest {
        repo.bulkSave("2026-06", mapOf(cat1 to 100_000L, cat2 to 200_000L))
        assertEquals(2, repo.getMonth("2026-06").size)

        // Clear cat1 (null), update cat2.
        repo.bulkSave("2026-06", mapOf(cat1 to null, cat2 to 300_000L))
        val after = repo.getMonth("2026-06")
        assertEquals(1, after.size)
        assertEquals(cat2, after[0].categoryId)
        assertEquals(300_000L, after[0].amount)
    }
}
