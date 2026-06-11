# Category Icons Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give each category an optional, color-tinted vector icon that users pick when creating/editing a category, rendered everywhere a category appears.

**Architecture:** Store a stable string key (`Category.icon`) in Room (DB v1→v2). A `ui/icons` registry maps key → `ImageVector` from the already-present `material-icons-extended`. One shared `CategoryIconChip` composable renders the tinted chip (with color-dot fallback) on every surface. A dedicated `IconPickerSheet` provides search + sectioned grid.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), Room 2.8.4 + KSP, Robolectric (JVM) tests. `material-icons-extended` and `room-testing` are already declared in `gradle/libs.versions.toml` + `app/build.gradle.kts` — **no new dependencies needed**.

**Spec:** `docs/superpowers/specs/2026-06-11-category-icons-design.md`

**Conventions reminders:**
- `JAVA_HOME` must point at the JBR for any Gradle command: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- Run a single test class with the concrete task: `./gradlew :app:testDebugUnitTest --tests "<FQN>"`.
- Conventional Commits; keep `./gradlew test` green per commit. This is one branch / one PR (`feat/category-icons`, already created off `main`).

---

## File Structure

**New files:**
- `app/src/main/java/com/example/budgettracker/data/db/Migrations.kt` — `MIGRATION_1_2`.
- `app/src/main/java/com/example/budgettracker/ui/icons/CategoryIcons.kt` — icon registry + lookup + search.
- `app/src/main/java/com/example/budgettracker/ui/components/CategoryIconChip.kt` — shared render component.
- `app/src/main/java/com/example/budgettracker/ui/screens/categories/IconPickerSheet.kt` — the picker sheet.
- `app/src/test/java/com/example/budgettracker/data/Migration1To2Test.kt`
- `app/src/test/java/com/example/budgettracker/ui/icons/CategoryIconsTest.kt`

**Modified files:**
- `data/entity/Category.kt`, `data/db/BudgetDatabase.kt`, `data/AppContainer.kt`, `data/db/SeedData.kt`, `data/db/DatabaseSeeder.kt`, `data/repository/CategoryRepository.kt`
- `ui/screens/categories/{CategorySheets.kt, CategoryComponents.kt, CategoriesViewModel.kt, CategoriesScreen.kt}`
- `ui/screens/log/{LogViewModel.kt, LogComponents.kt, TransactionSheet.kt}`
- `ui/screens/report/ReportComponents.kt`, `ui/screens/plan/PlanComponents.kt`, `ui/screens/recurring/RecurringSheet.kt`
- `app/build.gradle.kts` (test-assets source set for the migration test)
- `app/schemas/com.example.budgettracker.data.db.BudgetDatabase/2.json` (generated)
- `PRODUCT_SPEC.md`, `CLAUDE.md`

---

## Task 1: DB column + v2 migration

