package com.example.budgettracker.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.budgettracker.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PreferencesRepositoryTest {

    @Test
    fun defaultsToInrAndPersistsChanges() = runTest {
        val file = File.createTempFile("settings", ".preferences_pb").apply { delete() }
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
        val repo = PreferencesRepository(store)

        assertEquals("INR", repo.currency.first())
        repo.setCurrency("USD")
        assertEquals("USD", repo.currency.first())
    }
}
