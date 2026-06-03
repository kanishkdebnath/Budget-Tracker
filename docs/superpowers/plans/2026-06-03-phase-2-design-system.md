# Phase 2 — Design-System Theme — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Replace the Android Studio template's Purple theme with the approved navy Material 3 design system — color schemes (light + dark), Inter typography with a tabular `money` style, the shape scale, fixed semantic colors, and gradient brushes — wired through `BudgetTrackerTheme` with `dynamicColor` defaulting to **false**.

**Architecture:** All theme code lives in `com.example.budgettracker.ui.theme`. Color/Type/Shape tokens are declarative constants from the design spec §3. Semantic colors (`income`/`overage`/`expense`) are provided via a `CompositionLocal` so they stay fixed signal even when dynamic color is on (design §7.3). Inter is bundled as three static weights in `res/font/` (works on all APIs incl. minSdk 24).

**Tech Stack:** Jetpack Compose, Material 3, Inter (bundled TTF). No new dependencies.

**Verification reality:** No emulator is attached, so this phase is verified by `:app:compileDebugKotlin` + `:app:assembleDebug` (proves the bundled font + theme compile and package) and a `@Preview` gallery for manual inspection in Android Studio. The 18 existing unit tests must stay green. Theme tokens are declarative, so no new unit tests are added (nothing with branching logic to assert).

---

## File Structure

| File | Responsibility |
|---|---|
| `app/src/main/res/font/inter_{regular,medium,semibold}.ttf` | Bundled Inter static weights (already downloaded) |
| `app/src/main/java/.../ui/theme/Font.kt` | `InterFamily` `FontFamily` from the bundled weights |
| `app/src/main/java/.../ui/theme/Color.kt` | All color tokens: navy light + dark roles, semantic, seed group colors |
| `app/src/main/java/.../ui/theme/Type.kt` | `AppTypography` (Inter, 8 M3 styles) + `money` style (tabular nums) |
| `app/src/main/java/.../ui/theme/Shape.kt` | `AppShapes` (4/8/16/20/pill) + `RowShape` (12) |
| `app/src/main/java/.../ui/theme/Gradient.kt` | Gradient `Brush` tokens (design §3.6) |
| `app/src/main/java/.../ui/theme/Theme.kt` | `LightColors`/`DarkColors`, `BudgetSemanticColors` + `CompositionLocal`, `BudgetTrackerTheme`, `BudgetTheme` accessor |
| `app/src/main/java/.../ui/theme/ThemeGallery.kt` | `@Preview` swatch/type/shape gallery for manual review |

`MainActivity.kt` is left as-is (still the template `Greeting`) — it already wraps content in `BudgetTrackerTheme`, so it picks up the navy theme automatically. Real screens arrive in Phase 4+.

---

## Task 1: Bundle the Inter font family

The three weights are already in `res/font/`. This task wires them into a `FontFamily`.

**Files:**
- Create: `app/src/main/java/com/example/budgettracker/ui/theme/Font.kt`

- [ ] **Step 1: Create the font family**

```kotlin
package com.example.budgettracker.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.budgettracker.R

/** Inter, bundled as static weights (Regular 400, Medium 500, SemiBold 600). */
val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)
```

- [ ] **Step 2: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL` (resolves `R.font.inter_*`).

---

## Task 2: Color tokens

Implements design spec §3.1. M3 roles the spec doesn't name (e.g. `onSecondary`, `outlineVariant`, inverse roles) are filled with sensible derived values so nothing falls back to the M3 purple baseline.

**Files:**
- Create/replace: `app/src/main/java/com/example/budgettracker/ui/theme/Color.kt`

- [ ] **Step 1: Replace Color.kt**

```kotlin
package com.example.budgettracker.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Light theme roles (design §3.1) ----
val LightPrimary = Color(0xFF0D2736)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFBEDCEC)
val LightOnPrimaryContainer = Color(0xFF001E2C)
val LightSecondary = Color(0xFF4B5C66)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFCDDCE6)
val LightOnSecondaryContainer = Color(0xFF06151E)
val LightTertiary = Color(0xFFB07B3A)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFFDDB6)
val LightOnTertiaryContainer = Color(0xFF2A1700)
val LightError = Color(0xFFB3261E)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD4)
val LightOnErrorContainer = Color(0xFF410001)
val LightBackground = Color(0xFFF4F8FA)
val LightOnBackground = Color(0xFF0E141A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0E141A)
val LightSurfaceVariant = Color(0xFFE6ECEE)
val LightOnSurfaceVariant = Color(0xFF404951)
val LightOutline = Color(0xFF6F7E87)
val LightOutlineVariant = Color(0xFFC2CDD3)
val LightInverseSurface = Color(0xFF2B3238)
val LightInverseOnSurface = Color(0xFFEFF4F7)
val LightInversePrimary = Color(0xFF9CC8DE)
val LightScrim = Color(0xFF000000)

