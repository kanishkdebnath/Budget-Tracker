package com.example.budgettracker.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.budgettracker.BudgetApplication
import com.example.budgettracker.ui.screens.categories.CategoriesViewModel
import com.example.budgettracker.ui.screens.log.LogViewModel
import com.example.budgettracker.ui.screens.plan.PlanViewModel
import com.example.budgettracker.ui.screens.report.ReportViewModel

/** Builds ViewModels from the [BudgetApplication] container (manual DI — no Hilt). */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as BudgetApplication
            CategoriesViewModel(app.container.categoryRepository)
        }
        initializer {
            val app = this[APPLICATION_KEY] as BudgetApplication
            LogViewModel(
                app.container.transactionRepository,
                app.container.categoryRepository,
                app.container.preferencesRepository,
            )
        }
        initializer {
            val app = this[APPLICATION_KEY] as BudgetApplication
            PlanViewModel(
                app.container.categoryRepository,
                app.container.targetRepository,
                app.container.preferencesRepository,
            )
        }
        initializer {
            val app = this[APPLICATION_KEY] as BudgetApplication
            ReportViewModel(
                app.container.transactionRepository,
                app.container.targetRepository,
                app.container.categoryRepository,
                app.container.recurringRepository,
                app.container.preferencesRepository,
            )
        }
    }
}
