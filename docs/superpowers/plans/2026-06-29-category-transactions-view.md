# Category Transactions View — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users filter the Log by category via a chip+picker and drill into individual transactions per category inline on the Report screen.

**Architecture:** Two independent surface additions — Log gets a `selectedCategoryId` StateFlow that gates `buildLogState`'s section output; Report gets a `transactionsByCategoryId` map in `ReportUiState` (computed in the existing combine block) plus local `expandedCategoryId` UI state that toggles an inline row list. No new navigation, no new files.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, kotlinx.coroutines `combine` (5-flow overload).

## Global Constraints

- minSdk 24, compileSdk 36, JVM target 11; `java.time` is safe (core-library desugaring enabled).
- Money is always `Long` minor units — never `Int`, never `Double`.
- Format money with `Money.format(amount, currency)` or `Money.formatShort(amount, currency)` from `domain/money/Money`.
- All unit tests run JVM-only via `./gradlew :app:testDebugUnitTest`. No emulator needed.
- `buildLogState` is a pure top-level function in `LogViewModel.kt`; keep it there so `LogStateTest` can reach it.
- Commit with Conventional Commits (`feat:`, `test:`, …).
- Do NOT push or open a PR — the human reviews and merges.

---

### Task 1: Add category filter to `buildLogState` + `LogViewModel`

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/log/LogViewModel.kt`
- Extend: `app/src/test/java/com/example/budgettracker/ui/screens/log/LogStateTest.kt`

**Interfaces:**
- Produces: `buildLogState(..., categoryId: Long? = null)` — new optional last param; existing callers unaffected (default null).
- Produces: `LogViewModel.selectedCategoryId: StateFlow<Long?>` and `LogViewModel.liveGroups: StateFlow<List<CategoryGroup>>` and `LogViewModel.selectCategory(id: Long?)`.

- [ ] **Step 1: Write the failing tests** — add to the end of `LogStateTest.kt`:

```kotlin
@Test fun categoryFilterNarrowsSectionsButNotTotals() {
    val txns = listOf(
        txn(1, 1, 500_000, "2026-06-01T10:00:00Z"),
        txn(2, 2, 200_000, "2026-06-02T10:00:00Z"),
    )
    val state = buildLogState(txns, cats, groups, TxnFilter.ALL, utc, categoryId = 1L)
    // NetBand totals reflect full month regardless of category filter
    assertEquals(500_000L, state.income)
    assertEquals(200_000L, state.expense)
    // Sections contain only the selected category's transaction
    assertEquals(1, state.sections.sumOf { it.rows.size })
    assertEquals("C1", state.sections.single().rows.single().categoryName)
}

@Test fun categoryAndKindFiltersStack() {
    val txns = listOf(
        txn(1, 1, 500_000, "2026-06-01T10:00:00Z"),  // INCOME, cat 1
        txn(2, 2, 200_000, "2026-06-02T10:00:00Z"),  // EXPENSE, cat 2
    )
    // categoryId=1 (INCOME cat) + EXPENSE kind filter → zero section rows, totals still full month
    val state = buildLogState(txns, cats, groups, TxnFilter.EXPENSE, utc, categoryId = 1L)
    assertEquals(0, state.sections.sumOf { it.rows.size })
    assertEquals(500_000L, state.income)
    assertEquals(200_000L, state.expense)
}

