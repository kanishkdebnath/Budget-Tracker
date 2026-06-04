package com.example.budgettracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** User preferences in DataStore (PRODUCT_SPEC §6.6 / F7): currency, density, theme. */
class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val currency: Flow<String> = dataStore.data.map { it[CURRENCY] ?: DEFAULT_CURRENCY }
    val density: Flow<String> = dataStore.data.map { it[DENSITY] ?: DEFAULT_DENSITY }
    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE] ?: DEFAULT_THEME_MODE }

    suspend fun setCurrency(code: String) {
        dataStore.edit { it[CURRENCY] = code }
    }

    suspend fun setDensity(mode: String) {
        dataStore.edit { it[DENSITY] = mode }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[THEME_MODE] = mode }
    }

    companion object {
        const val DEFAULT_CURRENCY = "INR"
        const val DEFAULT_DENSITY = "comfortable"
        const val DEFAULT_THEME_MODE = "dark" // design is dark-first (matches the mockups)
        val CURRENCY = stringPreferencesKey("currency")
        val DENSITY = stringPreferencesKey("density")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
