package com.example.budgettracker.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.db.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Migration1To2Test {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BudgetDatabase::class.java,
    )

    @Test
    fun migrate1To2_addsIconColumn_andBackfillsSeedNames() {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO category_group (id, name, color, `order`, archived, createdAt, updatedAt) " +
                    "VALUES (1, 'Bills', '#ef4444', 0, 0, 0, 0)",
            )
            execSQL(
                "INSERT INTO category (id, groupId, name, kind, color, `order`, archived, createdAt, updatedAt) " +
                    "VALUES (1, 1, 'Rent', 'EXPENSE', NULL, 0, 0, 0, 0)",
            )
            execSQL(
                "INSERT INTO category (id, groupId, name, kind, color, `order`, archived, createdAt, updatedAt) " +
                    "VALUES (2, 1, 'My Custom', 'EXPENSE', NULL, 1, 0, 0, 0)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)

        db.query("SELECT name, icon FROM category ORDER BY id").use { c ->
            assertEquals(2, c.count)
            c.moveToFirst()
            assertEquals("Rent", c.getString(0))
            assertEquals("home", c.getString(1))
            c.moveToNext()
            assertEquals("My Custom", c.getString(0))
            assertNull(c.getString(1))
        }
        db.close()
    }
}
