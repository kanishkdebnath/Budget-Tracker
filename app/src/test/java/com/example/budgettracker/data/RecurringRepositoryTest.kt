package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.repository.RecurringRepository
import com.example.budgettracker.domain.time.MonthUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecurringRepositoryTest {

    private lateinit var db: BudgetDatabase
    private lateinit var repo: RecurringRepository
    private var categoryId: Long = 0
    private val utc = ZoneId.of("UTC")

    private suspend fun monthTransactions(month: String) =
        MonthUtils.monthRange(month, utc).let {
            db.transactionDao().observeInRange(it.startInclusive.toEpochMilli(), it.endExclusive.toEpochMilli()).first()
        }

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        val gid = db.categoryGroupDao().insert(CategoryGroup(name = "Income", color = "#10B981", order = 0, createdAt = 1L, updatedAt = 1L))
        categoryId = db.categoryDao().insert(Category(groupId = gid, name = "Salary", kind = Kind.INCOME, order = 0, createdAt = 1L, updatedAt = 1L))
        repo = RecurringRepository(db, db.recurringTemplateDao(), db.transactionDao(), now = { 1000L })
    }

    @After
    fun teardown() = db.close()

    @Test
    fun applyCreatesDatedTransactionAndStampsLastRun() = runTest {
        val id = repo.create("Salary", categoryId, amount = 500_000, dayOfMonth = 1)
        assertTrue(repo.apply(id, "2026-06", utc) is OpResult.Success)

        val txns = monthTransactions("2026-06")
        assertEquals(1, txns.size)
        assertEquals(id, txns[0].recurringTemplateId)
        assertEquals(500_000L, txns[0].amount)
        assertEquals("Salary", txns[0].description) // label carries onto the entry (shown in the Log)
        assertEquals("2026-06", db.recurringTemplateDao().getById(id)?.lastRunMonth)
    }

    @Test
    fun secondApplyInSameMonthIsRejectedAndNoDuplicateTransaction() = runTest {
        val id = repo.create("Salary", categoryId, amount = 500_000, dayOfMonth = 1)
        repo.apply(id, "2026-06", utc)
        val second = repo.apply(id, "2026-06", utc)
        assertTrue(second is OpResult.Failure)
        assertEquals(1, monthTransactions("2026-06").size)
    }

    @Test
    fun applyOnInactiveTemplateIsRejected() = runTest {
        val id = repo.create("Bonus", categoryId, amount = 100_000, dayOfMonth = 1, active = false)
        val result = repo.apply(id, "2026-06", utc)
        assertTrue(result is OpResult.Failure)
        assertEquals(0, monthTransactions("2026-06").size)
    }
}
