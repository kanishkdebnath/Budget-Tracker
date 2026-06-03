package com.example.budgettracker.domain.money

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test fun wholeAmountOmitsDecimals() {
        assertEquals("₹500", Money.format(50000, "INR"))
    }

    @Test fun fractionalAmountShowsTwoDecimals() {
        assertEquals("₹500.50", Money.format(50050, "INR"))
    }

    @Test fun fractionalAmountPadsSingleDigitCents() {
        assertEquals("₹500.05", Money.format(50005, "INR"))
    }

    @Test fun inrUsesIndianLakhCroreGrouping() {
        // 100000000 minor = 1,000,000.00 major = ten lakh
        assertEquals("₹10,00,000", Money.format(100000000, "INR"))
    }

    @Test fun inrGroupingForTenThousandMajor() {
        // 1000000 minor = 10,000.00 major (spec §9 third example has a typo in the minor amount)
        assertEquals("₹10,000", Money.format(1000000, "INR"))
    }

    @Test fun usdUsesWesternThousandsGrouping() {
        assertEquals("$1,000,000", Money.format(100000000, "USD"))
    }

    @Test fun jpyHasNoDecimals() {
        assertEquals("¥1,000", Money.format(100000, "JPY"))
    }

    @Test fun jpyTruncatesFractionalMinorUnits() {
        assertEquals("¥1,000", Money.format(100050, "JPY"))
    }

    @Test fun knownSymbolsRenderCorrectly() {
        assertEquals("€500", Money.format(50000, "EUR"))
        assertEquals("£500", Money.format(50000, "GBP"))
        assertEquals("A$500", Money.format(50000, "AUD"))
        assertEquals("C$500", Money.format(50000, "CAD"))
        assertEquals("S$500", Money.format(50000, "SGD"))
    }

    @Test fun unknownCurrencyUsesCodePrefix() {
        assertEquals("XYZ 500", Money.format(50000, "XYZ"))
    }

    @Test fun zeroFormatsCleanly() {
        assertEquals("₹0", Money.format(0, "INR"))
    }

    @Test fun negativeAmountPrefixesMinusBeforeSymbol() {
        assertEquals("-₹500.50", Money.format(-50050, "INR"))
    }

    @Test fun parsesWholeAndGroupedAndDecimalInput() {
        assertEquals(50000L, Money.parseToMinor("500"))
        assertEquals(120000L, Money.parseToMinor("1,200"))
        assertEquals(120050L, Money.parseToMinor("1200.50"))
        assertEquals(120050L, Money.parseToMinor("1200.5"))
        assertEquals(100000000L, Money.parseToMinor("10,00,000")) // Indian grouping input
    }

    @Test fun rejectsInvalidAmountInput() {
        assertEquals(null, Money.parseToMinor(""))
        assertEquals(null, Money.parseToMinor("0"))        // must be > 0
        assertEquals(null, Money.parseToMinor("-5"))
        assertEquals(null, Money.parseToMinor("1e3"))
        assertEquals(null, Money.parseToMinor("₹5"))
        assertEquals(null, Money.parseToMinor("1200.555")) // > 2 decimals
        assertEquals(null, Money.parseToMinor("12.."))
    }
}