Add the nullable `icon` column, bump the DB to v2 with a backfilling migration, wire it, and lock it in with a Robolectric migration test.

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/data/entity/Category.kt`
- Modify: `app/src/main/java/com/example/budgettracker/data/db/BudgetDatabase.kt:25`
- Create: `app/src/main/java/com/example/budgettracker/data/db/Migrations.kt`
- Modify: `app/src/main/java/com/example/budgettracker/data/AppContainer.kt:24`
- Modify: `app/build.gradle.kts` (test source-set assets)
- Test: `app/src/test/java/com/example/budgettracker/data/Migration1To2Test.kt`
- Generated: `app/schemas/com.example.budgettracker.data.db.BudgetDatabase/2.json`

- [ ] **Step 1: Add the `icon` field to the entity**

In `Category.kt`, add the column after `color`:

```kotlin
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val name: String,
    val kind: Kind,
    val color: String? = null,    // optional #RRGGBB
    val icon: String? = null,     // optional CategoryIcons registry key, e.g. "restaurant"
    val order: Int,               // per-group order
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
```

- [ ] **Step 2: Bump the DB version**

In `BudgetDatabase.kt`, change `version = 1` to `version = 2`.

- [ ] **Step 3: Create the migration**

Create `data/db/Migrations.kt`:

```kotlin
package com.example.budgettracker.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: add the optional `Category.icon` column. Best-effort backfill of the default seed
 * categories by name so existing installs get icons without a data wipe (only touches rows the
 * user hasn't already given an icon).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE category ADD COLUMN icon TEXT")
        val seeds = listOf(
            "Salary" to "payments",
            "Rent" to "home",
            "Electricity" to "bolt",
            "Groceries" to "shopping_cart",
            "Transport" to "directions_car",
            "Dining" to "restaurant",
        )
        for ((name, icon) in seeds) {
            db.execSQL("UPDATE category SET icon = ? WHERE icon IS NULL AND name = ?", arrayOf(icon, name))
        }
    }
}
```

- [ ] **Step 4: Wire the migration into the builder**

In `AppContainer.kt`, update the database builder (line 24) and add the import:

```kotlin
import com.example.budgettracker.data.db.MIGRATION_1_2
```
```kotlin
private val database = Room.databaseBuilder(context, BudgetDatabase::class.java, "budget.db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

- [ ] **Step 5: Expose the exported schema to Robolectric tests**

In `app/build.gradle.kts`, inside the `android { }` block (e.g. right after the `testOptions { }` block), add:

```kotlin
    sourceSets {
        // Make the exported Room schemas readable by MigrationTestHelper under Robolectric.
        getByName("test").assets.srcDirs(files("$projectDir/schemas"))
    }
```

- [ ] **Step 6: Generate the v2 schema**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL, and a new file appears at `app/schemas/com.example.budgettracker.data.db.BudgetDatabase/2.json`. Confirm it exists:

Run: `ls app/schemas/com.example.budgettracker.data.db.BudgetDatabase/`
Expected: `1.json  2.json`

- [ ] **Step 7: Write the migration test**

Create `app/src/test/java/com/example/budgettracker/data/Migration1To2Test.kt`:

```kotlin
package com.example.budgettracker.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.example.budgettracker.data.db.BudgetDatabase
import com.example.budgettracker.data.db.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Migration1To2Test {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BudgetDatabase::class.java,
    )

    @Test
    fun migrate1To2_addsIconColumn_andBackfillsSeedNames() {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO category_group (id, name, color, `order`, archived, createdAt, updatedAt) " +
                    "VALUES (1, 'Bills', '#ef4444', 0, 0, 0, 0)",
            )
            // A seed-named row (should be backfilled) and a custom row (should stay null).
            execSQL(
                "INSERT INTO category (id, groupId, name, kind, color, `order`, archived, createdAt, updatedAt) " +
                    "VALUES (1, 1, 'Rent', 'EXPENSE', NULL, 0, 0, 0, 0)",
            )
            execSQL(
                "INSERT INTO category (id, groupId, name, kind, color, `order`, archived, createdAt, updatedAt) " +
                    "VALUES (2, 1, 'My Custom', 'EXPENSE', NULL, 1, 0, 0, 0)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)

        db.query("SELECT name, icon FROM category ORDER BY id").use { c ->
            assertEquals(2, c.count)
            c.moveToFirst()
            assertEquals("Rent", c.getString(0))
            assertEquals("home", c.getString(1))   // backfilled
            c.moveToNext()
            assertEquals("My Custom", c.getString(0))
            assertNull(c.getString(1))              // untouched
        }
        db.close()
    }
}
```

- [ ] **Step 8: Run the migration test**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.data.Migration1To2Test"`
Expected: PASS. (If `MigrationTestHelper` cannot locate the schema, re-confirm Step 5's `sourceSets` block and that `2.json` exists from Step 6.)

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/data/entity/Category.kt \
        app/src/main/java/com/example/budgettracker/data/db/BudgetDatabase.kt \
        app/src/main/java/com/example/budgettracker/data/db/Migrations.kt \
        app/src/main/java/com/example/budgettracker/data/AppContainer.kt \
        app/build.gradle.kts \
        app/schemas/com.example.budgettracker.data.db.BudgetDatabase/2.json \
        app/src/test/java/com/example/budgettracker/data/Migration1To2Test.kt
git commit -m "feat(data): add Category.icon column with v1→v2 migration"
```

---

## Task 2: Seed icons + repository support

Give fresh installs their seed icons and let the repository persist an icon on create.

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/data/db/SeedData.kt`
- Modify: `app/src/main/java/com/example/budgettracker/data/db/DatabaseSeeder.kt:30-34`
- Modify: `app/src/main/java/com/example/budgettracker/data/repository/CategoryRepository.kt:37-47`
- Test: `app/src/test/java/com/example/budgettracker/data/SeedDataTest.kt` (add a case)
- Test: `app/src/test/java/com/example/budgettracker/data/CategoryRepositoryTest.kt` (add a case)

- [ ] **Step 1: Add icon to seed data**

In `SeedData.kt`, give `SeedCategory` an `icon` and set keys (keys must match Task 1's backfill and exist in Task 3's registry):

```kotlin
data class SeedCategory(val name: String, val kind: Kind, val icon: String? = null)
data class SeedGroup(val name: String, val color: String, val categories: List<SeedCategory>)

val GROUPS: List<SeedGroup> = listOf(
    SeedGroup("Income", "#10b981", listOf(SeedCategory("Salary", Kind.INCOME, "payments"))),
    SeedGroup(
        "Bills", "#ef4444",
        listOf(
            SeedCategory("Rent", Kind.EXPENSE, "home"),
            SeedCategory("Electricity", Kind.EXPENSE, "bolt"),
        ),
    ),
    SeedGroup(
        "Household", "#f59e0b",
        listOf(
            SeedCategory("Groceries", Kind.EXPENSE, "shopping_cart"),
            SeedCategory("Transport", Kind.EXPENSE, "directions_car"),
        ),
    ),
    SeedGroup("Debt", "#dc2626", emptyList()),
    SeedGroup("Leisure", "#8b5cf6", listOf(SeedCategory("Dining", Kind.EXPENSE, "restaurant"))),
    SeedGroup("Savings", "#0ea5e9", emptyList()),
    SeedGroup("Other", "#64748b", emptyList()),
)
```

- [ ] **Step 2: Thread the icon through the seeder**

In `DatabaseSeeder.kt`, the inner `categoryDao.insert(Category(...))` (around line 30) must pass `icon = category.icon`:

```kotlin
categoryDao.insert(
    Category(
        groupId = groupId, name = category.name, kind = category.kind, icon = category.icon,
        order = categoryOrder, createdAt = t, updatedAt = t,
    ),
)
```

- [ ] **Step 3: Add `icon` to the repository create path**

In `CategoryRepository.kt`, update `createCategory` (lines 37-47):

```kotlin
suspend fun createCategory(groupId: Long, name: String, kind: Kind, color: String?, order: Int, icon: String? = null): OpResult {
    val trimmed = name.trim()
    if (categoryDao.findLiveByName(trimmed) != null) {
        return OpResult.Failure("A category named \"$trimmed\" already exists")
    }
    val t = now()
    val id = categoryDao.insert(
        Category(groupId = groupId, name = trimmed, kind = kind, color = color, icon = icon, order = order, createdAt = t, updatedAt = t),
    )
    return OpResult.Success(id)
}
```

> **Important:** `icon` is added as the **last** parameter **with a default** (`icon: String? = null`). This keeps the existing 5-arg caller (`CategoriesViewModel.createCategory` → `repository.createCategory(groupId, name, kind, color, order)`) and any existing 5-arg test calls compiling unchanged, so **the build stays green after this commit**. Task 5 updates the ViewModel to pass the real `icon` as the 6th argument. Do NOT insert `icon` before `order`.

- [ ] **Step 4: Write the seed + repository tests**

In `SeedDataTest.kt`, add:

```kotlin
@Test
fun seedCategoriesCarryIcons() {
    val all = SeedData.GROUPS.flatMap { it.categories }
    assertEquals("restaurant", all.first { it.name == "Dining" }.icon)
    assertEquals("payments", all.first { it.name == "Salary" }.icon)
}
```
(Ensure `import org.junit.Assert.assertEquals` is present.)

In `CategoryRepositoryTest.kt`, add (mirror the existing setup in that file — it already builds an in-memory DB and a `CategoryRepository`; reuse its field names, typically `repository`/`repo` and a seeded/inserted group id):

```kotlin
@Test
fun createCategory_persistsIcon() = runTest {
    val groupId = (repository.createGroup("G", "#10b981", 0) as OpResult.Success).id
    val catId = (repository.createCategory(groupId, "Coffee", Kind.EXPENSE, null, 0, "local_cafe") as OpResult.Success).id
    val saved = db.categoryDao().getById(catId)
    assertEquals("local_cafe", saved?.icon)
}
```
(Add imports as needed: `com.example.budgettracker.data.OpResult`, `com.example.budgettracker.data.entity.Kind`, `org.junit.Assert.assertEquals`. If the existing test class names its repository/db fields differently, match them.)

- [ ] **Step 5: Run the tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.data.SeedDataTest" --tests "com.example.budgettracker.data.CategoryRepositoryTest" --tests "com.example.budgettracker.data.DatabaseSeederTest"`
Expected: PASS (existing seeder count tests still pass — counts are unchanged).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/data/db/SeedData.kt \
        app/src/main/java/com/example/budgettracker/data/db/DatabaseSeeder.kt \
        app/src/main/java/com/example/budgettracker/data/repository/CategoryRepository.kt \
        app/src/test/java/com/example/budgettracker/data/SeedDataTest.kt \
        app/src/test/java/com/example/budgettracker/data/CategoryRepositoryTest.kt
git commit -m "feat(data): seed category icons and persist icon on create"
```

---

## Task 3: Icon registry

The single source of truth mapping stable keys → `ImageVector`, plus lookup and search.

**Files:**
- Create: `app/src/main/java/com/example/budgettracker/ui/icons/CategoryIcons.kt`
- Test: `app/src/test/java/com/example/budgettracker/ui/icons/CategoryIconsTest.kt`

- [ ] **Step 1: Write the failing registry test**

Create `app/src/test/java/com/example/budgettracker/ui/icons/CategoryIconsTest.kt`:

```kotlin
package com.example.budgettracker.ui.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryIconsTest {

    private val all = CATEGORY_ICON_SECTIONS.flatMap { it.icons }

    @Test fun keysAreUnique() {
        val keys = all.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test fun everyKeyResolves() {
        all.forEach { assertNotNull("no vector for ${it.key}", iconVectorForKey(it.key)) }
    }

    @Test fun seedKeysArePresent() {
        listOf("payments", "home", "bolt", "shopping_cart", "directions_car", "restaurant")
            .forEach { assertNotNull("seed key missing: $it", iconVectorForKey(it)) }
    }

    @Test fun unknownAndNullResolveToNull() {
        assertNull(iconVectorForKey(null))
        assertNull(iconVectorForKey("not_a_real_key"))
    }

    @Test fun searchMatchesLabelAndKey() {
        assertTrue(searchIcons("coffee").any { it.key == "local_cafe" })   // label match
        assertTrue(searchIcons("local_cafe").any { it.key == "local_cafe" }) // key match
        assertTrue(searchIcons("").size == all.size)                        // blank → all
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.ui.icons.CategoryIconsTest"`
Expected: FAIL — compilation error (unresolved `CATEGORY_ICON_SECTIONS`, `iconVectorForKey`, `searchIcons`).

- [ ] **Step 3: Write the registry**

Create `app/src/main/java/com/example/budgettracker/ui/icons/CategoryIcons.kt`. Each `key` is unique lowercase snake_case; the `label` drives search; the vector is an `Icons.Outlined.*` (any name that doesn't resolve will fail compilation — fix to the nearest Outlined equivalent). This is the starter set (~60 icons, ~8 sections); extend toward ~100 by adding more `CategoryIcon` rows following the exact same pattern. **Must include all six seed keys.**

```kotlin
package com.example.budgettracker.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** A pickable category icon. [key] is the stable value stored in `Category.icon`. */
data class CategoryIcon(val key: String, val label: String, val vector: ImageVector)

/** A titled section in the icon picker. */
data class IconSection(val title: String, val icons: List<CategoryIcon>)

val CATEGORY_ICON_SECTIONS: List<IconSection> = listOf(
    IconSection(
        "Food & Drink",
        listOf(
            CategoryIcon("restaurant", "Restaurant dining out", Icons.Outlined.Restaurant),
            CategoryIcon("lunch_dining", "Lunch burger fast food", Icons.Outlined.LunchDining),
            CategoryIcon("fastfood", "Fast food snacks", Icons.Outlined.Fastfood),
            CategoryIcon("local_pizza", "Pizza", Icons.Outlined.LocalPizza),
            CategoryIcon("local_cafe", "Coffee cafe tea", Icons.Outlined.LocalCafe),
            CategoryIcon("local_bar", "Bar drinks alcohol", Icons.Outlined.LocalBar),
            CategoryIcon("cake", "Cake dessert sweets", Icons.Outlined.Cake),
        ),
    ),
    IconSection(
        "Home & Bills",
        listOf(
            CategoryIcon("home", "Home house rent", Icons.Outlined.Home),
            CategoryIcon("bolt", "Electricity power energy", Icons.Outlined.Bolt),
            CategoryIcon("water_drop", "Water utilities", Icons.Outlined.WaterDrop),
            CategoryIcon("wifi", "Internet wifi broadband", Icons.Outlined.Wifi),
            CategoryIcon("phone", "Phone mobile", Icons.Outlined.Phone),
            CategoryIcon("receipt_long", "Bills receipt", Icons.Outlined.ReceiptLong),
            CategoryIcon("cleaning_services", "Cleaning maintenance", Icons.Outlined.CleaningServices),
            CategoryIcon("local_laundry_service", "Laundry", Icons.Outlined.LocalLaundryService),
            CategoryIcon("chair", "Furniture home goods", Icons.Outlined.Chair),
        ),
    ),
    IconSection(
        "Transport",
        listOf(
            CategoryIcon("directions_car", "Car driving transport", Icons.Outlined.DirectionsCar),
            CategoryIcon("directions_bus", "Bus public transport", Icons.Outlined.DirectionsBus),
            CategoryIcon("train", "Train metro rail", Icons.Outlined.Train),
            CategoryIcon("directions_bike", "Bike cycling", Icons.Outlined.DirectionsBike),
            CategoryIcon("two_wheeler", "Scooter motorbike", Icons.Outlined.TwoWheeler),
            CategoryIcon("local_taxi", "Taxi cab ride", Icons.Outlined.LocalTaxi),
            CategoryIcon("local_gas_station", "Fuel petrol gas", Icons.Outlined.LocalGasStation),
            CategoryIcon("flight", "Flight travel plane", Icons.Outlined.Flight),
        ),
    ),
    IconSection(
        "Shopping",
        listOf(
            CategoryIcon("shopping_cart", "Groceries shopping cart", Icons.Outlined.ShoppingCart),
            CategoryIcon("shopping_bag", "Shopping bag retail", Icons.Outlined.ShoppingBag),
            CategoryIcon("local_mall", "Mall shopping", Icons.Outlined.LocalMall),
            CategoryIcon("storefront", "Store shop", Icons.Outlined.Storefront),
            CategoryIcon("checkroom", "Clothing apparel", Icons.Outlined.Checkroom),
            CategoryIcon("devices", "Electronics gadgets", Icons.Outlined.Devices),
            CategoryIcon("card_giftcard", "Gifts presents", Icons.Outlined.CardGiftcard),
        ),
    ),
    IconSection(
        "Money",
        listOf(
            CategoryIcon("payments", "Salary income payments", Icons.Outlined.Payments),
            CategoryIcon("attach_money", "Cash money", Icons.Outlined.AttachMoney),
            CategoryIcon("account_balance", "Bank account", Icons.Outlined.AccountBalance),
            CategoryIcon("savings", "Savings piggy bank", Icons.Outlined.Savings),
            CategoryIcon("credit_card", "Credit card debt", Icons.Outlined.CreditCard),
            CategoryIcon("trending_up", "Investment growth", Icons.Outlined.TrendingUp),
            CategoryIcon("currency_exchange", "Exchange transfer", Icons.Outlined.CurrencyExchange),
        ),
    ),
    IconSection(
        "Health",
        listOf(
            CategoryIcon("favorite", "Health wellbeing", Icons.Outlined.Favorite),
            CategoryIcon("local_hospital", "Hospital medical", Icons.Outlined.LocalHospital),
            CategoryIcon("medication", "Medicine pharmacy", Icons.Outlined.Medication),
            CategoryIcon("fitness_center", "Gym fitness workout", Icons.Outlined.FitnessCenter),
            CategoryIcon("spa", "Spa wellness", Icons.Outlined.Spa),
            CategoryIcon("self_improvement", "Meditation yoga", Icons.Outlined.SelfImprovement),
        ),
    ),
    IconSection(
        "Leisure",
        listOf(
            CategoryIcon("movie", "Movies cinema", Icons.Outlined.Movie),
            CategoryIcon("music_note", "Music streaming", Icons.Outlined.MusicNote),
            CategoryIcon("sports_esports", "Games gaming", Icons.Outlined.SportsEsports),
            CategoryIcon("sports_soccer", "Sports football", Icons.Outlined.SportsSoccer),
            CategoryIcon("travel_explore", "Travel explore", Icons.Outlined.TravelExplore),
            CategoryIcon("beach_access", "Vacation holiday beach", Icons.Outlined.BeachAccess),
            CategoryIcon("menu_book", "Books reading", Icons.Outlined.MenuBook),
        ),
    ),
    IconSection(
        "Work & Other",
        listOf(
            CategoryIcon("work", "Work job office", Icons.Outlined.Work),
            CategoryIcon("business_center", "Business briefcase", Icons.Outlined.BusinessCenter),
            CategoryIcon("badge", "ID badge work", Icons.Outlined.Badge),
            CategoryIcon("laptop", "Laptop computer", Icons.Outlined.Laptop),
            CategoryIcon("school", "Education school tuition", Icons.Outlined.School),
            CategoryIcon("volunteer_activism", "Donation charity", Icons.Outlined.VolunteerActivism),
            CategoryIcon("child_care", "Kids childcare", Icons.Outlined.ChildCare),
            CategoryIcon("pets", "Pets animals", Icons.Outlined.Pets),
            CategoryIcon("star", "Other favourite", Icons.Outlined.Star),
            CategoryIcon("category", "Miscellaneous other", Icons.Outlined.Category),
        ),
    ),
)

private val byKey: Map<String, CategoryIcon> =
    CATEGORY_ICON_SECTIONS.flatMap { it.icons }.associateBy { it.key }

/** Resolve a stored key to its vector; null for a null or unrecognized key (forward-compatible). */
fun iconVectorForKey(key: String?): ImageVector? = key?.let { byKey[it]?.vector }

/** Filter the full set by a case-insensitive substring of label or key; blank query returns all. */
fun searchIcons(query: String): List<CategoryIcon> {
    val q = query.trim()
    val all = CATEGORY_ICON_SECTIONS.flatMap { it.icons }
    if (q.isEmpty()) return all
    return all.filter { it.label.contains(q, ignoreCase = true) || it.key.contains(q, ignoreCase = true) }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.ui.icons.CategoryIconsTest"`
Expected: PASS. (A failing `everyKeyResolves`/compile error means an `Icons.Outlined.*` name is wrong — correct it to the nearest existing one.)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/ui/icons/CategoryIcons.kt \
        app/src/test/java/com/example/budgettracker/ui/icons/CategoryIconsTest.kt
git commit -m "feat(ui): add category icon registry with lookup and search"
```

---

## Task 4: Shared `CategoryIconChip`

One composable that renders the tinted chip when the key resolves, else the color-dot fallback — reused on every surface.

**Files:**
- Create: `app/src/main/java/com/example/budgettracker/ui/components/CategoryIconChip.kt`

- [ ] **Step 1: Write the component**

Create `CategoryIconChip.kt`. It reuses the existing `ColorDot` (`ui.screens.categories.ColorDot`):

```kotlin
package com.example.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.budgettracker.ui.icons.iconVectorForKey
import com.example.budgettracker.ui.screens.categories.ColorDot

/**
 * Leading visual for a category. When [iconKey] resolves to a registry icon, render it tinted by
 * [color] inside a faint color-washed rounded square; otherwise fall back to the color dot, centered
 * in the same [size] slot so rows stay aligned whether or not a category has an icon.
 */
@Composable
fun CategoryIconChip(
    iconKey: String?,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
) {
    val vector = iconVectorForKey(iconKey)
    if (vector != null) {
        Box(
            modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.3f))
                .background(color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(vector, contentDescription = null, tint = color, modifier = Modifier.size(size * 0.58f))
        }
    } else {
        Box(modifier.size(size), contentAlignment = Alignment.Center) {
            ColorDot(color, size = size * 0.32f)
        }
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/ui/components/CategoryIconChip.kt
git commit -m "feat(ui): add shared CategoryIconChip with color-dot fallback"
```

---

## Task 5: Category form Icon row, picker sheet, and list render

Add the picker to the create/edit flow, wire it end-to-end, and show the chip on the Categories list.

**Files:**
- Create: `app/src/main/java/com/example/budgettracker/ui/screens/categories/IconPickerSheet.kt`
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/categories/CategorySheets.kt:88-134` (`CategoryFormSheet`)
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/categories/CategoriesViewModel.kt:108-111`
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/categories/CategoriesScreen.kt:148-165`
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/categories/CategoryComponents.kt:184-197` (`CategoryRow`)

- [ ] **Step 1: Build the picker sheet**

Create `IconPickerSheet.kt`:

```kotlin
package com.example.budgettracker.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.budgettracker.ui.icons.CATEGORY_ICON_SECTIONS
import com.example.budgettracker.ui.icons.searchIcons

/**
 * Dedicated icon picker. Shows a search box, a "None" option (clears the icon → dot fallback), and
 * the sectioned grid. While browsing, icons render neutral; selecting one calls [onSelect] and the
 * caller closes the sheet. [tint] previews the currently selected category color on the chosen chip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerSheet(
    selectedKey: String?,
    tint: Color,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text("Choose icon", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                placeholder = { Text("Search icons") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
            )
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp).clip(CircleShape)
                    .clickable { onSelect(null) }
                    .background(
                        if (selectedKey == null) tint.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape,
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("None", style = MaterialTheme.typography.labelLarge)
                Text("  ·  use color dot", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val filtering = query.isNotBlank()
            val sections = if (filtering) listOf("Results" to searchIcons(query)) else CATEGORY_ICON_SECTIONS.map { it.title to it.icons }
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sections.forEach { (title, icons) ->
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                        )
                    }
                    items(icons, key = { it.key }) { icon ->
                        val selected = icon.key == selectedKey
                        Box(
                            Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (selected) tint.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { onSelect(icon.key) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                icon.vector,
                                contentDescription = icon.label,
                                tint = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }
                }
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.padding(bottom = 16.dp)) {}
                }
            }
        }
    }
}
```

- [ ] **Step 2: Add the Icon row to `CategoryFormSheet`**

In `CategorySheets.kt`, update `CategoryFormSheet` to hold `icon` state, render the Icon row, open the picker, and include `icon` in `onSave`. Replace the function (lines 88-134) with:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFormSheet(
    existing: Category?,
    groups: List<CategoryGroup>,
    defaultGroupId: Long?,
    onDismiss: () -> Unit,
    onSave: (groupId: Long, name: String, kind: Kind, color: String?, icon: String?) -> Unit,
    onArchive: () -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var groupId by remember { mutableStateOf(existing?.groupId ?: defaultGroupId ?: groups.firstOrNull()?.id) }
    var kind by remember { mutableStateOf(existing?.kind ?: Kind.EXPENSE) }
    var color by remember { mutableStateOf(existing?.color) }
    var icon by remember { mutableStateOf(existing?.icon) }
    var showIconPicker by remember { mutableStateOf(false) }
    val tint = color?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.primary

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (existing == null) "New category" else "Edit category", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            GroupDropdown(groups, groupId, onSelect = { groupId = it })
            Text("Kind", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Kind.entries.forEach { option ->
                    FilterChip(
                        selected = option == kind,
                        onClick = { kind = option },
                        label = { Text(if (option == Kind.INCOME) "Income" else "Expense") },
                    )
                }
            }
            Text("Icon", style = MaterialTheme.typography.labelLarge)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { showIconPicker = true }
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryIconChip(icon, tint, size = 36.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    iconLabelFor(icon) ?: "No icon",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Text("Change ›", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Color (optional)", style = MaterialTheme.typography.labelLarge)
            ColorPickerRow(selectedHex = color, allowNone = true, onSelect = { color = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (existing != null) TextButton(onClick = onArchive) { Text("Archive") }
                Spacer(Modifier.weight(1f))
                GradientButton(
                    "Save",
                    onClick = { groupId?.let { onSave(it, name, kind, color, icon) } },
                    enabled = name.isNotBlank() && groupId != null,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showIconPicker) {
        IconPickerSheet(
            selectedKey = icon,
            tint = tint,
            onSelect = { icon = it; showIconPicker = false },
            onDismiss = { showIconPicker = false },
        )
    }
}

/** Display label for the form's selected icon key, or null when none. */
private fun iconLabelFor(key: String?): String? =
    key?.let { k -> CATEGORY_ICON_SECTIONS.flatMap { it.icons }.firstOrNull { it.key == k }?.label?.substringBefore(' ') }
```

Add the imports needed at the top of `CategorySheets.kt` (some may already be present):

```kotlin
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.budgettracker.ui.components.CategoryIconChip
import com.example.budgettracker.ui.icons.CATEGORY_ICON_SECTIONS
```

- [ ] **Step 3: Thread `icon` through the ViewModel**

In `CategoriesViewModel.kt`, update `createCategory` (lines 108-111):

```kotlin
fun createCategory(groupId: Long, name: String, kind: Kind, color: String?, icon: String?) = viewModelScope.launch {
    val order = repository.observeCategories().first().count { it.groupId == groupId }
    report(repository.createCategory(groupId, name, kind, color, order, icon))
}
```

- [ ] **Step 4: Update the screen call site**

In `CategoriesScreen.kt`, update the `CategoryFormSheet` `onSave` (lines 153-159):

```kotlin
onSave = { groupId, name, kind, color, icon ->
    if (current.category == null) {
        viewModel.createCategory(groupId, name, kind, color, icon)
    } else {
        viewModel.updateCategory(current.category.copy(groupId = groupId, name = name, kind = kind, color = color, icon = icon))
    }
    sheet = null
},
```

- [ ] **Step 5: Render the chip on the Categories list**

In `CategoryComponents.kt`, update `CategoryRow` (lines 184-197) to lead with the chip instead of the dot:

```kotlin
@Composable
private fun CategoryRow(category: Category, groupColor: Color, onClick: () -> Unit) {
    val density = BudgetTheme.density
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .heightIn(min = density.rowMinHeight)
            .padding(horizontal = 16.dp, vertical = density.rowPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIconChip(category.icon, category.color?.let { parseHexColor(it) } ?: groupColor, size = 32.dp)
        Spacer(Modifier.width(12.dp))
        Text(category.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}
```

Add the import to `CategoryComponents.kt`:

```kotlin
import com.example.budgettracker.ui.components.CategoryIconChip
```

- [ ] **Step 6: Compile and run the full test suite**

Run: `./gradlew :app:compileDebugKotlin && ./gradlew test`
Expected: BUILD SUCCESSFUL; all unit tests PASS (the ViewModel now passes the real picked `icon` as `createCategory`'s 6th argument instead of relying on the default).

- [ ] **Step 7: Visual check (manual)**

Per repo convention, verify rendering on a device/emulator with mobile-mcp: open Categories → New category → tap the Icon row → search + pick an icon → save → confirm the tinted chip shows on the list row, and an icon-less category still shows the dot.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/ui/screens/categories/
git commit -m "feat(categories): icon picker in the category form and list render"
```

---

## Task 6: Log — icon on rows and in the category picker

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/log/LogViewModel.kt:27-37` (`TxnRow`) and `:96-109` (`buildLogState` mapping)
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/log/LogComponents.kt:101-117` (`TxnRowItem`)
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/log/TransactionSheet.kt:142-162` (`CategoryDropdown`)
- Test: `app/src/test/java/com/example/budgettracker/ui/screens/log/LogStateTest.kt` (add a case)

- [ ] **Step 1: Add `iconKey` to `TxnRow` and the failing test**

In `LogViewModel.kt`, add the field to `TxnRow` (after `leadingColor`):

```kotlin
data class TxnRow(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val groupName: String,
    val leadingColor: String,   // category.color ?: group.color
    val iconKey: String?,       // category.icon
    val kind: Kind,
    val amount: Long,
    val note: String?,
    val date: Long,
)
```

In `LogStateTest.kt`, add a test asserting the row carries the icon. Mirror the file's existing helpers for building a `Category`/`TransactionEntity` (it already constructs them); set `icon = "restaurant"` on the category and assert:

```kotlin
@Test
fun rowCarriesCategoryIcon() {
    val category = Category(id = 1, groupId = 1, name = "Dining", kind = Kind.EXPENSE, icon = "restaurant", order = 0, createdAt = 0, updatedAt = 0)
    val group = CategoryGroup(id = 1, name = "Leisure", color = "#8b5cf6", order = 0, createdAt = 0, updatedAt = 0)
    val txn = TransactionEntity(id = 1, categoryId = 1, amount = 1000, date = 0, description = null, createdAt = 0, updatedAt = 0)
    val state = buildLogState(listOf(txn), mapOf(1L to category), mapOf(1L to group), TxnFilter.ALL, ZoneId.of("UTC"))
    assertEquals("restaurant", state.sections.first().rows.first().iconKey)
}
```
(Match the exact `TransactionEntity` constructor parameters used elsewhere in that test file; add imports for `Category`, `CategoryGroup`, `TransactionEntity`, `ZoneId`, `assertEquals` if missing.)

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.ui.screens.log.LogStateTest"`
Expected: FAIL — compile error (`TxnRow` has no `iconKey`) until Step 3.

- [ ] **Step 3: Populate `iconKey` in `buildLogState`**

In `LogViewModel.kt`, in the `TxnRow(...)` construction inside `buildLogState` (around lines 98-108), add:

```kotlin
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
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.ui.screens.log.LogStateTest"`
Expected: PASS.

- [ ] **Step 5: Render the chip on the Log row**

In `LogComponents.kt`, in `TxnRowItem`, replace the leading 3dp color bar (line 117) with the icon chip:

```kotlin
        CategoryIconChip(row.iconKey, parseHexColor(row.leadingColor), size = 32.dp)
        Spacer(Modifier.width(12.dp))
```

Add the import to `LogComponents.kt`:

```kotlin
import com.example.budgettracker.ui.components.CategoryIconChip
```
(`parseHexColor` is already imported in this file.)

- [ ] **Step 6: Render the icon in the transaction category dropdown**

In `TransactionSheet.kt`, update the `DropdownMenuItem` inside `CategoryDropdown` (lines 157-159) to show a leading chip:

```kotlin
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    leadingIcon = {
                        CategoryIconChip(
                            category.icon,
                            category.color?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 28.dp,
                        )
                    },
                    onClick = { onSelect(category.id); expanded = false },
                )
            }
```

Add imports to `TransactionSheet.kt`:

```kotlin
import com.example.budgettracker.ui.components.CategoryIconChip
import com.example.budgettracker.ui.screens.categories.parseHexColor
```

- [ ] **Step 7: Compile and run tests**

Run: `./gradlew :app:compileDebugKotlin && ./gradlew :app:testDebugUnitTest --tests "com.example.budgettracker.ui.screens.log.LogStateTest"`
Expected: BUILD SUCCESSFUL; PASS.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/ui/screens/log/ \
        app/src/test/java/com/example/budgettracker/ui/screens/log/LogStateTest.kt
git commit -m "feat(log): show category icon on rows and in the category picker"
```

---

## Task 7: Report, Plan, and Recurring render

These surfaces already carry the full `Category`, so no state changes — just render the chip.

**Files:**
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/report/ReportComponents.kt:121-136` (`ReportRow`)
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/plan/PlanComponents.kt:104-119` (category row)
- Modify: `app/src/main/java/com/example/budgettracker/ui/screens/recurring/RecurringSheet.kt:118-120` (`CategoryDropdown`)

- [ ] **Step 1: Report rows**

In `ReportComponents.kt`, in `ReportRow`, add the chip before the category name `Text` (line 130). Insert:

```kotlin
        CategoryIconChip(
            row.category.icon,
            row.category.color?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant,
            size = 26.dp,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            row.category.name,
            modifier = Modifier.weight(1f),
            ...
        )
```

Add imports to `ReportComponents.kt`:

```kotlin
import com.example.budgettracker.ui.components.CategoryIconChip
import com.example.budgettracker.ui.screens.categories.parseHexColor
```
(Confirm `Spacer`/`androidx.compose.foundation.layout.width` imports exist; add `import androidx.compose.foundation.layout.Spacer` and `import androidx.compose.foundation.layout.width` if missing.)

- [ ] **Step 2: Plan rows**

In `PlanComponents.kt`, in the `group.categories.forEach { category -> Row { ... } }` block (lines 104-119), add the chip before the name `Text` (line 109):

```kotlin
                    CategoryIconChip(
                        category.icon,
                        category.color?.let { parseHexColor(it) } ?: parseHexColor(group.group.color),
                        size = 26.dp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(category.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
```

Add imports to `PlanComponents.kt` (`parseHexColor` is already imported per line 35's sibling import; add the chip + `Spacer`/`width` if missing):

```kotlin
import com.example.budgettracker.ui.components.CategoryIconChip
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
```

- [ ] **Step 3: Recurring category dropdown**

In `RecurringSheet.kt`, update the `DropdownMenuItem` (lines 118-120) like the Log one:

```kotlin
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    leadingIcon = {
                        CategoryIconChip(
                            category.icon,
                            category.color?.let { parseHexColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 28.dp,
                        )
                    },
                    onClick = { onSelect(category.id); expanded = false },
                )
            }
```

Add imports to `RecurringSheet.kt`:

```kotlin
import com.example.budgettracker.ui.components.CategoryIconChip
import com.example.budgettracker.ui.screens.categories.parseHexColor
import androidx.compose.material3.MaterialTheme
```
(Drop the `MaterialTheme` import line if it's already imported.)

- [ ] **Step 4: Compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Visual check (manual)**

With mobile-mcp, confirm the icon appears on Report tables, Plan target rows, and the Recurring category dropdown; icon-less categories still show the dot.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/example/budgettracker/ui/screens/report/ReportComponents.kt \
        app/src/main/java/com/example/budgettracker/ui/screens/plan/PlanComponents.kt \
        app/src/main/java/com/example/budgettracker/ui/screens/recurring/RecurringSheet.kt
git commit -m "feat(ui): show category icon on Report, Plan, and Recurring picker"
```

---

## Task 8: Docs

**Files:**
- Modify: `PRODUCT_SPEC.md` (§6.2 Category table)
- Modify: `CLAUDE.md` (progress section)

- [ ] **Step 1: Update the spec**

In `PRODUCT_SPEC.md` §6.2, add a row to the Category field table after `color`:

```markdown
| `icon` | `String?` | optional CategoryIcons registry key (e.g. `restaurant`); null → color dot |
```

Bump the schema/version note in that section if one is present, noting DB is now v2.

- [ ] **Step 2: Update CLAUDE.md**

In `CLAUDE.md`, under the progress list, add a short note (per the update-cadence preference, folded into this PR):

```markdown
- **Category icons ✅** — optional per-category vector icon (`Category.icon` registry key; DB **v2** + `MIGRATION_1_2` with seed backfill). Curated set in `ui/icons/CategoryIcons.kt`, rendered via the shared `ui/components/CategoryIconChip` (tinted chip, color-dot fallback) on Categories/Log/Report/Plan + the category pickers; picked via `IconPickerSheet`. Groups stay dot-only; export unchanged.
```

Also update the Phase 3 line's "`BudgetDatabase` v1" to note v2 + the first real migration.

- [ ] **Step 3: Final full verification**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL, all tests PASS.

- [ ] **Step 4: Commit**

```bash
git add PRODUCT_SPEC.md CLAUDE.md
git commit -m "docs: document Category.icon and DB v2 in spec and CLAUDE.md"
```

- [ ] **Step 5: Push and open the PR**

```bash
git push -u origin feat/category-icons
gh pr create --base main --title "feat: category icons" --body "Adds optional, color-tinted vector icons to categories (design: docs/superpowers/specs/2026-06-11-category-icons-design.md). DB v1→v2 with seed backfill; shared CategoryIconChip rendered across Categories/Log/Report/Plan and the category pickers; dedicated IconPickerSheet."
```

The human reviews and merges; do not self-merge.

---

## Self-Review

- **Spec coverage:** §Architecture data layer → Task 1 (column, migration, backfill, schema, wiring) + Task 2 (seed, repo). §Icon registry → Task 3. §Shared render component → Task 4. §Form + picker → Task 5. §Surfaces table (Categories/Log/dropdowns/Report/Plan) → Tasks 5/6/7. §Testing (registry, migration, seed, repo, buildLogState) → Tasks 1/2/3/6. §Out-of-scope (no group icons, no export) → respected (no `CategoryGroup`/`export/` changes). §Docs → Task 8. No gaps.
- **Placeholder scan:** No TBD/TODO; every code step shows full code; the icon set is a concrete ~60-entry starter (seed keys pinned) with an explicit "extend to ~100 following the same pattern" instruction (not a placeholder — it compiles and passes tests as written).
- **Type consistency:** `createCategory(groupId, name, kind, color, icon, order)` defined in Task 2 and called in Task 5 ViewModel — match. `onSave: (Long, String, Kind, String?, String?)` defined in Task 5 sheet and called in Task 5 screen — match. `CategoryIconChip(iconKey, color, modifier, size)` defined in Task 4, called in Tasks 5/6/7 with `(key, color, size = …)` — match. `iconVectorForKey`/`searchIcons`/`CATEGORY_ICON_SECTIONS` defined in Task 3, used in Tasks 4/5 — match. `TxnRow.iconKey` defined and populated in Task 6 — match.
- **Ordering note:** `createCategory`'s new `icon` param is last-with-default, so every commit compiles and stays green; Task 5 swaps the ViewModel from the default to the real picked icon (6th arg). No red-build window.
