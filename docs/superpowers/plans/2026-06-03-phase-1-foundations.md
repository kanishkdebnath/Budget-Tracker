# Phase 1 — Foundations: Money & Time Core — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the build support `java.time` on `minSdk 24`, and implement + exhaustively unit-test the two trickiest pure-logic utilities — currency formatting and local-timezone month math.

**Architecture:** Pure Kotlin objects under `com.example.budgettracker.domain`, with zero Android-framework dependencies so they run as fast JVM unit tests in `src/test` (no emulator). `Money` formats `Long` minor units per `PRODUCT_SPEC §9`; `MonthUtils` derives months in the device-local timezone per `§7.1`. Grouping (Indian lakh/crore vs Western thousands) is implemented manually for deterministic, locale-independent results.

**Tech Stack:** Kotlin 2.2.10, JUnit 4 (already in the version catalog), `java.time`, Android core-library desugaring.

---

## File Structure

| File | Responsibility |
|---|---|
| `gradle/libs.versions.toml` | add `desugar_jdk_libs` version + library entry |
| `app/build.gradle.kts` | enable core-library desugaring; add the desugar dependency |
| `app/src/main/java/com/example/budgettracker/domain/money/Money.kt` | `Money.format(minor, currency)` — symbol, decimals, grouping (§9) |
| `app/src/test/java/com/example/budgettracker/domain/money/MoneyTest.kt` | money formatting tests |
| `app/src/main/java/com/example/budgettracker/domain/time/MonthUtils.kt` | `monthOf`, `monthRange`, `monthLabel` (§7.1) |
| `app/src/test/java/com/example/budgettracker/domain/time/MonthUtilsTest.kt` | month math tests |

> **Spec correction pinned here:** `PRODUCT_SPEC §9` lists the example `1000000,INR → ₹10,00,000`. That is inconsistent with the same section's rule `major = minor / 100` (which makes `1000000` minor = `10000.00` major = **₹10,000**). The internally-consistent value `₹10,00,000` requires `100000000` minor units. This plan implements `major = minor / 100` and tests **both** `1000000 → ₹10,000` and `100000000 → ₹10,00,000`, treating the spec's third example as a typo in the minor amount.

---

## Task 1: Enable core-library desugaring

`MonthUtils` (Task 3) uses `java.time`, which is API 26+. With `minSdk 24` the app would crash on API 24–25 and fail lint without desugaring. Enable it before introducing `java.time` into the main source set.

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts:31-34` (the `compileOptions` block) and the `dependencies` block

- [ ] **Step 1: Add the desugar library to the version catalog**

In `gradle/libs.versions.toml`, under `[versions]` add:

```toml
desugar = "2.1.5"
```

Under `[libraries]` add:

```toml
desugar-jdk-libs = { group = "com.android.tools", name = "desugar_jdk_libs", version.ref = "desugar" }
```

- [ ] **Step 2: Enable desugaring in the app module**

In `app/build.gradle.kts`, replace the existing `compileOptions` block:

```kotlin
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
```

with:

```kotlin
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
```

- [ ] **Step 3: Add the desugar dependency**

In `app/build.gradle.kts`, inside the `dependencies { ... }` block, add this line (alongside the other `implementation(...)` lines):

```kotlin
    coreLibraryDesugaring(libs.desugar.jdk.libs)
```

- [ ] **Step 4: Verify the build still compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.
If dependency resolution fails on the `desugar = "2.1.5"` version, bump it to the latest stable `com.android.tools:desugar_jdk_libs` and re-run.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build: enable core-library desugaring for java.time on minSdk 24"
```

---

## Task 2: Money formatting

Implements `PRODUCT_SPEC §9`. Storage is always `Long` minor units (1/100 of the major unit) regardless of currency. Display: pick symbol; group the integer part (Indian for INR, Western otherwise); show 2 decimals only when there's a non-zero fractional part; JPY shows 0 decimals; unknown ISO codes use a `"{CODE} "` prefix.

**Files:**
- Create: `app/src/main/java/com/example/budgettracker/domain/money/Money.kt`
- Test: `app/src/test/java/com/example/budgettracker/domain/money/MoneyTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/example/budgettracker/domain/money/MoneyTest.kt`:

