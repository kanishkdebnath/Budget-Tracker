# Budget Tracker (Android) — Product Specification

**Status:** Draft v1
**Date:** 2026-06-03
**Provenance:** Feature-extracted from the *pathforge* web budget module, adapted to a **local-only, single-user Android app**.

---

## 1. Overview & Goals

Budget Tracker is an offline-first personal budgeting app for Android. A single user logs income and expense transactions, organizes them into categories, sets monthly spending targets, reviews a monthly report with a plain-language narrative, manages recurring entries, and exports any month to Excel or PDF.

**Goals**

- **Zero-friction daily logging.** Adding a transaction is a one-row, few-tap action with an inline calculator for amounts.
- **Plan vs. actual.** Per-category monthly targets, with a report that shows actual-vs-target deltas and a one-line narrative summary.
- **Own your data.** Everything lives on-device. Excel/PDF export doubles as the backup/portability mechanism.
- **Multi-currency display.** One user-chosen currency, formatted correctly (₹ lakh/crore grouping, JPY zero-decimal, etc.).

**Target user.** An individual tracking personal finances on their phone who wants structure (categories, targets, reports) without an account, a subscription, or a network connection.

---

## 2. Non-Goals (v1)

- **No accounts, login, or auth.** Single user per device install.
- **No cloud sync or multi-device.** Data is local. (See §13 Deferred.)
- **No bank/SMS auto-import.** All transactions are entered manually (or applied from a recurring template).
- **No automatic recurring posting.** Recurring entries are applied by an explicit user tap (see §7). Auto-posting is deferred.
- **No multi-currency *per transaction*.** One display currency for all data.
- **No shared/household budgets, no transfers between accounts.** `kind` is strictly `income | expense`.

---

## 3. User Stories

**Logging (the Log screen)**
- As a user, I can add an income or expense with an amount, category, date (default today), and optional note, so my month stays current.
- As a user, I can enter an amount using a calculator (`+ − × ÷`), so I don't have to do mental math.
- As a user, I can see month-to-date Income, Expense, and Net at a glance.
- As a user, I can edit or delete any transaction.
- As a user, I can move between months and view/enter past or future transactions.

**Categories (the Categories screen)**
- As a user, I can create groups (e.g. Bills, Leisure) and categories within them, each with a kind and optional color.
- As a user, I can reorder groups and categories by dragging.
- As a user, I can archive a category I no longer use without losing its past transactions.

**Planning (the Plan screen)**
- As a user, I can set a monthly target amount per category.
- As a user, when I open a new month with no targets, last month's targets are pre-filled so I can tweak rather than retype.

**Reporting (the Report screen)**
- As a user, I can see a group-by-group breakdown of target vs. actual with signed deltas, color-coded.
- As a user, I can read a one-sentence narrative summarizing how the month went.
- As a user, I can export the month to Excel or PDF and share it.

**Recurring (the Recurring screen)**
- As a user, I can define recurring entries (e.g. Salary on day 1, Rent on day 5) with a fixed amount and category.
- As a user, I can apply a recurring entry to the current month with one tap, and the app stops me from applying it twice.

**Settings**
- As a user, I can choose my display currency, and it applies everywhere instantly.

---

## 4. Feature List

| # | Feature | Summary |
|---|---------|---------|
| F1 | **Transactions (Log)** | CRUD income/expense entries; month view; running Income/Expense/Net band; calculator amount entry. |
| F2 | **Groups & Categories** | Two-level hierarchy; CRUD; archive (soft); drag-reorder; per-category kind & color; per-group color. |
| F3 | **Monthly Targets (Plan)** | One target per category per month; bulk save; previous-month carry-forward pre-fill. |
| F4 | **Report** | Group/category target-vs-actual table with deltas; deterministic narrative; income/expense/net totals. |
| F5 | **Recurring templates** | Monthly templates; manual one-tap apply per month; idempotent guard; active toggle. |
| F6 | **Export** | Excel (3 sheets) and PDF report; share via Android share sheet. |
| F7 | **Currency & Settings** | User-level ISO-4217 currency; locale-aware formatting everywhere. |
| F8 | **Calculator popover** | Arithmetic entry helper bound to amount fields. |

