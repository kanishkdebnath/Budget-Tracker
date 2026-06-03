# Phase 3 — Room Data Layer — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:executing-plans / subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** A reactive, offline persistence layer — five Room entities, DAOs returning `Flow`, repositories with the atomic/guarded business logic, idempotent first-launch seeding, and DataStore preferences — all JVM-testable via Robolectric (no emulator).

**Architecture:** `com.example.budgettracker.data` holds `entity/`, `dao/`, `db/` (database + converters + seed), `repository/`, and `AppContainer` (manual DI). Money is `Long` minor units; months are `YYYY-MM` strings derived in the device-local zone (reusing Phase 1 `MonthUtils`). Multi-DAO writes (recurring apply, bulk target upsert, seeding) run inside `RoomDatabase.withTransaction { }` for atomicity. Timestamps are injected via a `now: () -> Long` provider so tests are deterministic.

**Tech stack added:** Room 2.8.4 (KSP 2.2.10-2.0.2), DataStore 1.2.1, coroutines 1.10.2, Robolectric 4.16.1 + androidx.test.core 1.7.0 (test). `android.disallowKotlinSourceSets=false` is required for KSP under AGP 9's built-in Kotlin.

**Verification:** Robolectric DAO/repository tests run under `./gradlew test` (proven by the `CategoryGroupDaoTest` spike). Room validates all queries at KSP compile time. Pure logic (converters, seed data, `instantForDay`) is plain JVM-tested. No emulator needed. The Application/manifest wiring that *runs* the seeder on launch is deferred to **Phase 4** (app shell) — Phase 3 builds and tests the seeder as a class.

---

## Key design decisions (deviations from a literal reading of §6, with rationale)

1. **Name uniqueness is enforced at the repository layer, not by a DB unique index.** §6.1/§6.2 list `unique (name)` but the functional requirements (§F2.1/§F2.2) say *unique among **live** items, case-insensitive*. Room can't express partial (`WHERE archived=0`) or case-insensitive unique indices via `@Index`. On a single device (§10, concurrency moot) a repository-level check (`LOWER(name)` among non-archived) is sufficient and correctly lets an archived name be reused. The DB keeps a plain (non-unique) `name` index for lookups.
2. **`Target (month, categoryId)` stays a true DB unique index** — it's a genuine global invariant (one target per category per month) with no archive dimension, so the DB enforces it (and powers upsert-on-conflict).
3. **Transaction entity class is named `TransactionEntity`** (table `transactions`) to avoid colliding with Room's `@Transaction` and the SQLite keyword (§6 note).
4. **`recurringTemplateId` FK uses `onDelete = SET_NULL`** so hard-deleting a template (§F5.5) preserves its past transactions with a null link.
5. **Enum constants are uppercase (`INCOME`/`EXPENSE`/`MONTHLY`)** with a `TypeConverter` storing `.name`; display casing is a UI concern.

---

## Task slices

Built and committed in four green slices. Each slice: write code + tests, run `./gradlew test`, commit.

### Slice A — Entities, enums, converters, database
- `data/entity/Enums.kt` — `Kind { INCOME, EXPENSE }`, `Cadence { MONTHLY }`.
- `data/entity/{CategoryGroup,Category,TransactionEntity,Target,RecurringTemplate}.kt` — per §6, indices per the decisions above.
- `data/db/Converters.kt` — enum ↔ String.
- `data/db/BudgetDatabase.kt` — `@Database(version = 1)` with all 5 entities + `@TypeConverters(Converters)`; one abstract `xDao()` per DAO.
- `domain/time/MonthUtils.instantForDay(month, day, zone, hour=9): Long` — instant for `dayOfMonth` (recurring apply, §7.4).
- Tests (pure JVM): `ConvertersTest`, `MonthUtilsInstantTest`.

