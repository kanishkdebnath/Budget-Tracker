package com.example.budgettracker.domain.calc

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorTest {

    private fun digits(s: CalcState, text: String): CalcState =
        text.fold(s) { acc, c -> Calculator.digit(acc, c) }

    @Test fun addition() {
        var s = Calculator.clear()
        s = digits(s, "12")
        s = Calculator.op(s, CalcOp.ADD)
        s = digits(s, "5")
        assertEquals("17", Calculator.equals(s).display)
    }

    @Test fun chainsLeftToRightWithoutPrecedence() {
        var s = Calculator.clear()
        s = digits(s, "100")
        s = Calculator.op(s, CalcOp.SUB)
        s = digits(s, "30")
        s = Calculator.op(s, CalcOp.ADD)
        s = digits(s, "5")
        assertEquals("75", Calculator.equals(s).display)
    }

    @Test fun divisionYieldsDecimal() {
        var s = Calculator.clear()
        s = digits(s, "10")
        s = Calculator.op(s, CalcOp.DIV)
        s = digits(s, "4")
        assertEquals("2.5", Calculator.equals(s).display)
    }

    @Test fun decimalMultiplyFromInitial() {
        var s = Calculator.fromInitial("1.5")
        s = Calculator.op(s, CalcOp.MUL)
        s = digits(s, "2")
        assertEquals("3", Calculator.equals(s).display)
    }

    @Test fun backspaceEditsCurrentOperand() {
        var s = Calculator.clear()
        s = digits(s, "129")
        s = Calculator.backspace(s)
        assertEquals("12", s.display)
    }

    @Test fun divideByZeroIsIgnored() {
        var s = Calculator.clear()
        s = digits(s, "8")
        s = Calculator.op(s, CalcOp.DIV)
        s = digits(s, "0")
        assertEquals("8", Calculator.equals(s).display)
    }
}
