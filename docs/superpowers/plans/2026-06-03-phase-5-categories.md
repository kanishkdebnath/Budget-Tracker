# Phase 5 — Categories (F2) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:executing-plans.

**Goal:** Replace the `CategoriesScreen` placeholder with real, ViewModel-backed group/category management reading the Phase 3 repositories — grouped list with filter chips, create/edit, and archive with the live-categories guard.

**Scope note:** Drag-to-reorder (F2.6) is **deferred to a focused follow-up PR (Phase 5b)** — it needs careful gesture handling and deserves isolated review. Everything else in F2 ships here.

**Architecture:** A `CategoriesViewModel` combines `CategoryRepository.observeGroups/observeCategories` (incl. archived) + a filter into a list of `GroupSection`s via a pure `buildSections(...)` (unit-tested). The screen renders sections as group cards with category rows, filter chips (All/Income/Expense/Archived), a FAB that opens create sheets, and a snackbar for guard failures. ViewModels are built by `AppViewModelProvider.Factory` reading `BudgetApplication.container` via `CreationExtras` (no Hilt). Order on create is computed from the current live count (`flow.first()`); editing adds `updateGroup/updateCategory` to the repository.

**Verification:** pure `buildSections` unit tests + Robolectric repository update tests under `./gradlew test`; `:app:assembleDebug`; live emulator run (install + drive via mobile-mcp) — create a category, archive a group with live categories (expect the guard snackbar), filter chips.

---

## Files

| File | Responsibility |
|---|---|
| `data/repository/CategoryRepository.kt` (edit) | add `updateGroup` / `updateCategory` (uniqueness-checked) |
| `ui/AppViewModelProvider.kt` | `viewModelFactory` reading `BudgetApplication.container` |
| `ui/screens/categories/CategoriesViewModel.kt` | `CategoryFilter`, `GroupSection`, pure `buildSections`, VM (sections/filter/message StateFlows; create/update/archive) |
| `ui/screens/categories/CategoriesScreen.kt` (rewrite) | filter chips + `LazyColumn` of group cards + FAB + snackbar + sheet host |
| `ui/screens/categories/CategoryComponents.kt` | `GroupCard`, `CategoryRow`, kind chip, color dot |
| `ui/screens/categories/CategorySheets.kt` | `ModalBottomSheet` create/edit forms for category & group |
| tests | `CategoriesSectionsTest` (pure), `CategoryRepository` update cases (Robolectric) |

## Tasks
1. **Repository edits + tests** — `updateGroup`/`updateCategory` with case-insensitive uniqueness (excluding self); Robolectric tests. Run `./gradlew test`.
2. **ViewModel + pure sections + tests** — `buildSections` for ALL/INCOME/EXPENSE/ARCHIVED; VM wiring; `AppViewModelProvider`. Unit-test `buildSections`. Run `./gradlew test`.
3. **UI** — components, sheets, screen rewrite; wire VM via `viewModel(factory = AppViewModelProvider.Factory)`. Compile + assemble.
4. **Verify on emulator**, commit, PR.

## Key behaviors
- **Filters:** ALL → live groups (incl. empty) with live categories; INCOME/EXPENSE → live groups having ≥1 live category of that kind, those categories only; ARCHIVED → archived categories grouped under their group + archived groups.
- **Create:** FAB → choose "New category" (name, group, kind, optional color) or "New group" (name, color). Order = current live count. Duplicate live name → snackbar, sheet stays.
- **Edit:** tap a category/group → form prefilled; Save (uniqueness-checked) or Archive.
- **Archive group** with live categories → repository `Failure` surfaced as a snackbar (§F2.5); no state change.
- Seeded data (7 groups / 6 categories) appears immediately on first launch via the Phase 3 seeder.

## Self-Review
- **Spec coverage:** F2.1–F2.5, F2.7 (display + CRUD + archive + guard + seed). F2.6 (reorder) explicitly deferred to Phase 5b.
- **No Phase 3 regressions:** repository gains methods (additive); existing tests untouched.
