package com.example.budgettracker.data

import com.example.budgettracker.data.db.Converters
import com.example.budgettracker.data.entity.Cadence
import com.example.budgettracker.data.entity.Kind
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test fun kindRoundTrips() {
        assertEquals("INCOME", converters.kindToString(Kind.INCOME))
        assertEquals("EXPENSE", converters.kindToString(Kind.EXPENSE))
        assertEquals(Kind.INCOME, converters.stringToKind("INCOME"))
        assertEquals(Kind.EXPENSE, converters.stringToKind("EXPENSE"))
    }

    @Test fun cadenceRoundTrips() {
        assertEquals("MONTHLY", converters.cadenceToString(Cadence.MONTHLY))
        assertEquals(Cadence.MONTHLY, converters.stringToCadence("MONTHLY"))
    }
}
