# Category Icons — Design

**Status:** Approved (brainstorm) · **Date:** 2026-06-11 · **Feature area:** F2 (Categories)

## Summary

Give every category an **optional icon** drawn from a curated set of monochrome vector
icons, tinted by the category's color. Users pick an icon when creating or editing a
category. The icon renders everywhere a category appears (Categories, Log, the category
pickers, Report, Plan). A category with no icon falls back to today's color dot, so the
change is purely additive and backward-compatible.

This extends `PRODUCT_SPEC` F2 / §6.2. The spec is updated as part of this work (see §9).

## Decisions (from brainstorming)

1. **Representation: curated vector set** — monochrome Material-style line icons, **tinted by
   the category color**. Chosen over emoji (clashes with the navy/monochrome aesthetic, not
   tintable) and custom image upload (heavy; out of scope for an offline-first app).
2. **Scope: categories only.** Groups keep their existing colored dot in headers. No icon field
   on `CategoryGroup`.
3. **Surfaces: everywhere a category shows** — Categories list, Log transaction rows, the
   category dropdowns (Add/Edit transaction + Recurring template), Report per-group tables, Plan
   target rows.
4. **Source: `material-icons-extended`** referenced through an in-app registry. The DB stores a
   **stable string key** (e.g. `"restaurant"`), not the library symbol, so storage is decoupled
   from the icon source. R8 strips unreferenced icons in release builds.
5. **Row treatment: tinted chip** — a rounded-square chip (~34dp in the Categories list) holding
   the line icon tinted by the category color over a faint color wash. No-icon rows fall back to
   the color dot, centered in the same leading slot so alignment holds.
6. **Picker: a dedicated `IconPickerSheet`** launched from an Icon row in the category form;
   search + a "None" option + a sectioned grid. Icons render neutral while browsing; the chosen
   one shows the tinted chip.

Visual mockups produced during brainstorming live in
`.superpowers/brainstorm/49491-1781162285/content/` (gitignored scratch).

## Architecture

### Data layer

- **`Category` entity** gains one nullable column:
  ```kotlin
  val icon: String? = null   // stable CategoryIcons registry key, e.g. "restaurant"
  ```
  No new index. Color resolution and all other fields are unchanged.

- **`BudgetDatabase` v1 → v2.** Add a `MIGRATION_1_2`:
  ```sql
  ALTER TABLE category ADD COLUMN icon TEXT;
  ```
  Wire it via `Room.databaseBuilder(...).addMigrations(MIGRATION_1_2)` in `AppContainer`
  (no migrations are wired today). `exportSchema = true` is already on, so **regenerate the
  exported schema** — `app/schemas/.../2.json` must be committed (CI/Room verify it).

- **Best-effort backfill (same migration).** So existing installs (including the dev's test
  device, where the once-only seeder won't re-run) get icons without a data wipe, the migration
  also runs name-matched updates for the seed categories:
  ```sql
  UPDATE category SET icon='payments'        WHERE icon IS NULL AND name='Salary';
  UPDATE category SET icon='home'            WHERE icon IS NULL AND name='Rent';
  UPDATE category SET icon='bolt'            WHERE icon IS NULL AND name='Electricity';
  UPDATE category SET icon='shopping_cart'   WHERE icon IS NULL AND name='Groceries';
  UPDATE category SET icon='directions_car'  WHERE icon IS NULL AND name='Transport';
  UPDATE category SET icon='restaurant'      WHERE icon IS NULL AND name='Dining';
  ```
  This is intentionally name-based and `icon IS NULL`-guarded — it touches only untouched seed
  rows and is harmless if a user renamed them.

- **`SeedData`** assigns the same icon keys to the 6 seed categories so **fresh installs** get
  them directly (the `SeedCategory` data class gains an `icon: String?`, threaded through
  `DatabaseSeeder.seedIfEmpty`).

- **`CategoryRepository.createCategory(...)`** gains an `icon: String?` parameter (persisted onto
  the inserted `Category`). `updateCategory(category: Category)` already takes the whole entity,
  so edits are covered with no signature change.

### Icon registry — `ui/icons/CategoryIcons.kt`

`ImageVector` is a Compose type, so the registry lives in `ui/`; the data layer stays
Compose-free and only ever handles the string key.

```kotlin
data class CategoryIcon(val key: String, val label: String, val vector: ImageVector)
data class IconSection(val title: String, val icons: List<CategoryIcon>)

val CATEGORY_ICON_SECTIONS: List<IconSection>   // ~100 icons, ~8–10 sections

fun iconVectorForKey(key: String?): ImageVector?  // null for null/unknown key
```

- Sections: **Food & Drink, Home & Bills, Transport, Shopping, Money, Health, Leisure,
  Work & Education, Misc** (final list finalized during implementation; ~10–14 icons each,
  ~100 total).
- Keys are lowercase snake_case, stable, unique across the whole set.
- `iconVectorForKey` returns `null` for an unknown key — forward-compatible: a key from a future
  version that an older build doesn't recognize simply falls back to the dot rather than crashing.
- A flat `allIcons` accessor + a substring filter over `label`/`key` backs the picker search.

### Shared render component — `CategoryIconChip` (`ui/components/`)

One composable owns the treatment, reused on every surface so it stays consistent:

```kotlin
@Composable
fun CategoryIconChip(iconKey: String?, color: Color, size: Dp = 34.dp, modifier: Modifier = …)
```

- Resolves `iconVectorForKey(iconKey)`:
  - **non-null** → tinted chip: rounded-square (`size`) with `color.copy(alpha = ~0.16f)` wash and
    the vector tinted `color`, sized to roughly `size * 0.6`.
  - **null** → the existing color dot (`ColorDot`), centered within a `size`-wide slot so leading
    edges align across mixed rows.
- Light/dark: follows the existing `BudgetTheme.isLight` conventions; alpha/inks chosen to read in
  both themes (mirror `KindChip`/`ColorDot` handling).
- Callers pass the already-resolved color (`category.color ?: groupColor`), matching today's logic.
- The existing standalone `ColorDot` stays (group headers still use it).

### Category form + picker — `ui/screens/categories/`

- **`CategoryFormSheet`** gains an **Icon** row between Name and Color: a `CategoryIconChip`
  preview (in the currently selected color) + the icon's label + "Change ›". Holds `icon: String?`
  state. `onSave` signature becomes `(groupId, name, kind, color, icon)`; wire the new arg through
  `CategoriesScreen` → `CategoriesViewModel` → `CategoryRepository.createCategory` /
  `updateCategory`.
- **`IconPickerSheet`** (new, `ModalBottomSheet`): a search `OutlinedTextField` (filters the flat
  set by label/key), a **None** chip (sets `icon = null` → dot), then `CATEGORY_ICON_SECTIONS`
  rendered as labeled `LazyVerticalGrid` sections. Browsing cells render the vector neutral
  (`onSurfaceVariant`); the selected cell shows the tinted chip. Selecting closes the sheet and
  updates the form's `icon` state. Respects the `extraLarge = 28dp` sheet-shape rule (no pill on
  `ModalBottomSheet`).

