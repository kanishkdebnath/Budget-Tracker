package com.example.budgettracker.ui.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryIconsTest {

    private val all = CATEGORY_ICON_SECTIONS.flatMap { it.icons }

    @Test fun keysAreUnique() {
        val keys = all.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test fun everyKeyResolves() {
        all.forEach { assertNotNull("no vector for ${it.key}", iconVectorForKey(it.key)) }
    }

    @Test fun seedKeysArePresent() {
        listOf("payments", "home", "bolt", "shopping_cart", "directions_car", "restaurant")
            .forEach { assertNotNull("seed key missing: $it", iconVectorForKey(it)) }
    }

    @Test fun unknownAndNullResolveToNull() {
        assertNull(iconVectorForKey(null))
        assertNull(iconVectorForKey("not_a_real_key"))
    }

    @Test fun searchMatchesLabelAndKey() {
        assertTrue(searchIcons("coffee").any { it.key == "local_cafe" })
        assertTrue(searchIcons("local_cafe").any { it.key == "local_cafe" })
        assertTrue(searchIcons("").size == all.size)
    }
}