---

## 5. Functional Requirements

Notation: amounts are **minor units** (e.g. paise/cents) stored as `Long`; displayed in major units. "Live" = not archived.

### F1 — Transactions (Log screen)

- **F1.1** Add a transaction with: `date` (default = today, local), `categoryId` (required, must be a live category), `amount` (required, > 0 minor units), `description` (optional, ≤ 500 chars).
- **F1.2** The Log screen is scoped to a selected month (default = current local month). It lists that month's transactions in two columns — **Income** and **Expense** — each sorted by `date` DESC, then `createdAt` DESC.
- **F1.3** A **Net band** shows month-to-date Income (Σ income txns), Expense (Σ expense txns), and Net (Income − Expense), with Net visually positive/negative.
- **F1.4** Edit any field of a transaction; delete is a **hard delete** (no archive for transactions).
- **F1.5** If the user enters a transaction whose date falls in a month other than the one being viewed, the app navigates to that month and confirms with a toast/snackbar.
- **F1.6** Amount entry uses `BudgetAmountInput` semantics: parses grouped input ("1,200" → `120000`), rejects negatives / scientific notation / symbols, and shows minor→major correctly (e.g. `120050` → "1200.50").

**Acceptance:** Adding a ₹500 expense in June updates the June Expense column and the Net band immediately; deleting it reverts both.

### F2 — Groups & Categories (Categories screen)

- **F2.1** **Group:** `name` (1–80, unique among live groups, case-insensitive), `color` (`#RRGGBB`, required), `order` (≥0), `archived`.
- **F2.2** **Category:** belongs to a group; `name` (1–80, unique among live categories), `kind` (`income|expense`), `color` (`#RRGGBB`, optional), `order` (per-group), `archived`.
- **F2.3** Create/edit/move categories between groups; create/edit groups.
- **F2.4** **Archive** (soft-delete) categories and groups; archived items disappear from pickers and lists but their transactions remain.
- **F2.5** A group may only be archived when it has **no live categories** (otherwise block with an explanatory message).
- **F2.6** Drag-to-reorder groups (global order) and categories (within a group); order persists.
- **F2.7** On **first app launch**, seed 7 default groups and 6 default categories (see §6.7). Seeding is idempotent — it runs only when no groups exist.

**Acceptance:** A fresh install shows Income/Bills/Household/Debt/Leisure/Savings/Other with the six seeded categories; archiving "Dining" hides it from the add-transaction picker but past Dining transactions still appear in reports.

### F3 — Monthly Targets (Plan screen)

- **F3.1** A target is `(categoryId, month YYYY-MM, amount ≥ 0)`; **at most one** target per category per month (enforced by a unique index).
- **F3.2** The Plan screen lists all live categories grouped by group, each with an amount input (major units, optional/blank = no target).
- **F3.3** **Save** performs a bulk upsert of all entered targets for the month in one action.
- **F3.4** **Pre-fill rule:** if the month has targets, show them. Else if the *previous* month has targets, copy them into the form and show a "pre-filled from last month" banner. Else show an empty form with a "first time" banner.
- **F3.5** Clearing an input and saving removes that category's target for the month.

**Acceptance:** Setting June targets, then opening July (empty) pre-fills July's form with June's values, editable before save.

### F4 — Report (Report screen)

- **F4.1** For the selected month, show per **group**: name, inferred kind label, and a subtotal row (target, actual, delta).
- **F4.2** Within each group, per **category**: target, actual, `delta = actual − target` with signed `+/−` display.
- **F4.3** Color-code deltas: overage on expense in red, underspend / favorable in green.
- **F4.4** Show **totals**: Income (Σ income-group actuals), Expense (Σ expense-group actuals), Net; and **target totals** the same way.
- **F4.5** Show the **narrative** (one to three sentences) generated per §6.4.
- **F4.6** Show **Recurring due this month** (templates not yet applied) and provide **Export** actions (F6).

**Acceptance:** With ₹3,500 actual vs ₹3,000 planned expense, the report shows a red +₹500 delta on the relevant categories and a narrative stating "over by ₹500 (17%)".

### F5 — Recurring templates (Recurring screen)