```kotlin
package com.example.budgettracker.domain.money

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test fun wholeAmountOmitsDecimals() {
        assertEquals("₹500", Money.format(50000, "INR"))
    }

    @Test fun fractionalAmountShowsTwoDecimals() {
        assertEquals("₹500.50", Money.format(50050, "INR"))
    }

    @Test fun fractionalAmountPadsSingleDigitCents() {
        assertEquals("₹500.05", Money.format(50005, "INR"))
    }

    @Test fun inrUsesIndianLakhCroreGrouping() {
        // 100000000 minor = 1,000,000.00 major = ten lakh
        assertEquals("₹10,00,000", Money.format(100000000, "INR"))
    }

    @Test fun inrGroupingForTenThousandMajor() {
        // 1000000 minor = 10,000.00 major (spec §9 third example has a typo in the minor amount)
        assertEquals("₹10,000", Money.format(1000000, "INR"))
    }

    @Test fun usdUsesWesternThousandsGrouping() {
        assertEquals("$1,000,000", Money.format(100000000, "USD"))
    }

    @Test fun jpyHasNoDecimals() {
        assertEquals("¥1,000", Money.format(100000, "JPY"))
    }

    @Test fun jpyTruncatesFractionalMinorUnits() {
        assertEquals("¥1,000", Money.format(100050, "JPY"))
    }

    @Test fun knownSymbolsRenderCorrectly() {
        assertEquals("€500", Money.format(50000, "EUR"))
        assertEquals("£500", Money.format(50000, "GBP"))
        assertEquals("A$500", Money.format(50000, "AUD"))
        assertEquals("C$500", Money.format(50000, "CAD"))
        assertEquals("S$500", Money.format(50000, "SGD"))
    }

    @Test fun unknownCurrencyUsesCodePrefix() {
        assertEquals("XYZ 500", Money.format(50000, "XYZ"))
    }

    @Test fun zeroFormatsCleanly() {
        assertEquals("₹0", Money.format(0, "INR"))
    }

    @Test fun negativeAmountPrefixesMinusBeforeSymbol() {
        assertEquals("-₹500.50", Money.format(-50050, "INR"))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.example.budgettracker.domain.money.MoneyTest"`
Expected: compilation failure / `unresolved reference: Money`.

- [ ] **Step 3: Write the implementation**

Create `app/src/main/java/com/example/budgettracker/domain/money/Money.kt`:

```kotlin
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
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.example.budgettracker.domain.money.MoneyTest"`
Expected: `BUILD SUCCESSFUL`, all 13 test methods pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/domain/money/Money.kt app/src/test/java/com/example/budgettracker/domain/money/MoneyTest.kt
git commit -m "feat: add Money.format for Long minor units (PRODUCT_SPEC §9)"
```

---

## Task 3: Month utilities (local timezone)

Implements `PRODUCT_SPEC §7.1`. Months are derived in the device-local timezone (a deliberate divergence from pathforge's UTC anchoring, §12 #3) so a late-night local entry lands in the user's local month. `monthRange` returns a half-open `[start, endExclusive)` instant pair for querying transactions.

**Files:**
- Create: `app/src/main/java/com/example/budgettracker/domain/time/MonthUtils.kt`
- Test: `app/src/test/java/com/example/budgettracker/domain/time/MonthUtilsTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/example/budgettracker/domain/time/MonthUtilsTest.kt`:

```kotlin
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
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew test --tests "com.example.budgettracker.domain.time.MonthUtilsTest"`
Expected: compilation failure / `unresolved reference: MonthUtils`.

- [ ] **Step 3: Write the implementation**

Create `app/src/main/java/com/example/budgettracker/domain/time/MonthUtils.kt`:

```kotlin
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
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew test --tests "com.example.budgettracker.domain.time.MonthUtilsTest"`
Expected: `BUILD SUCCESSFUL`, all 5 test methods pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/domain/time/MonthUtils.kt app/src/test/java/com/example/budgettracker/domain/time/MonthUtilsTest.kt
git commit -m "feat: add MonthUtils for local-timezone month math (PRODUCT_SPEC §7.1)"
```

---

## Task 4: Phase verification

- [ ] **Step 1: Run the full unit-test suite**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL`; `MoneyTest`, `MonthUtilsTest`, and the existing `ExampleUnitTest` all pass.

- [ ] **Step 2: Verify the app still compiles with desugaring**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Confirm clean working tree**

Run: `git status`
Expected: nothing uncommitted (everything landed in the per-task commits).

---

## Phase exit criteria

- `./gradlew test` is green, including the new `MoneyTest` (13 cases) and `MonthUtilsTest` (5 cases).
- The app compiles with core-library desugaring enabled, so `java.time` is safe on `minSdk 24`.
- `domain/money` and `domain/time` exist as pure, Android-free Kotlin, ready for Phase 2 (theme) and Phase 3 (data layer) to depend on.

---

## Self-Review notes

- **Spec coverage:** Task 2 covers §9 (every documented currency + grouping + decimal rule + the corrected typo case). Task 3 covers §7.1 (`monthOf`, `monthRange`, label) including the local-TZ-vs-UTC divergence from §12 #3. Task 1 resolves the minSdk-24 / `java.time` desugaring requirement flagged in `CLAUDE.md`.
- **Placeholders:** none — every code and test step is complete.
- **Type consistency:** `Money.format(Long, String): String` and `MonthUtils.monthOf/monthRange/monthLabel` signatures are used identically in their tests; `MonthRange(startInclusive, endExclusive)` field names match between impl and test.