### Surfaces (all via `CategoryIconChip`)

| Surface | Component | Plumbing |
|---|---|---|
| Categories list | `CategoryComponents.CategoryRow` | replace leading `ColorDot` with `CategoryIconChip(category.icon, color)` |
| Log rows | `log/` `TxnRow` → row composable | **add `iconKey: String?` to `TxnRow`** (built from `category.icon` in `buildLogState`); render chip as the row's leading element |
| Add/Edit txn dropdown | `log/TransactionSheet` | category dropdown items show `CategoryIconChip` (has full `Category`) |
| Recurring dropdown | `recurring/RecurringSheet` | same |
| Report tables | `report/ReportComponents.ReportRow` | `CategoryReportRow` already carries the full `Category` → `category.icon` |
| Plan target rows | `plan/` row composable | `PlanGroup.categories` already carries full `Category` → `category.icon` |

Only **Log's `TxnRow`** needs a new field; everything else already carries the `Category` entity
and gets `icon` for free once the column exists. Sizes scale per context (e.g. ~24–28dp in dense
dropdown/Report/Plan rows vs 34dp in the Categories list).

## Out of scope (YAGNI)

- **Group icons** — categories only.
- **Emoji / custom image upload** — curated vector set only.
- **Icons in Excel/PDF export** — those stay text/number tables; `export/` is untouched.
- **Per-icon recoloring beyond the category color** — the icon always takes the category color.

## Testing

JVM-unit-testable (no emulator), consistent with the repo's testing conventions:

- **Registry** (`src/test`): every `key` is unique across all sections; every `CategoryIcon`
  resolves via `iconVectorForKey`; `iconVectorForKey(null)` and an unknown key return `null`; the
  search filter matches by label and key substring.
- **Migration** (Robolectric DAO test, `src/test`): open v1, run `MIGRATION_1_2`, assert the
  `icon` column exists, a pre-inserted `name='Rent'` row is backfilled to `home`, and a
  non-seed/renamed row stays `null`.
- **Seed** (existing seeder test pattern): seeded `Dining` has `icon = "restaurant"`, etc.
- **Repository**: `createCategory(..., icon = "x")` persists the key; round-trips via the DAO.
- UI render is covered by the existing Compose preview / manual-check flow (mobile-mcp), per repo
  convention — no new instrumented tests.

## Affected files

- `data/entity/Category.kt` — add `icon`.
- `data/db/BudgetDatabase.kt` — `version = 2`; `data/db/Migrations.kt` (new) — `MIGRATION_1_2`.
- `data/AppContainer.kt` — `.addMigrations(MIGRATION_1_2)`.
- `app/schemas/.../2.json` — regenerated, committed.
- `data/db/SeedData.kt`, `data/db/DatabaseSeeder.kt` — seed icon keys.
- `data/repository/CategoryRepository.kt` — `createCategory` icon param.
- `ui/icons/CategoryIcons.kt` (new) — registry.
- `ui/components/CategoryIconChip.kt` (new) — shared chip.
- `ui/screens/categories/` — `CategoryFormSheet`, new `IconPickerSheet`, `CategoryRow`,
  `CategoriesViewModel`, `CategoriesScreen`.
- `ui/screens/log/LogViewModel.kt` (`TxnRow` + `buildLogState`), `LogComponents`/row, `TransactionSheet`.
- `ui/screens/recurring/RecurringSheet.kt`; `ui/screens/report/ReportComponents.kt`;
  `ui/screens/plan/` row composable.
- `gradle/libs.versions.toml` + `app/build.gradle.kts` — add `material-icons-extended`.
- `PRODUCT_SPEC.md` §6.2 — document the `icon` field.
- `CLAUDE.md` — note the icon feature + DB v2 in the progress section (per the update-cadence
  preference: folded into this feature's PR).