- **F5.1** Template fields: `label` (1–120), `categoryId`, `amount` (≥0), `cadence` (`monthly`), `dayOfMonth` (1–28), `active` (default true), `lastRunMonth` (YYYY-MM, nullable).
- **F5.2** List templates sorted by `active` DESC, then `createdAt` ASC. Each row shows label, category, day, amount, active toggle, and an **Apply** action.
- **F5.3** **Apply** creates a transaction for the current month dated `dayOfMonth` (see §6.5), linked via `recurringTemplateId`, and sets `lastRunMonth` to the current month.
- **F5.4** **Idempotency guard:** if `lastRunMonth == currentMonth`, Apply is disabled / returns "already applied this month". If `active == false`, Apply is blocked ("template inactive").
- **F5.5** Create/edit/delete templates (hard delete). Editing an inactive template is allowed; only active templates can be applied.

**Acceptance:** Tapping Apply on "Salary (day 1)" in June creates one June-1 income transaction and disables the button for June; re-tapping does nothing destructive.

> **Decision (faithful port):** recurring entries are **manually applied**, mirroring pathforge. The web app's DB race-safety becomes a simple local "already applied" check. **Auto-apply via WorkManager** is listed in §13 Deferred — flip this decision there if you want automatic posting.

### F6 — Export

- **F6.1** From the Report screen, export the selected month as **Excel**, **PDF**, or **both**.
- **F6.2** Generate the file on a background thread, write to app cache / a user-chosen location (SAF), and present the Android **share sheet** (and/or "Save to Files").
- **F6.3** Excel structure per §8.1; PDF structure per §8.2. Filenames: `budget-{YYYY-MM}-logs.xlsx`, `budget-{YYYY-MM}-report.pdf`.
- **F6.4** Embed a Unicode font (Noto Sans) in the PDF so currency glyphs (₹, €, ¥, £) and Indic scripts render correctly.
- **F6.5** Disable export with an explanatory tooltip/snackbar while month data is still loading.

**Acceptance:** Exporting June produces a 3-sheet xlsx whose totals match the report, and a PDF whose ₹ symbols render correctly.

### F7 — Currency & Settings

- **F7.1** A single user-level currency (ISO-4217), default **INR**. Picker promotes a common list (INR, USD, EUR, GBP, AUD, CAD, SGD, JPY) and accepts any valid 3-letter code.
- **F7.2** Changing currency retroactively reformats all displayed and exported amounts (amounts are stored currency-agnostic in minor units).

### F8 — Calculator popover

- **F8.1** Tapping an amount field opens a calculator overlay supporting `+ − × ÷ = C`, decimal point, and grouped display, feeding the parsed minor-unit value back into the field.

---

## 6. Data Model

Five Room entities + one preference store. **No `userId`** (single-user). Timestamps are epoch millis (`Long`). Money is `Long` minor units (range `0 .. 1_000_000_000_000`). `kind`/`cadence` are stored as enums (TEXT).

> **Room note:** `TRANSACTION` is a SQLite keyword — name the transaction table `transactions` (plural) or `budget_transaction` to avoid collisions.

### 6.1 CategoryGroup  (table `category_group`)
| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` PK autoGenerate | |
| `name` | `String` | 1–80 chars |
| `color` | `String` | `#RRGGBB` |
| `order` | `Int` | ≥0, display order |
| `archived` | `Boolean` | default `false` |
| `createdAt` / `updatedAt` | `Long` | epoch millis |

Indices: `(archived, order)`; **unique** `(name)` (case-insensitive — store/compare normalized).

### 6.2 Category  (table `category`)
| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` PK autoGenerate | |
| `groupId` | `Long` FK → category_group | |
| `name` | `String` | 1–80 chars |
| `kind` | `Kind` enum | `income` \| `expense` |
| `color` | `String?` | optional `#RRGGBB` |
| `order` | `Int` | per-group order |
| `archived` | `Boolean` | default `false` |
| `createdAt` / `updatedAt` | `Long` | |

Indices: `(groupId, archived, order)`; **unique** `(name)`; `(archived, kind)`.

