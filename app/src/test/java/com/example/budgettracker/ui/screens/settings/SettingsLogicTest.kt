package com.example.budgettracker.ui.screens.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsLogicTest {

    @Test fun themeModeResolvesDark() {
        assertTrue(ThemeMode.DARK.resolveDark(systemDark = false))
        assertFalse(ThemeMode.LIGHT.resolveDark(systemDark = true))
        assertTrue(ThemeMode.SYSTEM.resolveDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.resolveDark(systemDark = false))
    }

    @Test fun themeModeFromStorageFallsBackToSystem() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("dark"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("nonsense"))
    }

    @Test fun validatesCurrencyCodes() {
        assertTrue(isValidCurrencyCode("usd"))
        assertTrue(isValidCurrencyCode("INR"))
        assertFalse(isValidCurrencyCode("US"))
        assertFalse(isValidCurrencyCode("US1"))
        assertFalse(isValidCurrencyCode(""))
    }
}