### Slice B — DAOs
One DAO per entity (`@Dao` interfaces). Reads return `Flow`; writes are `suspend`. Key queries:
- `CategoryGroupDao`: `observeLive` (archived=0, ORDER BY `order`), `observeAll`, `getById`, `findLiveByName` (`LOWER(name)=LOWER(:n) AND archived=0`), `count`, `insert`, `update`.
- `CategoryDao`: `observeAllLive`, `observeAll`, `getById`, `findLiveByName`, `countLiveInGroup`, `insert`, `update`.
- `TransactionDao`: `observeInRange(start, end)` (date >= start AND date < end, ORDER BY date DESC, createdAt DESC), `getById`, `insert`, `update`, `deleteById` (hard delete).
- `TargetDao`: `observeByMonth`, `getByMonth`, `upsert` (`@Insert(onConflict = REPLACE)`), `deleteByCategoryAndMonth`.
- `RecurringTemplateDao`: `observeAll` (ORDER BY active DESC, createdAt ASC), `getById`, `insert`, `update`, `deleteById`, `setLastRunMonth(id, month)`.
- Tests (Robolectric): `CategoryGroupDaoTest` (exists, extend), `CategoryDaoTest`, `TransactionDaoTest` (range + hard delete), `TargetDaoTest` (unique + upsert replaces), `RecurringTemplateDaoTest` (sort order).

### Slice C — Repositories (atomic + guarded logic)
- `CategoryRepository(db, groupDao, categoryDao, now)`:
  - `observeGroups`/`observeCategories` (Flow passthrough).
  - `createGroup(name, color)` / `createCategory(groupId, name, kind, color)` → reject duplicate live name (case-insensitive) with a typed `Result`.
  - `archiveGroup(id)` → reject if `countLiveInGroup > 0` ("group has live categories", §F2.5).
  - `archiveCategory(id)`, `reorder…`.
- `TransactionRepository(db, txnDao, now)`: `observeMonth(month, zone)` via `MonthUtils.monthRange`; `add/edit/delete`.
- `TargetRepository(db, targetDao, now)`: `observeMonth`; `bulkSave(month, entries)` inside `withTransaction` — upsert non-blank, delete cleared (§F3.3/§F3.5).
- `RecurringRepository(db, recurringDao, txnDao, now)`: `apply(templateId, month, zone)` — guard `!active`/`lastRunMonth==month`, then `withTransaction { txnDao.insert(...); recurringDao.setLastRunMonth(...) }` (§7.4); typed `Result`. CRUD.
- `data/Result.kt` — `sealed interface OpResult { object Success; data class Failure(reason) }` (or `kotlin.Result`); used by guarded ops.
- Tests (Robolectric): `CategoryRepositoryTest` (case-insensitive dup rejected, archived name reusable, archive guard), `TargetRepositoryTest` (bulk upsert + clear), `RecurringRepositoryTest` (apply once creates 1 txn + sets lastRunMonth; double-apply rejected; inactive rejected).

### Slice D — Seeding, preferences, AppContainer
- `data/db/SeedData.kt` — pure data: 7 groups + 6 categories (§6.7) with colors/orders/kinds.
- `data/db/DatabaseSeeder(db, groupDao, categoryDao, now)` — `seedIfEmpty()` inside `withTransaction`, guarded by `groupDao.count() == 0` (idempotent, §F2.7).
- `data/repository/PreferencesRepository(dataStore)` — `currency` (default `"INR"`), `density` (default `"comfortable"`) as `Flow`, with setters.
- `data/AppContainer(context)` — builds `BudgetDatabase` (`budget.db`), the DataStore, all DAOs, repositories, and the seeder. Manual DI; consumed by `BudgetApplication` in Phase 4.
- Tests: `SeedDataTest` (pure: 7 groups, 6 categories, names/kinds/orders match §6.7), `DatabaseSeederTest` (Robolectric: seeds 7+6; calling twice stays 7+6), `PreferencesRepositoryTest` (Robolectric/temp DataStore: default INR; set persists).

---

## Phase exit criteria
- `./gradlew test` green: all Robolectric DAO/repo/seeder tests + pure converter/seed/time tests + the Phase 1/2 suites.
- `./gradlew :app:assembleDebug` green (Room KSP validates every query; schema exported to `app/schemas/`).
- Atomicity, idempotency, uniqueness, and the archive guard are each covered by a test.
- `AppContainer` + `DatabaseSeeder` exist and are tested; their launch wiring lands in Phase 4.

## Self-Review notes
- **Spec coverage:** §6.1–6.6 entities; §6.7 seeding; §7.4 recurring apply; §F2.5 archive guard; §F3.3/§3.5 target bulk save; hard-delete transactions (§6.3). Money stays `Long`; months reuse `MonthUtils`.
- **Deferred (documented):** Report aggregation queries (§7.2) land in Phase 8; per-transaction sum queries are added there, not here. Application/manifest seeding wiring lands in Phase 4. Schema migrations: v1 only for now; `exportSchema=true` lays the groundwork.
