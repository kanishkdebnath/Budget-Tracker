# Phase 6b — Calculator Popover (F8) — Implementation Plan

> Follow-up to Phase 6. REQUIRED SUB-SKILL: superpowers:executing-plans.

**Goal:** A calculator popover bound to the transaction amount field (F8): `+ − × ÷ = C ⌫ .`, live display, result fed back to the field.

**Architecture:** A pure `Calculator` state machine (`domain/calc`) — basic left-to-right evaluation (no precedence, like a phone calculator), unit-tested. The UI is a `Dialog` keypad (not a nested bottom sheet) opened from a calculator trailing-icon on the amount `OutlinedTextField`; "Use" computes and writes the result back via `onResult`.

## Files
- `domain/calc/Calculator.kt` — `CalcState`, `CalcOp`, pure transitions (`digit/decimal/op/equals/backspace/clear/fromInitial/format`).
- `ui/screens/log/CalculatorDialog.kt` — `Dialog` with display + 5×4 keypad (op keys primary-tint, `=`/`Use` filled, `C` error-tint).
- `ui/screens/log/TransactionSheet.kt` — calculator trailing icon + `CalculatorDialog` wiring.
- test: `CalculatorTest` (add/chain/divide/decimal/backspace/÷0).

**Also fixed (same file):** the transaction sheet now defaults its category once `liveCategories` loads (it could open before the Flow emitted, leaving Save disabled).

## Verification (done)
- `./gradlew test` green (63 tests; calculator engine covered).
- Emulator: opened the popover from the amount field, computed `1200 + 50 = 1250`, "Use" wrote ₹1250 back to the field; category now defaults to Salary.

## Self-Review
- **Spec coverage:** F8 (calculator overlay feeding the amount field). Completes F1+F8.
- **Note:** the calculator evaluates in `Double` (an entry helper); the field still parses the result to `Long` minor units via `Money.parseToMinor`, so storage stays integer.