### 6.3 Transaction  (table `transactions`)
| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` PK autoGenerate | |
| `date` | `Long` | epoch millis (instant); month derived in **local TZ** |
| `categoryId` | `Long` FK → category | |
| `amount` | `Long` | minor units, > 0 |
| `description` | `String?` | ≤ 500 chars |
| `recurringTemplateId` | `Long?` FK → recurring_template | set when created from a template |
| `createdAt` / `updatedAt` | `Long` | |

Indices: `(date DESC)`; `(categoryId, date DESC)`. **Hard-delete** only.

### 6.4 Target  (table `target`)
| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` PK autoGenerate | |
| `categoryId` | `Long` FK → category | |
| `month` | `String` | `YYYY-MM` |
| `amount` | `Long` | minor units, ≥ 0 |
| `createdAt` / `updatedAt` | `Long` | |

Indices: **unique** `(month, categoryId)`; `(categoryId, month DESC)`. Hard-delete.

### 6.5 RecurringTemplate  (table `recurring_template`)
| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` PK autoGenerate | |
| `label` | `String` | 1–120 chars |
| `categoryId` | `Long` FK → category | |
| `amount` | `Long` | minor units, ≥ 0 |
| `cadence` | `Cadence` enum | `monthly` (only) |
| `dayOfMonth` | `Int` | 1–28 (avoids Feb 29+ edge cases) |
| `lastRunMonth` | `String?` | `YYYY-MM`, last applied month |
| `active` | `Boolean` | default `true` |
| `createdAt` / `updatedAt` | `Long` | |

Index: `(active)`.

### 6.6 Preferences (DataStore)
- `currency: String` — ISO-4217, default `"INR"`.

### 6.7 Seed data (first launch)
**Groups (7):** Income `#10b981`, Bills `#ef4444`, Household `#f59e0b`, Debt `#dc2626`, Leisure `#8b5cf6`, Savings `#0ea5e9`, Other `#64748b` (in this `order`).
**Categories (6):** Income › Salary *(income)*; Bills › Rent *(expense)*; Bills › Electricity *(expense)*; Household › Groceries *(expense)*; Household › Transport *(expense)*; Leisure › Dining *(expense)*.

### Enums
- `Kind = { income, expense }`
- `Cadence = { monthly }`

---

## 7. Business Logic

### 7.1 Month handling (local timezone)
- `monthOf(epochMillis, zone) → "YYYY-MM"` using the **device local `ZoneId`**.
- `monthRange("YYYY-MM", zone) → [startInstant, endExclusiveInstant)` via `java.time.YearMonth` / `ZoneId`, used to query transactions (`date >= start AND date < endExclusive`).
- This differs from pathforge (UTC-anchored) by design — see §12.

### 7.2 Report aggregation
Given a month:
1. Load that month's transactions; sum `amount` per `categoryId` → **actual**.
2. Load that month's targets → map `categoryId → target`.
3. For each live group, collect live categories; **infer group kind** = `income` if *all* its live categories are income, else `expense` (mixed → expense).
4. Per category: `delta = actual − target`. Group actual/target = Σ of its categories.
5. **Totals:** `income` = Σ actuals of income-kind groups; `expense` = Σ actuals of expense-kind groups; `net = income − expense`. Compute `targetTotals` the same way over targets.
6. `recurringDue` = active templates whose `lastRunMonth != currentMonth`.

### 7.3 Narrative generation (deterministic, no LLM)
1. **No transactions:** `"In {Month YYYY}, no transactions were logged."`
2. **Transactions but no targets:** `"In {Month YYYY}, you spent {expenseActual} — no plan was set for this month."`
3. **Otherwise:**
   - `diff = expenseActual − expenseTarget`; direction = `over` if `diff > 0` else `under`; `pct = round(|diff| / expenseTarget × 100)`.
   - Find the expense category with the largest positive delta (**biggest overage**) and the largest `target − actual` (**biggest underspend**).
   - Compose: `"In {Month YYYY}, you spent {expenseActual} against a planned {expenseTarget} — {over|under} by {|diff|} ({pct}%). Biggest overage: {cat} ({actual} vs {target}). Biggest underspend: {cat} ({actual} vs {target})."` (Omit overage/underspend clauses when none apply.)

