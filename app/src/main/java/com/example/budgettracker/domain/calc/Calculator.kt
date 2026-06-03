package com.example.budgettracker.domain.calc

import kotlin.math.floor
import kotlin.math.roundToLong

enum class CalcOp(val symbol: String) { ADD("+"), SUB("−"), MUL("×"), DIV("÷") }

/** Immutable calculator state for the amount-entry popover (PRODUCT_SPEC F8). */
data class CalcState(
    val display: String = "0",
    val accumulator: Double = 0.0,
    val pending: CalcOp? = null,
    val startNew: Boolean = true,
)

/**
 * A basic left-to-right calculator (no operator precedence — each operator applies to the running
 * result, like a phone calculator). All transitions are pure.
 */
object Calculator {

    fun fromInitial(initial: String): CalcState {
        val d = initial.replace(",", "").trim()
        return if (d.isNotEmpty() && d.toDoubleOrNull() != null) CalcState(display = d, startNew = false) else CalcState()
    }

    fun digit(s: CalcState, c: Char): CalcState =
        if (s.startNew) s.copy(display = c.toString(), startNew = false)
        else s.copy(display = if (s.display == "0") c.toString() else s.display + c)

    fun decimal(s: CalcState): CalcState = when {
        s.startNew -> s.copy(display = "0.", startNew = false)
        "." in s.display -> s
        else -> s.copy(display = s.display + ".")
    }

    fun op(s: CalcState, op: CalcOp): CalcState {
        val current = s.display.toDoubleOrNull() ?: 0.0
        val acc = if (s.pending != null && !s.startNew) apply(s.accumulator, current, s.pending) else current
        return CalcState(display = format(acc), accumulator = acc, pending = op, startNew = true)
    }

    fun equals(s: CalcState): CalcState {
        val current = s.display.toDoubleOrNull() ?: 0.0
        val result = if (s.pending != null) apply(s.accumulator, current, s.pending) else current
        return CalcState(display = format(result), accumulator = result, pending = null, startNew = true)
    }

    fun backspace(s: CalcState): CalcState =
        if (s.startNew) s
        else s.display.dropLast(1).let { d -> s.copy(display = if (d.isEmpty() || d == "-") "0" else d) }

    fun clear(): CalcState = CalcState()

    private fun apply(a: Double, b: Double, op: CalcOp): Double = when (op) {
        CalcOp.ADD -> a + b
        CalcOp.SUB -> a - b
        CalcOp.MUL -> a * b
        CalcOp.DIV -> if (b == 0.0) a else a / b
    }

    /** Format a result to at most 2 decimals, trimming trailing zeros (17.0 -> "17", 2.50 -> "2.5"). */
    fun format(d: Double): String {
        val rounded = (d * 100).roundToLong() / 100.0
        return if (rounded == floor(rounded)) rounded.toLong().toString()
        else rounded.toString().trimEnd('0').trimEnd('.')
    }
}
