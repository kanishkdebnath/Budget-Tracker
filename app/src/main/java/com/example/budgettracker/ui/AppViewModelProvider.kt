package com.example.budgettracker.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.budgettracker.BudgetApplication
import com.example.budgettracker.ui.screens.categories.CategoriesViewModel

/** Builds ViewModels from the [BudgetApplication] container (manual DI — no Hilt). */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as BudgetApplication
            CategoriesViewModel(app.container.categoryRepository)
        }
    }
}
