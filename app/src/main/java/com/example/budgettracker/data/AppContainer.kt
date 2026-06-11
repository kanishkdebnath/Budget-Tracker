package com.example.budgettracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.db.DatabaseSeeder
import com.example.budgettracker.data.db.MIGRATION_1_2
import com.example.budgettracker.data.repository.CategoryRepository
import com.example.budgettracker.data.repository.PreferencesRepository
import com.example.budgettracker.data.repository.RecurringRepository
import com.example.budgettracker.data.repository.TargetRepository
import com.example.budgettracker.data.repository.TransactionRepository

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manual dependency container (PRODUCT_SPEC §11 — repositories are simple enough to wire by hand).
 * Instantiated once by BudgetApplication (wired in Phase 4); exposes repositories + the seeder.
 */
class AppContainer(context: Context) {

    private val database = Room.databaseBuilder(context, BudgetDatabase::class.java, "budget.db")
        .addMigrations(MIGRATION_1_2)
        .build()
    private val dataStore = context.settingsDataStore

    val categoryRepository = CategoryRepository(database.categoryGroupDao(), database.categoryDao())
    val transactionRepository = TransactionRepository(database.transactionDao())
    val targetRepository = TargetRepository(database, database.targetDao())
    val recurringRepository = RecurringRepository(database, database.recurringTemplateDao(), database.transactionDao())
    val preferencesRepository = PreferencesRepository(dataStore)
    val seeder = DatabaseSeeder(database, database.categoryGroupDao(), database.categoryDao())
}
