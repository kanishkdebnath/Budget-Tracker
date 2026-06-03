package com.example.budgettracker.domain.money

import kotlin.math.abs

/**
 * Formats Long minor units (1/100 of the major unit) for display per PRODUCT_SPEC §9.
 * Currency is a single user preference and is NOT stored per amount.
 */
object Money {

    private data class Format(
        val symbol: String,
        val decimals: Int,
        val indianGrouping: Boolean,
    )

    private val FORMATS: Map<String, Format> = mapOf(
        "INR" to Format("₹", 2, indianGrouping = true),
        "USD" to Format("$", 2, indianGrouping = false),
        "EUR" to Format("€", 2, indianGrouping = false),
        "GBP" to Format("£", 2, indianGrouping = false),
        "AUD" to Format("A$", 2, indianGrouping = false),
        "CAD" to Format("C$", 2, indianGrouping = false),
        "SGD" to Format("S$", 2, indianGrouping = false),
        "JPY" to Format("¥", 0, indianGrouping = false),
    )

    fun format(minor: Long, currency: String): String {
        val fmt = FORMATS[currency]
        val symbol = fmt?.symbol ?: "$currency "
        val decimals = fmt?.decimals ?: 2
        val indian = fmt?.indianGrouping ?: false

        val negative = minor < 0
        val absMinor = abs(minor)
        val whole = absMinor / 100
        val cents = (absMinor % 100).toInt()

        val grouped = if (indian) groupIndian(whole) else groupWestern(whole)
        val body = if (decimals > 0 && cents != 0) {
            "$grouped.${cents.toString().padStart(2, '0')}"
        } else {
            grouped
        }
        val sign = if (negative) "-" else ""
        return "$sign$symbol$body"
    }

    /** Western grouping: a comma every 3 digits from the right (1,000,000). */
    private fun groupWestern(n: Long): String = insertSeparators(n.toString(), 3, 3)

    /** Indian grouping: groups of 3 then 2 (10,00,000). */
    private fun groupIndian(n: Long): String = insertSeparators(n.toString(), 3, 2)

    /**
     * Inserts commas right-to-left: [firstGroup] digits closest to the right,
     * then repeating groups of [restGroup].
     */
    private fun insertSeparators(digits: String, firstGroup: Int, restGroup: Int): String {
        if (digits.length <= firstGroup) return digits
        val tail = digits.substring(digits.length - firstGroup)
        val head = digits.substring(0, digits.length - firstGroup)
        val sb = StringBuilder()
        var count = 0
        for (i in head.indices.reversed()) {
            sb.append(head[i])
            count++
            if (count % restGroup == 0 && i != 0) sb.append(',')
        }
        return sb.reverse().toString() + "," + tail
    }
}