// ---- Dark theme roles (design §3.1, first-class) ----
val DarkPrimary = Color(0xFF9CC8DE)
val DarkOnPrimary = Color(0xFF003547)
val DarkPrimaryContainer = Color(0xFF143548)
val DarkOnPrimaryContainer = Color(0xFFC5E5F9)
val DarkSecondary = Color(0xFFB5C5CF)
val DarkOnSecondary = Color(0xFF20303A)
val DarkSecondaryContainer = Color(0xFF36464F)
val DarkOnSecondaryContainer = Color(0xFFD1E1EB)
val DarkTertiary = Color(0xFFFFB68D)
val DarkOnTertiary = Color(0xFF4A2700)
val DarkTertiaryContainer = Color(0xFF6A3B16)
val DarkOnTertiaryContainer = Color(0xFFFFDDB6)
val DarkError = Color(0xFFFFB4A8)
val DarkOnError = Color(0xFF690003)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD4)
val DarkBackground = Color(0xFF0A1218)
val DarkOnBackground = Color(0xFFE0E7EC)
val DarkSurface = Color(0xFF11191F)
val DarkOnSurface = Color(0xFFE0E7EC)
val DarkSurfaceVariant = Color(0xFF1A2229) // design "surface-2"
val DarkOnSurfaceVariant = Color(0xFFC0C9D0)
val DarkOutline = Color(0xFF7C8A93)
val DarkOutlineVariant = Color(0xFF3F484F)
val DarkInverseSurface = Color(0xFFE0E7EC)
val DarkInverseOnSurface = Color(0xFF2B3238)
val DarkInversePrimary = Color(0xFF0D2736)
val DarkScrim = Color(0xFF000000)

// ---- Semantic signal colors (fixed, never confused with brand chrome; §3.1) ----
val IncomeLight = Color(0xFF1F7A5A)
val IncomeDark = Color(0xFF74D9B5)
val OverageLight = Color(0xFFB3261E)
val OverageDark = Color(0xFFFFB4A8)
// `expense` is intentionally NOT a fixed hex — it uses the theme's onSurface (see Theme.kt).

// ---- Seed group colors (PRODUCT_SPEC §6.7): accent bars / 8dp dots only, never full backgrounds ----
val SeedIncome = Color(0xFF10B981)
val SeedBills = Color(0xFFEF4444)
val SeedHousehold = Color(0xFFF59E0B)
val SeedDebt = Color(0xFFDC2626)
val SeedLeisure = Color(0xFF8B5CF6)
val SeedSavings = Color(0xFF0EA5E9)
val SeedOther = Color(0xFF64748B)
```

- [ ] **Step 2: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. (Old `Purple*`/`Pink*` vals are gone; Task 5 updates Theme.kt which still references them, so a transient failure here is fine until Task 5 — run this check after Task 5 if it fails now.)

---

## Task 3: Typography

Implements design §3.2. Eight M3 styles in Inter + a `money` style with tabular numerals (`fontFeatureSettings = "tnum"`), exposed as an extension property so call sites use `MaterialTheme.typography.money`.

**Files:**
- Create/replace: `app/src/main/java/com/example/budgettracker/ui/theme/Type.kt`

- [ ] **Step 1: Replace Type.kt**

```kotlin
package com.example.budgettracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Material 3 type scale in Inter (design §3.2). */
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.022).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.012).em,
    ),
    titleLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.em,
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.em,
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.em,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.em,
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.em,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.04.em,
    ),
)

/**
 * Money style (design §3.2): 18sp, medium, tabular numerals.
 * MANDATORY anywhere a monetary amount is rendered. Access via MaterialTheme.typography.money.
 */
val MoneyTextStyle = TextStyle(
    fontFamily = InterFamily, fontWeight = FontWeight.Medium,
    fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.01).em,
    fontFeatureSettings = "tnum",
)

val Typography.money: TextStyle get() = MoneyTextStyle
```

- [ ] **Step 2: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL` (after Task 5 swaps Theme.kt to `AppTypography`).

---

## Task 4: Shapes

Implements design §3.3 / §7.1.

**Files:**
- Create: `app/src/main/java/com/example/budgettracker/ui/theme/Shape.kt`

- [ ] **Step 1: Create Shape.kt**

```kotlin
package com.example.budgettracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Shape scale (design §3.3): extraLarge is the signature pill. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // chip-tight
    small = RoundedCornerShape(8.dp),        // input
    medium = RoundedCornerShape(16.dp),      // card (signature)
    large = RoundedCornerShape(20.dp),       // sheet
    extraLarge = RoundedCornerShape(percent = 50), // pill / FAB
)

/** 12dp row radius — not an M3 slot, used directly by list rows. */
val RowShape = RoundedCornerShape(12.dp)
```

