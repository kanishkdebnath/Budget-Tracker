package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.dao.CategoryGroupDao
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.CategoryGroup
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
class CategoryGroupDaoTest {

    private lateinit var db: BudgetDatabase
    private lateinit var dao: CategoryGroupDao

    private fun group(name: String, order: Int = 0, archived: Boolean = false) =
        CategoryGroup(name = name, color = "#EF4444", order = order, archived = archived, createdAt = 1L, updatedAt = 1L)

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        dao = db.categoryGroupDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun observeLiveOrdersByOrderAndExcludesArchived() = runTest {
        dao.insert(group("Bills", order = 1))
        dao.insert(group("Income", order = 0))
        dao.insert(group("Gone", order = 2, archived = true))
        val live = dao.observeLive().first()
        assertEquals(listOf("Income", "Bills"), live.map { it.name })
        assertEquals(3, dao.count())
    }

    @Test
    fun findLiveByNameIsCaseInsensitiveAndSkipsArchived() = runTest {
        dao.insert(group("Bills"))
        assertEquals("Bills", dao.findLiveByName("bills")?.name)
        dao.insert(group("Leisure", archived = true))
        assertNull(dao.findLiveByName("leisure"))
    }
}
