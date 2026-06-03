package com.example.budgettracker

import android.app.Application
import com.example.budgettracker.data.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** App entry point: owns the [AppContainer] and seeds the database on first launch (§6.7). */
class BudgetApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch { container.seeder.seedIfEmpty() }
    }
}
