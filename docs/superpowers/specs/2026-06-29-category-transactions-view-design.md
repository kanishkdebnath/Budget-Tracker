# Category Transactions View — Design Spec

**Date:** 2026-06-29  
**Status:** Approved  
**Feature:** View transactions grouped/filtered by category for a selected month

---

## 1. Overview

Two entry points, same goal — see all transactions for a specific category in the current month:

1. **Log screen** — a "Category" filter chip that opens a picker sheet. The existing date-grouped list filters to only that category's transactions when a category is selected.
2. **Report screen** — tapping a category row expands it inline to reveal its individual transactions beneath the Plan/Actual/Δ summary.

No new navigation destinations. No new files beyond `LogComponents.kt` additions.

---

## 2. Data / ViewModel Changes

### 2.1 LogViewModel

Add a `_selectedCategoryId: MutableStateFlow<Long?>` (null = no category filter).

Expose:
```kotlin
val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()
fun selectCategory(id: Long?) { _selectedCategoryId.value = id }
```

Thread `selectedCategoryId` into the existing `combine(...)` block alongside `_filter`. Modify `buildLogState` to accept an additional `categoryId: Long?` parameter:

- When non-null, filter `resolved` to only transactions whose `category.id == categoryId` **before** applying the kind filter.
- NetBand totals (`income`, `expense`, `net`) continue to reflect the **full month** regardless of category filter — same convention as the existing kind filter.
- The kind filter and category filter stack (AND logic).

### 2.2 ReportViewModel + ReportUiState

`ReportData` contains only aggregated actuals; individual transactions are not present. Add:

```kotlin
data class ReportUiState(
    ...
    val transactionsByCategoryId: Map<Long, List<TxnRow>>,  // new
)
```

Computed in the existing `combine` block from `scoped.transactions`, reusing the same `categoriesById` / `groupsById` maps already constructed there. Each `TransactionEntity` is mapped to a `TxnRow` (imported from `ui.screens.log`) using the same field mapping as `buildLogState`.

`TxnRow` is imported cross-package from `ui.screens.log` — no refactor needed.

---

## 3. Log Screen — Category Filter Chip + Picker Sheet

### 3.1 Chip row

Below the existing `LogFilterChips` row, add a second chip row containing a single `FilterChip`:

- **Unselected state:** label `"Category"`, no trailing icon.
- **Selected state:** label = selected category name, trailing `Close` icon. Tapping the `×` calls `selectCategory(null)`.
- Uses the existing `chipPop` animation modifier.
- Tapping the chip (in either state) opens the `CategoryFilterSheet`.

### 3.2 CategoryFilterSheet

New composable in `LogComponents.kt`. A `ModalBottomSheet` containing:

- A title row: `"Filter by Category"` in `labelLarge`.
- A `LazyColumn` of category rows, sectioned by group name (group name as a `labelMedium` header row).
- Each category row: `CategoryIconChip` (existing component) + category name in `bodyMedium`. Min height follows `BudgetTheme.density.rowMinHeight`.
- Tapping a row calls `viewModel.selectCategory(id)` and dismisses the sheet.
- No search input — skipped (YAGNI; add if list becomes unwieldy for the user).

Sheet open/close state lives in `LogScreen` as `var showCategoryPicker by remember { mutableStateOf(false) }`, same pattern as the existing `showSheet`.

### 3.3 Interaction flow

```
Tap "Category" chip
  → showCategoryPicker = true → sheet opens
  → user taps category → selectCategory(id), showCategoryPicker = false
  → chip updates to selected state, list filters
Tap "×" on chip → selectCategory(null), chip returns to default
```

Kind filter and category filter are independent and stack.

---

## 4. Report Screen — Inline Transaction Expansion

### 4.1 Expanded state

```kotlin
var expandedCategoryId by remember { mutableStateOf<Long?>(null) }
```

Local state in `ReportScreen`. One category expanded at a time. Tapping an already-expanded category collapses it (set to null).

### 4.2 Category row tap target

In `ReportComponents.kt`, the existing category row `Row` receives:

- `clickable { onToggle(row.category.id) }` modifier.
- A trailing `KeyboardArrowDown` / `KeyboardArrowUp` icon (Material outlined) indicating collapsed/expanded state. The icon replaces or sits beside the existing trailing content.

The existing Plan/Actual/Δ column layout is unchanged.

### 4.3 Expanded transaction list

Rendered as a `Column` directly beneath the category row, inside the same `BudgetCard`:

- A `HorizontalDivider` (with horizontal padding) separates the summary row from the transactions.
- Each transaction is a `Row`:
  - **Leading:** date label (`"Jun 5"` — formatted with `DateTimeFormatter.ofPattern("MMM d")` using the transaction's epoch millis + system zone) in `onSurfaceVariant` muted color, `labelMedium`.
  - **Middle:** `description` if non-null/non-blank, else `categoryName`. Single line, `bodyMedium`, fills remaining width with `weight(1f)`.
  - **Trailing:** `Money.formatShort(amount, currency)`, `money` typography, colored `BudgetTheme.semanticColors.income` (INCOME kind) or `onSurfaceVariant` muted (EXPENSE kind) — expense amounts shown neutrally in the Report context since the Actual column already shows the total.
- Rows are sorted descending by date (same as Log).
- Guard: if `transactionsByCategoryId[id]` is empty or null, show a single muted `"No transactions"` `Text` row.
- No tap handler on individual rows — read-only in Report context.

### 4.4 Wiring

`ReportScreen` passes `expandedCategoryId` and `onToggle: (Long) -> Unit` down through `ReportGroupCard` to each category row composable. The relevant `List<TxnRow>` slice (`uiState.transactionsByCategoryId[row.category.id].orEmpty()`) is passed alongside.

---

## 5. Files Changed

| File | Change |
|---|---|
| `ui/screens/log/LogViewModel.kt` | Add `_selectedCategoryId`, `selectCategory()`, update `buildLogState` signature |
| `ui/screens/log/LogComponents.kt` | Add second chip row, `CategoryFilterSheet` composable |
| `ui/screens/log/LogScreen.kt` | Wire `showCategoryPicker`, pass `selectedCategoryId` to chip row |
| `ui/screens/report/ReportViewModel.kt` | Add `transactionsByCategoryId` to `ReportUiState`, compute in combine |
| `ui/screens/report/ReportComponents.kt` | Make category rows tappable, render expanded transaction list |
| `ui/screens/report/ReportScreen.kt` | Add `expandedCategoryId` state, pass `onToggle` + transactions to components |

No new files. No domain layer changes. No DAO changes.

---

## 6. Out of Scope

- Search within the category picker sheet (add if needed).
- Multi-category selection (not requested).
- Tapping a transaction in the Report expansion to edit it (Report is read-only).
- Cross-month category view (always scoped to the current month navigator selection).
