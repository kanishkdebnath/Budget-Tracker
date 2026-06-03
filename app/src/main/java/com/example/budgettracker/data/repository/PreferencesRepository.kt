package com.example.budgettracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** User preferences in DataStore (PRODUCT_SPEC §6.6 / F7): display currency + UI density. */
class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val currency: Flow<String> = dataStore.data.map { it[CURRENCY] ?: DEFAULT_CURRENCY }
    val density: Flow<String> = dataStore.data.map { it[DENSITY] ?: DEFAULT_DENSITY }

    suspend fun setCurrency(code: String) {
        dataStore.edit { it[CURRENCY] = code }
    }

    suspend fun setDensity(mode: String) {
        dataStore.edit { it[DENSITY] = mode }
    }

    companion object {
        const val DEFAULT_CURRENCY = "INR"
        const val DEFAULT_DENSITY = "comfortable"
        val CURRENCY = stringPreferencesKey("currency")
        val DENSITY = stringPreferencesKey("density")
    }
}
