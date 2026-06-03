package com.example.budgettracker.domain.report

import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.domain.money.Money
import com.example.budgettracker.domain.time.MonthUtils
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Deterministic monthly narrative (§7.3), no LLM. Three cases: no transactions; transactions but no
 * expense plan; otherwise an over/under summary plus the biggest overage and underspend.
 */
fun generateNarrative(month: String, data: ReportData, currency: String, hasTransactions: Boolean): String {
    val label = MonthUtils.monthLabel(month)
    if (!hasTransactions) return "In $label, no transactions were logged."

    fun money(v: Long) = Money.format(v, currency)
    val expenseActual = data.actuals.expense
    val expenseTarget = data.targets.expense

    if (expenseTarget == 0L) {
        return "In $label, you spent ${money(expenseActual)} — no plan was set for this month."
    }

    val diff = expenseActual - expenseTarget
    val direction = if (diff > 0) "over" else "under"
    val pct = ((abs(diff).toDouble() / expenseTarget) * 100).roundToInt()
    val sb = StringBuilder(
        "In $label, you spent ${money(expenseActual)} against a planned ${money(expenseTarget)} — " +
            "$direction by ${money(abs(diff))} ($pct%).",
    )

    val expenseRows = data.groups.filter { it.kind == Kind.EXPENSE }.flatMap { it.rows }
    expenseRows.filter { it.delta > 0 }.maxByOrNull { it.delta }?.let {
        sb.append(" Biggest overage: ${it.category.name} (${money(it.actual)} vs ${money(it.target)}).")
    }
    expenseRows.filter { it.target - it.actual > 0 }.maxByOrNull { it.target - it.actual }?.let {
        sb.append(" Biggest underspend: ${it.category.name} (${money(it.actual)} vs ${money(it.target)}).")
    }
    return sb.toString()
}
