package com.example.budgettracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgettracker.data.repository.PreferencesRepository
import com.example.budgettracker.ui.theme.DensityMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ThemeMode(val storageValue: String, val label: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark"),
    ;

    fun resolveDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        fun fromStorage(value: String): ThemeMode = entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

/** Common ISO-4217 currencies promoted in the picker (§F7.1); any valid 3-letter code is also accepted. */
data class CurrencyOption(val code: String, val name: String)

val COMMON_CURRENCIES = listOf(
    CurrencyOption("INR", "Indian Rupee"),
    CurrencyOption("USD", "US Dollar"),
    CurrencyOption("EUR", "Euro"),
    CurrencyOption("GBP", "British Pound"),
    CurrencyOption("AUD", "Australian Dollar"),
    CurrencyOption("CAD", "Canadian Dollar"),
    CurrencyOption("SGD", "Singapore Dollar"),
    CurrencyOption("JPY", "Japanese Yen"),
)

fun isValidCurrencyCode(code: String): Boolean = code.matches(Regex("^[A-Za-z]{3}$"))

class SettingsViewModel(private val preferences: PreferencesRepository) : ViewModel() {

    val currency: StateFlow<String> = preferences.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PreferencesRepository.DEFAULT_CURRENCY)

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
        .map { ThemeMode.fromStorage(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val densityMode: StateFlow<DensityMode> = preferences.density
        .map { DensityMode.fromStorage(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DensityMode.COMFORTABLE)

    fun setCurrency(code: String) = viewModelScope.launch { preferences.setCurrency(code.uppercase()) }
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { preferences.setThemeMode(mode.storageValue) }
    fun setDensity(mode: DensityMode) = viewModelScope.launch { preferences.setDensity(mode.storageValue) }
}
