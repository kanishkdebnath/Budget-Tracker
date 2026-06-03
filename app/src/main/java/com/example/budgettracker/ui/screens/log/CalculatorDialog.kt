package com.example.budgettracker.ui.screens.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.budgettracker.domain.calc.CalcOp
import com.example.budgettracker.domain.calc.Calculator
import com.example.budgettracker.domain.money.Money

/** Calculator popover for amount entry (F8). Feeds the computed value back to the amount field. */
@Composable
fun CalculatorDialog(initial: String, currency: String, onDismiss: () -> Unit, onResult: (String) -> Unit) {
    var state by remember { mutableStateOf(Calculator.fromInitial(initial)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = Money.symbolOf(currency) + state.display,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )

                CalcRow {
                    CalcKey("C", KeyKind.ERROR) { state = Calculator.clear() }
                    CalcKey("⌫", KeyKind.MUTED) { state = Calculator.backspace(state) }
                    CalcKey("÷", KeyKind.OP) { state = Calculator.op(state, CalcOp.DIV) }
                    CalcKey("×", KeyKind.OP) { state = Calculator.op(state, CalcOp.MUL) }
                }
                CalcRow {
                    CalcKey("7") { state = Calculator.digit(state, '7') }
                    CalcKey("8") { state = Calculator.digit(state, '8') }
                    CalcKey("9") { state = Calculator.digit(state, '9') }
                    CalcKey("−", KeyKind.OP) { state = Calculator.op(state, CalcOp.SUB) }
                }
                CalcRow {
                    CalcKey("4") { state = Calculator.digit(state, '4') }
                    CalcKey("5") { state = Calculator.digit(state, '5') }
                    CalcKey("6") { state = Calculator.digit(state, '6') }
                    CalcKey("+", KeyKind.OP) { state = Calculator.op(state, CalcOp.ADD) }
                }
                CalcRow {
                    CalcKey("1") { state = Calculator.digit(state, '1') }
                    CalcKey("2") { state = Calculator.digit(state, '2') }
                    CalcKey("3") { state = Calculator.digit(state, '3') }
                    CalcKey("=", KeyKind.PRIMARY) { state = Calculator.equals(state) }
                }
                CalcRow {
                    CalcKey("0", weight = 2f) { state = Calculator.digit(state, '0') }
                    CalcKey(".") { state = Calculator.decimal(state) }
                    CalcKey("Use", KeyKind.PRIMARY) {
                        val result = Calculator.equals(state)
                        onResult(result.display)
                        onDismiss()
                    }
                }
            }
        }
    }
}

private enum class KeyKind { DIGIT, OP, PRIMARY, ERROR, MUTED }

@Composable
private fun CalcRow(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
private fun RowScope.CalcKey(label: String, kind: KeyKind = KeyKind.DIGIT, weight: Float = 1f, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val (container, content) = when (kind) {
        KeyKind.DIGIT -> cs.surfaceVariant to cs.onSurface
        KeyKind.OP -> cs.primaryContainer to cs.onPrimaryContainer
        KeyKind.PRIMARY -> cs.primary to cs.onPrimary
        KeyKind.ERROR -> cs.errorContainer to cs.onErrorContainer
        KeyKind.MUTED -> cs.surfaceVariant to cs.onSurfaceVariant
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(weight).height(56.dp),
        shape = RoundedCornerShape(14.dp),
        color = container,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleLarge, color = content)
        }
    }
}
