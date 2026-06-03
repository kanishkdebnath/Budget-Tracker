package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.dao.CategoryDao
import com.example.budgettracker.data.dao.CategoryGroupDao
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CategoryDaoTest {

    private lateinit var db: BudgetDatabase
    private lateinit var groupDao: CategoryGroupDao
    private lateinit var dao: CategoryDao
    private var groupId: Long = 0

    private fun cat(name: String, order: Int = 0, archived: Boolean = false) =
        Category(groupId = groupId, name = name, kind = Kind.EXPENSE, order = order, archived = archived, createdAt = 1L, updatedAt = 1L)

    @Before
    fun setup() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        groupDao = db.categoryGroupDao()
        dao = db.categoryDao()
        groupId = groupDao.insert(CategoryGroup(name = "Bills", color = "#EF4444", order = 0, createdAt = 1L, updatedAt = 1L))
    }

    @After
    fun teardown() = db.close()

    @Test
    fun observeLiveOrdersByGroupThenOrderAndExcludesArchived() = runTest {
        dao.insert(cat("Electricity", order = 1))
        dao.insert(cat("Rent", order = 0))
        dao.insert(cat("Old", order = 2, archived = true))
        val live = dao.observeLive().first()
        assertEquals(listOf("Rent", "Electricity"), live.map { it.name })
    }

    @Test
    fun countLiveInGroupExcludesArchived() = runTest {
        dao.insert(cat("Rent"))
        dao.insert(cat("Electricity"))
        dao.insert(cat("Old", archived = true))
        assertEquals(2, dao.countLiveInGroup(groupId))
    }

    @Test
    fun findLiveByNameIsCaseInsensitive() = runTest {
        dao.insert(cat("Groceries"))
        assertEquals("Groceries", dao.findLiveByName("GROCERIES")?.name)
        assertNull(dao.findLiveByName("missing"))
    }
}
