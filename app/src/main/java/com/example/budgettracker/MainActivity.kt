package com.example.budgettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.budgettracker.ui.BudgetApp
import com.example.budgettracker.ui.screens.settings.ThemeMode
import com.example.budgettracker.ui.theme.BudgetTrackerTheme
import com.example.budgettracker.ui.theme.DensityMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = (application as BudgetApplication).container.preferencesRepository
        setContent {
            val themeModeValue by preferences.themeMode.collectAsStateWithLifecycle(initialValue = "dark")
            val dynamicColor by preferences.dynamicColor.collectAsStateWithLifecycle(initialValue = false)
            val densityValue by preferences.density.collectAsStateWithLifecycle(initialValue = "comfortable")
            val darkTheme = ThemeMode.fromStorage(themeModeValue).resolveDark(isSystemInDarkTheme())
            BudgetTrackerTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor,
                densityMode = DensityMode.fromStorage(densityValue),
            ) {
                BudgetApp()
            }
        }
    }
}
