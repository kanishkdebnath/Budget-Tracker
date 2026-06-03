package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.repository.CategoryRepository
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
}