- [ ] **Step 2: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

---

## Task 5: Color schemes, semantic colors & theme wiring

Implements design §3.1 + §7.1 + §7.3. Replaces the template `BudgetTrackerTheme`. `dynamicColor` defaults **false** (fixed navy is the brand default). Semantic income/overage stay fixed even under dynamic color; `expense` resolves to the live `onSurface`.

**Files:**
- Create/replace: `app/src/main/java/com/example/budgettracker/ui/theme/Theme.kt`

- [ ] **Step 1: Replace Theme.kt**

```kotlin
package com.example.budgettracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = LightPrimary, onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer, onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary, onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer, onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary, onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer, onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError, onError = LightOnError,
    errorContainer = LightErrorContainer, onErrorContainer = LightOnErrorContainer,
    background = LightBackground, onBackground = LightOnBackground,
    surface = LightSurface, onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant, onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline, outlineVariant = LightOutlineVariant,
    inverseSurface = LightInverseSurface, inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary, scrim = LightScrim,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary, onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer, onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary, onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer, onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary, onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer, onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError, onError = DarkOnError,
    errorContainer = DarkErrorContainer, onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground, onBackground = DarkOnBackground,
    surface = DarkSurface, onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant, onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline, outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkInverseSurface, inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary, scrim = DarkScrim,
)

/** Fixed semantic signal colors — never replaced by dynamic color (design §7.3). */
@Immutable
data class BudgetSemanticColors(
    val income: Color,
    val expense: Color,
    val overage: Color,
)

val LocalBudgetSemanticColors = staticCompositionLocalOf<BudgetSemanticColors> {
    error("BudgetSemanticColors not provided; wrap content in BudgetTrackerTheme")
}

/** Ergonomic accessor: BudgetTheme.semanticColors.income, etc. */
object BudgetTheme {
    val semanticColors: BudgetSemanticColors
        @Composable @ReadOnlyComposable
        get() = LocalBudgetSemanticColors.current
}

@Composable
fun BudgetTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Fixed navy brand palette is the default; dynamic color is opt-in via Settings (design §2, §7.3).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    // income/overage are fixed signal; expense follows the live onSurface ("uses text color", §3.1).
    val semanticColors = BudgetSemanticColors(
        income = if (darkTheme) IncomeDark else IncomeLight,
        overage = if (darkTheme) OverageDark else OverageLight,
        expense = colorScheme.onSurface,
    )

    CompositionLocalProvider(LocalBudgetSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
```

- [ ] **Step 2: Delete the obsolete template color vals**

The old `Purple40/80`, `PurpleGrey40/80`, `Pink40/80` were defined in the old `Color.kt` (already replaced in Task 2). Confirm none remain:
Run: `grep -rn "Purple\|Pink40\|Pink80" app/src/main/java`
Expected: no matches.

- [ ] **Step 3: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

---

## Task 6: Gradient brushes

Implements design §3.6. These are the dark-mockup hero gradients (the design default is dark). Components in later phases consume them; light-theme variants are derived later if needed.

**Files:**
- Create: `app/src/main/java/com/example/budgettracker/ui/theme/Gradient.kt`

- [ ] **Step 1: Create Gradient.kt**

```kotlin
package com.example.budgettracker.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Gradient tokens (design §3.6). Gradients are used ONLY on surfaces/CTAs, NEVER on data.
 * 135° gradients run top-left -> bottom-right (Offset.Zero -> Offset.Infinite);
 * 180° gradients use the default vertical linearGradient.
 */
object BudgetGradients {

    private val diagStart = Offset.Zero
    private val diagEnd = Offset.Infinite

    /** Net band hero — 135°. */
    val NetBand = Brush.linearGradient(
        colors = listOf(Color(0xFF1B4561), Color(0xFF143548), Color(0xFF0F2A3D)),
        start = diagStart, end = diagEnd,
    )

    /** FAB — 135°. */
    val Fab = Brush.linearGradient(
        colors = listOf(Color(0xFF2A5E80), Color(0xFF1B4561), Color(0xFF143548)),
        start = diagStart, end = diagEnd,
    )

    /** Tonal button / active bottom-nav pill — 180°. */
    val TonalButton = Brush.verticalGradient(listOf(Color(0xFF1B4561), Color(0xFF143548)))

    /** Filled (primary) button — 180°. */
    val FilledButton = Brush.verticalGradient(listOf(Color(0xFFB0DDED), Color(0xFF9CC8DE)))

    /** Card surfaces (txn / group / recurring / narrative / settings) — 180°, ~3% lift at top. */
    val CardSurface = Brush.verticalGradient(listOf(Color(0xFF131A20), Color(0xFF0F1619)))

    /** Info banner — 135°. */
    val BannerInfo = Brush.linearGradient(
        colors = listOf(Color(0xFF1B4561), Color(0xFF143548)),
        start = diagStart, end = diagEnd,
    )

    /** Amber banner — 135°. */
    val BannerAmber = Brush.linearGradient(
        colors = listOf(Color(0xFF7B4520), Color(0xFF5C3013)),
        start = diagStart, end = diagEnd,
    )

    /** Applied-recurring surface — green-tinted, 180°. */
    val AppliedRecurring = Brush.verticalGradient(
        listOf(Color(0x1A74D9B5), Color(0x0A74D9B5)),
    )
}
```