@Test fun nullCategoryIdShowsAllTransactions() {
    val txns = listOf(
        txn(1, 1, 500_000, "2026-06-01T10:00:00Z"),
        txn(2, 2, 200_000, "2026-06-02T10:00:00Z"),
    )
    val state = buildLogState(txns, cats, groups, TxnFilter.ALL, utc, categoryId = null)
    assertEquals(2, state.sections.sumOf { it.rows.size })
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.ui.screens.log.LogStateTest" 2>&1 | tail -20
```

Expected: compilation error — `buildLogState` does not accept `categoryId` parameter yet.

- [ ] **Step 3: Modify `buildLogState` in `LogViewModel.kt`** — add `categoryId: Long? = null` as the last parameter and apply it before the kind filter:

```kotlin
fun buildLogState(
    transactions: List<TransactionEntity>,
    categoriesById: Map<Long, Category>,
    groupsById: Map<Long, CategoryGroup>,
    filter: TxnFilter,
    zone: ZoneId,
    categoryId: Long? = null,   // <-- new
): LogUiState {
    data class Resolved(val txn: TransactionEntity, val category: Category)
    val resolved = transactions.mapNotNull { t -> categoriesById[t.categoryId]?.let { Resolved(t, it) } }

    val income = resolved.filter { it.category.kind == Kind.INCOME }.sumOf { it.txn.amount }
    val expense = resolved.filter { it.category.kind == Kind.EXPENSE }.sumOf { it.txn.amount }
    val incomeCount = resolved.count { it.category.kind == Kind.INCOME }
    val expenseCount = resolved.count { it.category.kind == Kind.EXPENSE }

    val categoryFiltered = if (categoryId != null) resolved.filter { it.category.id == categoryId } else resolved
    val filtered = when (filter) {
        TxnFilter.ALL -> categoryFiltered
        TxnFilter.INCOME -> categoryFiltered.filter { it.category.kind == Kind.INCOME }
        TxnFilter.EXPENSE -> categoryFiltered.filter { it.category.kind == Kind.EXPENSE }
    }

    val sections = filtered
        .groupBy { Instant.ofEpochMilli(it.txn.date).atZone(zone).toLocalDate() }
        .entries
        .sortedByDescending { it.key }
        .map { (date, items) ->
            val rows = items
                .sortedWith(compareByDescending<Resolved> { it.txn.date }.thenByDescending { it.txn.createdAt })
                .map { r ->
                    val group = groupsById[r.category.groupId]
                    TxnRow(
                        id = r.txn.id,
                        categoryId = r.category.id,
                        categoryName = r.category.name,
                        groupName = group?.name ?: "",
                        leadingColor = r.category.color ?: group?.color ?: "#64748b",
                        iconKey = r.category.icon,
                        kind = r.category.kind,
                        amount = r.txn.amount,
                        note = r.txn.description,
                        date = r.txn.date,
                    )
                }
            val dayNet = rows.sumOf { if (it.kind == Kind.INCOME) it.amount else -it.amount }
            DateSection(date.format(DAY_FORMAT), date.format(WEEKDAY_FORMAT), dayNet, rows)
        }

    return LogUiState(sections, income, expense, income - expense, incomeCount, expenseCount)
}
```

- [ ] **Step 4: Add ViewModel state and update `uiState` combine** — in `LogViewModel` class body, add these members (after the existing `_filter` and `filter` declarations):

```kotlin
private val _selectedCategoryId = MutableStateFlow<Long?>(null)
val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

val liveGroups: StateFlow<List<CategoryGroup>> = categoryRepository.observeGroups()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

fun selectCategory(id: Long?) { _selectedCategoryId.value = id }
```

Then replace the `uiState` combine (4-flow → 5-flow):

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
val uiState: StateFlow<LogUiState> = combine(
    month.filterNotNull().flatMapLatest { transactionRepository.observeMonth(it, zoneId) },
    categoryRepository.observeCategories(includeArchived = true),
    categoryRepository.observeGroups(includeArchived = true),
    _filter,
    _selectedCategoryId,
) { txns, cats, groups, filter, catId ->
    buildLogState(txns, cats.associateBy { it.id }, groups.associateBy { it.id }, filter, zoneId, catId)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogUiState.EMPTY)
```

- [ ] **Step 5: Run tests to confirm they pass**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.ui.screens.log.LogStateTest" 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`, all `LogStateTest` tests pass.

- [ ] **Step 6: Compile-check the full module**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:compileDebugKotlin 2>&1 | grep -E "error:|warning:" | head -20
```

Expected: no errors.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/ui/screens/log/LogViewModel.kt \
        app/src/test/java/com/example/budgettracker/ui/screens/log/LogStateTest.kt
git commit -m "feat(log): add category filter to buildLogState and LogViewModel"
```

---

### Task 2: Log screen UI — category chip row + picker sheet

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/log/LogComponents.kt`
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/log/LogScreen.kt`

**Interfaces:**
- Consumes: `LogViewModel.selectedCategoryId`, `LogViewModel.liveGroups`, `LogViewModel.selectCategory(id)` from Task 1.
- Produces: `CategoryChipRow` and `CategoryFilterSheet` composables in `LogComponents.kt`.

No unit tests — UI-only composables; verify manually on device/emulator.

- [ ] **Step 1: Add `CategoryChipRow` and `CategoryFilterSheet` to `LogComponents.kt`**

Add the following new imports at the top of `LogComponents.kt`:

```kotlin
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.input.pointer.pointerInput
import com.example.budgettracker.data.entity.CategoryGroup
```

Then append these two composables at the end of `LogComponents.kt`:

```kotlin
/** Single chip showing the active category filter. Tap to open picker; tap × to clear. */
@Composable
fun CategoryChipRow(
    selectedCategoryId: Long?,
    categories: List<com.example.budgettracker.data.entity.Category>,
    onChipClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedName = categories.firstOrNull { it.id == selectedCategoryId }?.name
    Row(modifier.padding(horizontal = 16.dp)) {
        FilterChip(
            selected = selectedCategoryId != null,
            onClick = onChipClick,
            label = { Text(selectedName ?: "Category") },
            trailingIcon = if (selectedCategoryId != null) {
                {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear category filter",
                        modifier = Modifier
                            .size(16.dp)
                            // detectTapGestures consumes the pointer event so the chip's
                            // onClick doesn't also fire when the × is tapped.
                            .pointerInput(onClear) { detectTapGestures { onClear() } },
                    )
                }
            } else null,
            modifier = Modifier.chipPop(selectedCategoryId != null),
        )
    }
}

/** Bottom sheet listing all live categories grouped under their group headers. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterSheet(
    categories: List<com.example.budgettracker.data.entity.Category>,
    groups: List<CategoryGroup>,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val groupsById = remember(groups) { groups.associateBy { it.id } }
    val grouped = remember(categories, groups) {
        categories
            .groupBy { it.groupId }
            .entries
            .sortedBy { (gid, _) -> groupsById[gid]?.order ?: Int.MAX_VALUE }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "Filter by Category",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
            grouped.forEach { (groupId, cats) ->
                val groupName = groupsById[groupId]?.name ?: return@forEach
                item(key = "group-$groupId") {
                    Text(
                        groupName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(cats, key = { it.id }) { cat ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(cat.id); onDismiss() }
                            .padding(horizontal = 16.dp)
                            .heightIn(min = BudgetTheme.density.rowMinHeight),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CategoryIconChip(
                            cat.icon,
                            cat.color?.let { parseHexColor(it) }
                                ?: parseHexColor(groupsById[cat.groupId]?.color ?: "#64748b"),
                            size = 26.dp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(cat.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Wire into `LogScreen.kt`**

**2a.** Add these new `val` declarations in `LogScreen`, after the existing `val categories` line:

```kotlin
val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
val groups by viewModel.liveGroups.collectAsStateWithLifecycle()
var showCategoryPicker by remember { mutableStateOf(false) }
```

**2b.** Add the chip row after `LogFilterChips` (after the `Spacer(Modifier.height(8.dp))` that follows `LogFilterChips`). Replace that spacer block:

```kotlin
LogFilterChips(filter, uiState.incomeCount, uiState.expenseCount, viewModel::setFilter)
Spacer(Modifier.height(4.dp))
CategoryChipRow(
    selectedCategoryId = selectedCategoryId,
    categories = categories,
    onChipClick = { showCategoryPicker = true },
    onClear = { viewModel.selectCategory(null) },
)
Spacer(Modifier.height(8.dp))
```

**2c.** Add the sheet at the bottom of `LogScreen`, alongside the existing `TransactionSheet` block:

```kotlin
if (showCategoryPicker) {
    CategoryFilterSheet(
        categories = categories,
        groups = groups,
        onSelect = { viewModel.selectCategory(it) },
        onDismiss = { showCategoryPicker = false },
    )
}
```

**2d.** Add missing imports to `LogScreen.kt`:

```kotlin
import androidx.compose.runtime.setValue
import com.example.budgettracker.ui.screens.log.CategoryChipRow
import com.example.budgettracker.ui.screens.log.CategoryFilterSheet
```

(`getValue` and `mutableStateOf` should already be imported; add them if not.)

- [ ] **Step 3: Compile-check**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:compileDebugKotlin 2>&1 | grep -E "error:" | head -20
```

Expected: no errors.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/ui/screens/log/LogComponents.kt \
        app/src/main/java/com/example/budgettracker/ui/screens/log/LogScreen.kt
git commit -m "feat(log): add category filter chip and picker sheet"
```

---

### Task 3: Add `buildCategoryTxnMap` and extend `ReportUiState`

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/report/ReportViewModel.kt`
- Create: `app/src/test/java/com/example/budgettracker/ui/screens/report/CategoryTxnMapTest.kt`

**Interfaces:**
- Consumes: `TxnRow` from `com.example.budgettracker.ui.screens.log`.
- Produces: top-level pure function `buildCategoryTxnMap(transactions, categoriesById, groupsById): Map<Long, List<TxnRow>>`.
- Produces: `ReportUiState.transactionsByCategoryId: Map<Long, List<TxnRow>>`.

- [ ] **Step 1: Write the failing tests** — create `CategoryTxnMapTest.kt`:

```kotlin
package com.example.budgettracker.ui.screens.report

import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.Kind
import com.example.budgettracker.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryTxnMapTest {

    private fun cat(id: Long, color: String? = "#ff0000") = Category(
        id = id, groupId = 10, name = "Cat$id", kind = Kind.EXPENSE,
        color = color, order = 0, createdAt = 0, updatedAt = 0,
    )
    private fun group(color: String = "#0000ff") = CategoryGroup(
        id = 10, name = "Bills", color = color, order = 0, createdAt = 0, updatedAt = 0,
    )
    private fun txn(id: Long, categoryId: Long, date: Long, description: String? = "note") =
        TransactionEntity(id = id, categoryId = categoryId, amount = 100_000, description = description, date = date, createdAt = 0, updatedAt = 0)

    @Test fun groupsTransactionsByCategoryId() {
        val result = buildCategoryTxnMap(
            listOf(txn(1, 1, 1000), txn(2, 1, 2000), txn(3, 2, 3000)),
            mapOf(1L to cat(1), 2L to cat(2)),
            mapOf(10L to group()),
        )
        assertEquals(2, result.size)
        assertEquals(2, result[1L]!!.size)
        assertEquals(1, result[2L]!!.size)
    }

    @Test fun rowsWithinCategoryAreSortedDescendingByDate() {
        val result = buildCategoryTxnMap(
            listOf(txn(1, 1, 1000L), txn(2, 1, 3000L), txn(3, 1, 2000L)),
            mapOf(1L to cat(1)),
            mapOf(10L to group()),
        )
        val dates = result[1L]!!.map { it.date }
        assertEquals(listOf(3000L, 2000L, 1000L), dates)
    }

    @Test fun skipsTransactionsWithUnknownCategory() {
        val result = buildCategoryTxnMap(listOf(txn(1, 99, 1000L)), emptyMap(), emptyMap())
        assertTrue(result.isEmpty())
    }

    @Test fun usesGroupColorFallbackWhenCategoryColorIsNull() {
        val result = buildCategoryTxnMap(
            listOf(txn(1, 1, 1000L)),
            mapOf(1L to cat(1, color = null)),
            mapOf(10L to group(color = "#abcdef")),
        )
        assertEquals("#abcdef", result[1L]!!.single().leadingColor)
    }

    @Test fun usesDefaultColorWhenBothCategoryAndGroupColorAreNull() {
        val catNoColor = Category(id = 1, groupId = 10, name = "Cat1", kind = Kind.EXPENSE, color = null, order = 0, createdAt = 0, updatedAt = 0)
        val groupNoColor = CategoryGroup(id = 10, name = "Bills", color = null, order = 0, createdAt = 0, updatedAt = 0)
        val result = buildCategoryTxnMap(listOf(txn(1, 1, 1000L)), mapOf(1L to catNoColor), mapOf(10L to groupNoColor))
        assertEquals("#64748b", result[1L]!!.single().leadingColor)
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.ui.screens.report.CategoryTxnMapTest" 2>&1 | tail -20
```

Expected: compilation error — `buildCategoryTxnMap` is not defined yet.

- [ ] **Step 3: Add `buildCategoryTxnMap` pure function to `ReportViewModel.kt`**

Add this import at the top of `ReportViewModel.kt`:

```kotlin
import com.example.budgettracker.ui.screens.log.TxnRow
```

Add the function above the `ReportUiState` data class:

```kotlin
fun buildCategoryTxnMap(
    transactions: List<TransactionEntity>,
    categoriesById: Map<Long, Category>,
    groupsById: Map<Long, CategoryGroup>,
): Map<Long, List<TxnRow>> =
    transactions
        .mapNotNull { txn ->
            val cat = categoriesById[txn.categoryId] ?: return@mapNotNull null
            val group = groupsById[cat.groupId]
            TxnRow(
                id = txn.id,
                categoryId = cat.id,
                categoryName = cat.name,
                groupName = group?.name ?: "",
                leadingColor = cat.color ?: group?.color ?: "#64748b",
                iconKey = cat.icon,
                kind = cat.kind,
                amount = txn.amount,
                note = txn.description,
                date = txn.date,
            )
        }
        .groupBy { it.categoryId }
        .mapValues { (_, rows) -> rows.sortedByDescending { it.date } }
```

- [ ] **Step 4: Add `transactionsByCategoryId` to `ReportUiState`** — replace the existing `ReportUiState` data class:

```kotlin
data class ReportUiState(
    val data: ReportData,
    val narrative: String,
    val recurringDueCount: Int,
    val currency: String,
    val loaded: Boolean,
    val transactionsByCategoryId: Map<Long, List<TxnRow>> = emptyMap(),
) {
    companion object {
        val EMPTY = ReportUiState(
            data = ReportData(emptyList(), ReportTotals(0, 0), ReportTotals(0, 0)),
            narrative = "",
            recurringDueCount = 0,
            currency = "INR",
            loaded = false,
        )
    }
}
```

- [ ] **Step 5: Update the `uiState` combine block in `ReportViewModel`** — replace the lambda body:

```kotlin
} { scoped, categories, groups, recurring, currency ->
    val data = aggregateReport(scoped.transactions, scoped.targets, categories, groups)
    val narrative = generateNarrative(scoped.month, data, currency, scoped.transactions.isNotEmpty())
    val dueCount = recurring.count { it.active && it.lastRunMonth != scoped.month }
    val catsById = categories.associateBy { it.id }
    val groupsById = groups.associateBy { it.id }
    ReportUiState(
        data = data,
        narrative = narrative,
        recurringDueCount = dueCount,
        currency = currency,
        loaded = true,
        transactionsByCategoryId = buildCategoryTxnMap(scoped.transactions, catsById, groupsById),
    )
}
```

- [ ] **Step 6: Run tests to confirm they pass**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.ui.screens.report.CategoryTxnMapTest" 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Run the full test suite**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:testDebugUnitTest 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`, all existing tests still pass.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/ui/screens/report/ReportViewModel.kt \
        app/src/test/java/com/example/budgettracker/ui/screens/report/CategoryTxnMapTest.kt
git commit -m "feat(report): add buildCategoryTxnMap and transactionsByCategoryId to ReportUiState"
```

---

### Task 4: Report screen UI — inline category transaction expansion

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/report/ReportComponents.kt`
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/report/ReportScreen.kt`

**Interfaces:**
- Consumes: `ReportUiState.transactionsByCategoryId` from Task 3.
- Consumes: `TxnRow` from `com.example.budgettracker.ui.screens.log`.

No unit tests — UI composable changes; verify manually on device/emulator.

- [ ] **Step 1: Update `ReportGroupCard` signature in `ReportComponents.kt`**

Add these imports at the top of `ReportComponents.kt`:

```kotlin
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import com.example.budgettracker.ui.screens.log.TxnRow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
```

Add a file-level formatter constant (below the existing `DELTA_COL` constant):

```kotlin
private val EXPANDED_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
```

Replace the `ReportGroupCard` function signature and the `report.rows.forEach` call:

```kotlin
@Composable
fun ReportGroupCard(
    report: GroupReport,
    currency: String,
    transactionsByCategoryId: Map<Long, List<TxnRow>>,
    expandedCategoryId: Long?,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BudgetCard(modifier) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    ColorDot(parseHexColor(report.group.color))
                    Spacer(Modifier.width(12.dp))
                    Text(report.group.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(8.dp))
                    KindChip(report.kind)
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.width(DELTA_COL), contentAlignment = Alignment.CenterEnd) {
                    DeltaPill(report.actualSubtotal - report.targetSubtotal, report.kind, currency)
                }
            }
            report.rows.forEach { row ->
                ReportRow(
                    row = row,
                    currency = currency,
                    groupColor = report.group.color,
                    transactions = transactionsByCategoryId[row.category.id].orEmpty(),
                    isExpanded = expandedCategoryId == row.category.id,
                    onToggle = { onToggle(row.category.id) },
                )
            }
        }
    }
}
```

- [ ] **Step 2: Replace `ReportRow` to support expansion**

Replace the existing private `ReportRow` function entirely:

```kotlin
@Composable
private fun ReportRow(
    row: CategoryReportRow,
    currency: String,
    groupColor: String,
    transactions: List<TxnRow>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    val density = BudgetTheme.density
    val expandable = transactions.isNotEmpty()
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (expandable) Modifier.clickable(onClick = onToggle) else Modifier)
                .heightIn(min = density.rowMinHeight)
                .padding(horizontal = 16.dp, vertical = density.rowPaddingVertical),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIconChip(
                row.category.icon,
                row.category.color?.let { parseHexColor(it) } ?: parseHexColor(groupColor),
                size = 26.dp,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                row.category.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(6.dp))
            Row(Modifier.width(PLAN_COL), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.TrackChanges,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    Money.format(row.target, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                Money.format(row.actual, currency),
                modifier = Modifier.width(ACTUAL_COL),
                style = MaterialTheme.typography.money,
                textAlign = TextAlign.Start,
            )
            Box(Modifier.width(DELTA_COL), contentAlignment = Alignment.CenterEnd) {
                if (expandable) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse transactions" else "Expand transactions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    DeltaPill(row.delta, row.category.kind, currency)
                }
            }
        }
        if (isExpanded && expandable) {
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            transactions.forEach { txn -> TxnExpandedRow(txn, currency) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TxnExpandedRow(txn: TxnRow, currency: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            Instant.ofEpochMilli(txn.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(EXPANDED_DATE_FORMAT),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(44.dp),
        )
        Text(
            txn.note?.takeIf { it.isNotBlank() } ?: txn.categoryName,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            Money.formatShort(txn.amount, currency),
            style = MaterialTheme.typography.money,
            color = if (txn.kind == Kind.INCOME)
                BudgetTheme.semanticColors.income
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

- [ ] **Step 3: Wire `expandedCategoryId` in `ReportScreen.kt`**

Add these imports to `ReportScreen.kt` if not already present:

```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
```

Add this local state declaration inside `ReportScreen`, before the `LazyColumn`:

```kotlin
var expandedCategoryId by remember { mutableStateOf<Long?>(null) }
```

Replace the existing `itemsIndexed` call (currently line 85–87):

```kotlin
itemsIndexed(state.data.groups, key = { _, it -> it.group.id }) { index, group ->
    ReportGroupCard(
        report = group,
        currency = state.currency,
        transactionsByCategoryId = state.transactionsByCategoryId,
        expandedCategoryId = expandedCategoryId,
        onToggle = { id -> expandedCategoryId = if (expandedCategoryId == id) null else id },
        modifier = Modifier.cardEntrance(index, month),
    )
}
```

- [ ] **Step 4: Compile-check**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:compileDebugKotlin 2>&1 | grep -E "error:" | head -20
```

Expected: no errors.

- [ ] **Step 5: Run full test suite**

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:testDebugUnitTest 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/ui/screens/report/ReportComponents.kt \
        app/src/main/java/com/example/budgettracker/ui/screens/report/ReportScreen.kt
git commit -m "feat(report): inline transaction expansion per category row"
```

---

## Self-Review

**Spec coverage:**
- §3 Log: category filter chip + picker sheet → Task 1 (ViewModel) + Task 2 (UI). ✓
- §3 NetBand totals unaffected by category filter → Task 1, `buildLogState` income/expense computed from `resolved` before category filter. ✓
- §3 Kind and category filters stack → Task 1 `categoryFiltered` then `when(filter)`. ✓
- §4 Report inline expansion → Task 3 (ViewModel data) + Task 4 (UI). ✓
- §4 One category expanded at a time → Task 4, `expandedCategoryId` is a single `Long?`. ✓
- §4 Chevron in delta column; DeltaPill for non-expandable rows → Task 4 `ReportRow`. ✓
- §4 Expanded rows: date label + description/categoryName + amount colored by kind → Task 4 `TxnExpandedRow`. ✓
- §5 Files changed — 6 files, no new files. ✓

**Placeholder scan:** No TBD, no TODO, no "similar to Task N" references. Every step has actual code. ✓

**Type consistency:**
- `buildLogState(..., categoryId: Long? = null)` — defined Task 1 Step 3, called in Task 1 Step 4 combine. ✓
- `LogViewModel.selectCategory(id: Long?)` — defined Task 1 Step 4, called in Task 2. ✓
- `LogViewModel.liveGroups` — defined Task 1 Step 4, consumed in Task 2 Step 2. ✓
- `buildCategoryTxnMap(transactions, categoriesById, groupsById)` — defined Task 3 Step 3, tested Task 3 Step 1, called in Task 3 Step 5. ✓
- `ReportUiState.transactionsByCategoryId: Map<Long, List<TxnRow>>` — defined Task 3 Step 4, consumed in Task 4 Step 3. ✓
- `ReportGroupCard(report, currency, transactionsByCategoryId, expandedCategoryId, onToggle, modifier)` — defined Task 4 Step 1, called Task 4 Step 3. ✓
