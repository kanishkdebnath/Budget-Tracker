package com.example.budgettracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.db.DatabaseSeeder
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
class DatabaseSeederTest {

    private lateinit var db: BudgetDatabase
    private lateinit var seeder: DatabaseSeeder

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), BudgetDatabase::class.java).build()
        seeder = DatabaseSeeder(db, db.categoryGroupDao(), db.categoryDao(), now = { 1L })
    }

    @After
    fun teardown() = db.close()

    @Test
    fun seedsSevenGroupsAndSixCategories() = runTest {
        seeder.seedIfEmpty()
        assertEquals(7, db.categoryGroupDao().observeAll().first().size)
        assertEquals(6, db.categoryDao().observeAll().first().size)
    }

    @Test
    fun seedIsIdempotent() = runTest {
        seeder.seedIfEmpty()
        seeder.seedIfEmpty()
        assertEquals(7, db.categoryGroupDao().observeAll().first().size)
        assertEquals(6, db.categoryDao().observeAll().first().size)
    }
}