### 7.4 Recurring apply (single-device idempotency)
On Apply for template `t` in `currentMonth`:
- Reject if `!t.active` ("template inactive") or `t.lastRunMonth == currentMonth` ("already applied").
- In one DB transaction: insert a `Transaction` with `categoryId = t.categoryId`, `amount = t.amount`, `recurringTemplateId = t.id`, `date =` the instant for `dayOfMonth` (local, e.g. 09:00 local) of `currentMonth`; set `t.lastRunMonth = currentMonth`. Roll back together on failure.

---

## 8. Export Specification

### 8.1 Excel workbook — `budget-{YYYY-MM}-logs.xlsx`
**Sheet 1 — Transactions:** columns `Date | Group | Category | Kind | Amount | Description`; rows sorted by date ASC; footer totals — Income (Σ income rows), Expense (Σ expense rows), Net. Frozen header. Empty state: "No transactions in this month."

**Sheet 2 — Targets:** columns `(blank) | Group | Category | Kind | Target | Actual | Delta`; rows where target or actual ≠ 0, sorted by group then category; bold "Income totals" and "Expense totals" summary rows.

**Sheet 3 — Recurring:** columns `Label | Day of Month | Amount | Applied?`; sorted by day ASC; `Applied?` = Yes/No for the exported month.

**Currency formatting** (number format strings): INR `"₹"#,##0.00`, USD `"$"#,##0.00`, JPY `"¥"#,##0` (no decimals), EUR/GBP/AUD/CAD/SGD with their symbols; unknown codes → plain numeric. Negative in red.

### 8.2 PDF report — `budget-{YYYY-MM}-report.pdf`
- **Header (all pages):** "Budget Tracker" left, month label right, rule beneath.
- **Summary band:** three columns Income | Expense | Net (large bold), target amounts beneath in gray; Net green if ≥ target else red.
- **Narrative box:** light-gray background, wrapped text.
- **Group tables:** one per group — title (+ kind label), rows of Category / Target / Actual / Delta, a Subtotal row; deltas color-coded.
- **Recurring section** (if any due): Label / Day / Amount, sorted by day.
- **Footer (all pages):** "Generated {YYYY-MM-DD HH:MM}" (local time) left, "Page X of Y" right.
- **Font:** embed Noto Sans (regular + bold) for full glyph coverage (₹, Indic scripts).

---

## 9. Currency & Money Handling

- **Storage:** integer **minor units** in `Long` (e.g. ₹500.50 → `50050`). Range `0 .. 1_000_000_000_000`. Currency is *not* stored per amount; it is the single user preference.
- **Display:** `formatMoney(minor, currency)`:
  - major = `minor / 100`.
  - **Grouping:** INR uses `en-IN` (lakh/crore: `₹10,00,000`); others use `en-US` thousands.
  - **Decimals:** JPY → 0; others → 0–2 (omit when no fractional part).
  - **Symbols:** INR ₹, USD $, EUR €, GBP £, AUD A$, CAD C$, SGD S$, JPY ¥; unknown code → `"{CODE} "` prefix.
- **Examples:** `50000,INR → ₹500`; `50050,INR → ₹500.50`; `1000000,INR → ₹10,00,000`; `100000,JPY → ¥1,000`.

---

## 10. Non-Functional Requirements