- [ ] **Step 2: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

---

## Task 7: Theme preview gallery

A `@Preview` gallery so the navy theme can be inspected in Android Studio (no emulator needed for previews). Not shipped UI.

**Files:**
- Create: `app/src/main/java/com/example/budgettracker/ui/theme/ThemeGallery.kt`

- [ ] **Step 1: Create ThemeGallery.kt**

```kotlin
package com.example.budgettracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
private fun Swatch(name: String, color: Color, onColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(color, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(name, color = onColor, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ThemeGalleryContent() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val cs = MaterialTheme.colorScheme
            Text("Type scale", style = MaterialTheme.typography.headlineMedium, color = cs.onBackground)
            Text("Net −₹6,800", style = MaterialTheme.typography.money, color = BudgetTheme.semanticColors.income)
            Swatch("primary", cs.primary, cs.onPrimary)
            Swatch("primaryContainer", cs.primaryContainer, cs.onPrimaryContainer)
            Swatch("tertiary", cs.tertiary, cs.onTertiary)
            Swatch("surfaceVariant", cs.surfaceVariant, cs.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(SeedIncome, SeedBills, SeedHousehold, SeedDebt, SeedLeisure, SeedSavings, SeedOther)
                    .forEach { Surface(Modifier.size(20.dp), shape = RoundedCornerShape(4.dp), color = it) {} }
            }
        }
    }
}

@Preview(name = "Theme — Light", showBackground = true)
@Composable
private fun ThemeGalleryLightPreview() {
    BudgetTrackerTheme(darkTheme = false) { ThemeGalleryContent() }
}

@Preview(name = "Theme — Dark", showBackground = true)
@Composable
private fun ThemeGalleryDarkPreview() {
    BudgetTrackerTheme(darkTheme = true) { ThemeGalleryContent() }
}
```

- [ ] **Step 2: Verify compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

---

## Task 8: Phase verification & commit

- [ ] **Step 1: Full debug build (proves font + theme package into an APK)**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Unit tests stay green**

Run: `./gradlew test`
Expected: `BUILD SUCCESSFUL`, 18 tests pass.

- [ ] **Step 3: Confirm no template Purple remains**

Run: `grep -rn "Purple\|Pink40\|Pink80" app/src/main/java`
Expected: no matches.

- [ ] **Step 4: Commit the theme**

```bash
git add app/src/main/res/font app/src/main/java/com/example/budgettracker/ui/theme docs/superpowers/plans/2026-06-03-phase-2-design-system.md
git commit -m "feat: navy Material 3 design-system theme (colors, Inter type, shapes, gradients)"
```

---

## Phase exit criteria

- `:app:assembleDebug` and `./gradlew test` both green; 18 unit tests pass.
- No `Purple`/`Pink` template tokens remain in `main`.
- `BudgetTrackerTheme` provides navy light + dark schemes, Inter typography (incl. tabular `money`), the shape scale, fixed semantic colors, and gradient brushes; `dynamicColor` defaults false.
- `@Preview` gallery renders the palette in both light and dark for design review.

---

## Self-Review notes

- **Spec coverage:** Tasks cover design §3.1 (Color/schemes/semantic/seed), §3.2 (Typography + money + tabular nums), §3.3/§7.1 (Shapes), §3.6 (Gradients), §7.3 (dynamicColor default false; income/overage fixed). The `expense = onSurface` rule (§3.1) is encoded in Theme.kt.
- **Placeholders:** none — every file has complete code.
- **Type consistency:** `InterFamily` (Font.kt) is referenced by Type.kt; `AppTypography`/`AppShapes`/`LightColors`/`DarkColors`/`BudgetSemanticColors`/`LocalBudgetSemanticColors`/`BudgetTheme` names are defined once and used consistently in Theme.kt and ThemeGallery.kt; all `Light*`/`Dark*`/`Seed*` color vals defined in Color.kt are consumed by Theme.kt and the gallery.
- **Deviation from Phase 1 TDD:** theme tokens are declarative (no branching logic), so verification is build + preview rather than failing-test-first. Documented above under "Verification reality."
