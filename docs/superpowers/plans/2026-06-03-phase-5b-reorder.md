# Phase 5b — Drag-to-Reorder (F2.6) — Implementation Plan

> Follow-up to Phase 5. REQUIRED SUB-SKILL: superpowers:executing-plans.

**Goal:** Drag-to-reorder groups (global order) and categories (within a group), persisted (§F2.6).

**Architecture:** Uses `sh.calvin.reorderable` 3.1.0. On the **All** filter only (reordering a filtered view is ambiguous), groups render in a `ReorderableLazyColumn` and each group card's categories in a nested non-lazy `ReorderableColumn`. Local state animates the drag; the new order persists on drop and re-syncs from the repository `Flow`. Order is written via atomic DAO `@Transaction reorder(ids, updatedAt)` default methods that `setOrder(id, index)` for each id.

## Files
- `gradle/libs.versions.toml` + `app/build.gradle.kts` — `sh.calvin.reorderable:reorderable:3.1.0`.
- `data/dao/{CategoryGroupDao,CategoryDao}.kt` — `setOrder` + `@Transaction reorder(orderedIds, updatedAt)`.
- `data/repository/CategoryRepository.kt` — `reorderGroups` / `reorderCategories`.
- `ui/screens/categories/CategoriesViewModel.kt` — `reorderGroups` / `reorderCategories`.
- `ui/screens/categories/CategoryReorder.kt` — `ReorderableSections` + `ReorderableGroupCard` (group handle outer, category handles inner).
- `ui/screens/categories/CategoriesScreen.kt` — use `ReorderableSections` when `filter == ALL`, else the static list.
- test: `CategoryRepositoryTest.reorderGroupsPersistsNewOrder`.

## Notes / API gotchas
- Lazy list: `ReorderableItem(state, key) { isDragging -> }`; handle via `Modifier.draggableHandle(onDragStopped = …)` in `ReorderableCollectionItemScope`.
- Non-lazy `ReorderableColumn(list, onSettle = { from, to -> }) { _, item, _ -> key(item.id) { ReorderableItem { … Modifier.draggableHandle() } } }` — the inner `ReorderableItem { }` wrapper is required to get the drag-handle scope (differs from the lazy API).

## Verification (done)
- `./gradlew test` green (50 tests; reorder persistence covered).
- Emulator: dragged Bills group to the top and Electricity above Rent; force-stop + relaunch retained both orders.

## Self-Review
- **Spec coverage:** F2.6 (reorder groups + categories, persisted). Completes F2 alongside Phase 5.
- Reorder gated to the All filter; filtered views keep the static (non-draggable) list.
