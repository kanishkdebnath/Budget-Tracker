package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.repository.CategoryRepository
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CategoryRepositoryTest {

    private lateinit var db: BudgetDatabase
    private lateinit var repo: CategoryRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        repo = CategoryRepository(db.categoryGroupDao(), db.categoryDao(), now = { 1L })
    }

    @After
    fun teardown() = db.close()

    @Test
    fun duplicateLiveNameRejectedCaseInsensitively() = runTest {
        assertTrue(repo.createGroup("Bills", "#EF4444", 0) is OpResult.Success)
        val dup = repo.createGroup("bills", "#000000", 1)
        assertTrue(dup is OpResult.Failure)
    }

    @Test
    fun archivedNameCanBeReused() = runTest {
        val first = repo.createGroup("Bills", "#EF4444", 0) as OpResult.Success
        assertTrue(repo.archiveGroup(first.id) is OpResult.Success)
        assertTrue(repo.createGroup("Bills", "#EF4444", 1) is OpResult.Success)
    }

    @Test
    fun archiveGroupBlockedWhenLiveCategoriesExist() = runTest {
        val group = repo.createGroup("Bills", "#EF4444", 0) as OpResult.Success
        repo.createCategory(group.id, "Rent", Kind.EXPENSE, null, 0)
        val result = repo.archiveGroup(group.id)
        assertTrue(result is OpResult.Failure)
        assertEquals("Archive or move this group's categories first", (result as OpResult.Failure).reason)
    }

    @Test
    fun renameGroupOntoAnotherLiveNameIsRejected() = runTest {
        repo.createGroup("Bills", "#EF4444", 0)
        val other = repo.createGroup("Income", "#10B981", 1) as OpResult.Success
        val group = db.categoryGroupDao().getById(other.id)!!
        assertTrue(repo.updateGroup(group.copy(name = "bills")) is OpResult.Failure)
    }

    @Test
    fun recolorKeepingOwnNameIsAllowed() = runTest {
        val created = repo.createGroup("Bills", "#EF4444", 0) as OpResult.Success
        val group = db.categoryGroupDao().getById(created.id)!!
        assertTrue(repo.updateGroup(group.copy(color = "#000000")) is OpResult.Success)
        assertEquals("#000000", db.categoryGroupDao().getById(created.id)?.color)
    }

    @Test
    fun reorderGroupsPersistsNewOrder() = runTest {
        val a = repo.createGroup("A", "#000000", 0) as OpResult.Success
        val b = repo.createGroup("B", "#000000", 1) as OpResult.Success
        val c = repo.createGroup("C", "#000000", 2) as OpResult.Success
        repo.reorderGroups(listOf(c.id, a.id, b.id))
        val live = db.categoryGroupDao().observeLive().first()
        assertEquals(listOf("C", "A", "B"), live.map { it.name })
    }
}
