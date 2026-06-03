package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.dao.RecurringTemplateDao
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.RecurringTemplate
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
class RecurringTemplateDaoTest {

    private lateinit var db: BudgetDatabase
    private lateinit var dao: RecurringTemplateDao
    private var categoryId: Long = 0

    private fun tmpl(label: String, active: Boolean, createdAt: Long) =
        RecurringTemplate(
            label = label, categoryId = categoryId, amount = 100_000, dayOfMonth = 1,
            active = active, createdAt = createdAt, updatedAt = createdAt,
        )

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        val gid = db.categoryGroupDao().insert(CategoryGroup(name = "Income", color = "#10B981", order = 0, createdAt = 1L, updatedAt = 1L))
        categoryId = db.categoryDao().insert(Category(groupId = gid, name = "Salary", kind = Kind.INCOME, order = 0, createdAt = 1L, updatedAt = 1L))
        dao = db.recurringTemplateDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun observeAllSortsActiveFirstThenOldestFirst() = runTest {
        dao.insert(tmpl("Inactive", active = false, createdAt = 1))
        dao.insert(tmpl("ActiveNewer", active = true, createdAt = 3))
        dao.insert(tmpl("ActiveOlder", active = true, createdAt = 2))
        val all = dao.observeAll().first()
        assertEquals(listOf("ActiveOlder", "ActiveNewer", "Inactive"), all.map { it.label })
    }

    @Test
    fun setLastRunMonthUpdatesField() = runTest {
        val id = dao.insert(tmpl("Salary", active = true, createdAt = 1))
        dao.setLastRunMonth(id, "2026-06", updatedAt = 99)
        assertEquals("2026-06", dao.getById(id)?.lastRunMonth)
    }
}