- **Offline:** 100% functional with no network. No runtime network permission required for core features.
- **Reactivity:** screens observe Room via `Flow`; edits reflect immediately without manual refresh (the local equivalent of pathforge's TanStack Query invalidation).
- **Performance:** month switching and report aggregation feel instant for typical data (thousands of transactions); heavy work (export) runs off the main thread.
- **Privacy / data ownership:** data never leaves the device except via explicit user export/share. No analytics/telemetry that transmits financial data in v1.
- **Integrity:** money never represented as floating point; all arithmetic in `Long` minor units. Multi-step writes (recurring apply, target bulk-save) are atomic Room transactions.
- **Accessibility:** color-coded deltas also carry sign/text (not color alone); amount fields keyboard-friendly; Material 3 dynamic color / dark theme.
- **Resilience:** Room schema versioned with migrations; export failures surface a clear error, never a silent no-op.

---

## 11. Recommended Android Stack

| Concern | Choice | Notes |
|---------|--------|-------|
| Language | **Kotlin** | Coroutines + Flow throughout. |
| UI | **Jetpack Compose + Material 3** | Screens map 1:1 to web pages: Log, Plan, Report, Categories, Recurring, Settings. |
| Navigation | **Navigation-Compose** | Bottom nav or drawer across the six screens. |
| Persistence | **Room** (SQLite) | Entities per §6; DAOs return `Flow` for reactive UI; `@Transaction` for atomic writes. |
| Preferences | **Jetpack DataStore** | Currency setting. |
| State | **ViewModel + StateFlow** | Per-screen view models; repository layer over DAOs. |
| Date/time | **java.time** (`YearMonth`, `ZoneId`) | Local-TZ month math (§7.1). |
| Excel export | **Apache POI** (or a lighter XLSX writer such as FastExcel/dhatim) | POI is heavy; evaluate APK size — see §13. |
| PDF export | Android **PdfDocument** + Compose/Canvas, or **pdfbox-android** | Embed Noto Sans for glyph coverage. |
| Sharing | **Storage Access Framework** + `FileProvider` share sheet | "Save to Files" and share targets. |
| Min SDK | **API 26** (recommended) | Enables `java.time` without desugaring; or API 24 + core-library desugaring. |
| DI (optional) | Hilt | Optional; repositories are simple enough to wire manually. |

**Suggested module/package layout:** `data` (Room entities, DAOs, repository, DataStore) · `domain` (report aggregation, narrative, money/month utils) · `ui` (Compose screens + view models per feature) · `export` (xlsx/pdf builders).

---

## 12. Adaptations Log (pathforge web → Android local)

| # | pathforge web | Budget Tracker (Android) | Rationale |
|---|---------------|--------------------------|-----------|
| 1 | Every entity carries `userId`; queries filter by it | **Dropped** | Single user, single device — no tenancy boundary. |
| 2 | Money as JS `number` (≤10¹²) | **`Long`** minor units | Kotlin `Int` overflows ~₹21M; `Long` keeps the 10¹² ceiling. |
| 3 | Months anchored to **UTC** | **Device local timezone** | A late-night entry should fall in the user's local month. |
| 4 | Recurring: manual apply, DB race-safe | **Manual apply**, simple local "already applied" guard | Faithful; concurrency moot on one device. (Auto-apply deferred.) |
| 5 | Seed on first API read | **Seed on first launch** | No server. |
| 6 | Cookie session + `RequireAuth` route guards | **Omitted** (optional app-lock deferred) | No accounts in a local app. |
| 7 | Browser export (SheetJS/jsPDF) | **POI/PdfDocument + share sheet** | Same outputs, native delivery. |
| 8 | TanStack Query cache invalidation | **Room `Flow`** observation | Native reactive equivalent. |

---

## 13. Deferred / Future Scope

- **Auto-apply recurring** via WorkManager on `dayOfMonth` (flip Decision #4).
- **Cloud sync / backup & multi-device** (account + backend or BaaS).
- **Biometric / PIN app-lock** for privacy.
- **Home-screen widget** and **notifications/reminders** (e.g. "apply recurring", "log today").
- **Quick-add tile** / app-shortcut for one-tap transaction entry.
- **CSV import** and **JSON backup/restore** as additional portability formats.
- **Additional cadences** for recurring (bi-weekly, annual) — `Cadence` enum is already extensible.
- **Charts / trends** across months.

---

## Appendix A — Screen ↔ Feature Map

| Screen | Features | Key components |
|--------|----------|----------------|
| **Log** | F1, F8 | Month selector, input row + calculator popover, Net band, Income/Expense columns |
| **Plan** | F3 | Month selector, per-group target grid, carry-forward banner, Save |
| **Report** | F4, F6 | Narrative, group/category delta table, totals, export menu |
| **Categories** | F2 | Group list, category list, create/edit dialog, archive confirm, drag-reorder |
| **Recurring** | F5 | Template list with Apply, create/edit dialog, active toggle |
| **Settings** | F7 | Currency selector |
