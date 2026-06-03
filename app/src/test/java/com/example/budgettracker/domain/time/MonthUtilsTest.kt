package com.example.budgettracker.domain.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class MonthUtilsTest {

    private val kolkata = ZoneId.of("Asia/Kolkata")
    private val utc = ZoneId.of("UTC")

    @Test fun monthOfReturnsLocalYearMonth() {
        val epoch = Instant.parse("2026-06-15T10:00:00Z").toEpochMilli()
        assertEquals("2026-06", MonthUtils.monthOf(epoch, kolkata))
    }

    @Test fun monthOfUsesLocalTimezoneNotUtc() {
        // 2026-06-30 20:00 UTC == 2026-07-01 01:30 IST -> belongs to July locally
        val epoch = Instant.parse("2026-06-30T20:00:00Z").toEpochMilli()
        assertEquals("2026-07", MonthUtils.monthOf(epoch, kolkata))
        assertEquals("2026-06", MonthUtils.monthOf(epoch, utc))
    }

    @Test fun monthRangeIsHalfOpen() {
        val range = MonthUtils.monthRange("2026-02", utc)
        assertEquals(Instant.parse("2026-02-01T00:00:00Z"), range.startInclusive)
        assertEquals(Instant.parse("2026-03-01T00:00:00Z"), range.endExclusive)
    }

    @Test fun monthRangeRespectsZoneOffset() {
        // Start of 2026-06 in IST is 2026-05-31T18:30:00Z
        val range = MonthUtils.monthRange("2026-06", kolkata)
        assertEquals(Instant.parse("2026-05-31T18:30:00Z"), range.startInclusive)
        assertEquals(Instant.parse("2026-06-30T18:30:00Z"), range.endExclusive)
    }

    @Test fun monthLabelIsHumanReadable() {
        assertEquals("June 2026", MonthUtils.monthLabel("2026-06", Locale.ENGLISH))
    }
}
