package com.example.budgettracker.domain.time

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Local-timezone month math per PRODUCT_SPEC §7.1.
 * A "month" is a "YYYY-MM" string derived in the device-local ZoneId.
 */
object MonthUtils {

    /** Half-open instant range [startInclusive, endExclusive) covering a month. */
    data class MonthRange(val startInclusive: Instant, val endExclusive: Instant)

    /** The "YYYY-MM" month that [epochMillis] falls in, in [zone]. */
    fun monthOf(epochMillis: Long, zone: ZoneId): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        return YearMonth.from(date).toString()
    }

    /** Instant bounds for querying transactions: date >= start AND date < endExclusive. */
    fun monthRange(month: String, zone: ZoneId): MonthRange {
        val ym = YearMonth.parse(month)
        val start = ym.atDay(1).atStartOfDay(zone).toInstant()
        val end = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()
        return MonthRange(start, end)
    }

    /** Human-readable label, e.g. "June 2026". */
    fun monthLabel(month: String, locale: Locale = Locale.getDefault()): String {
        val ym = YearMonth.parse(month)
        return ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
    }

    /**
     * Epoch millis for [dayOfMonth] of [month] at [hour] local time — the instant a recurring
     * template's transaction is dated when applied (§7.4). [dayOfMonth] is 1–28.
     */
    fun instantForDay(month: String, dayOfMonth: Int, zone: ZoneId, hour: Int = 9): Long {
        val ym = YearMonth.parse(month)
        return ym.atDay(dayOfMonth).atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
    }
}
