# Budget Tracker — Design System

**Status:** Approved, ready for implementation
**Date:** 2026-06-03
**Reference HTML:** `docs/design-system/{foundations,components,screens}.html`
**Drives:** [`PRODUCT_SPEC.md`](../../../PRODUCT_SPEC.md)

---

## 1. Overview

A design system for the offline-first Android budget tracker described in `PRODUCT_SPEC.md`. Material 3 Expressive, anchored to a navy brand color, with semantic green/red preserved for income/expense signal. Single-user, local-only — the design favors clarity of money flow over flashy chrome.

**Personality.** Trusted-bank navy as the voice; sea-green / coral-red as data signal; warm amber as the secondary accent for "info / due" callouts. Calm typography (Inter, tabular numerals everywhere money appears). Pill and 16dp card as the signature shapes. Subtle gradients on hero surfaces and CTAs to convey depth without becoming candy.

---

## 2. Decisions locked

| Decision | Choice | Rationale |
|---|---|---|
| **Design direction** | Material 3 Expressive (dark default, system follows) | Native Android feel; tonal flexibility |
| **Brand color** | `#0d2736` (deep navy) | User-supplied identity |
| **Color approach** | Hybrid — fixed brand palette default + Settings toggle for dynamic color (Android 12+) | Brand recognition + personalization |
| **Theme** | Follows system OS setting | Modern Android standard |
| **Font** | Inter, with system fallback | Crisp tabular numerals |
| **Navigation** | Bottom nav, 5 tabs (Log · Plan · Report · Categories · Recurring) + Settings gear in top app bar | Daily/weekly screens one tap away; Settings rarely visited |
| **Density** | Comfortable default (52dp rows); Settings exposes Compact (44dp) | Accessibility + power-user option |

---

## 3. Foundations

### 3.1 Color tokens

**Light theme** (`docs/design-system/foundations.html` — palette §1)

| Token | Hex | Role |
|---|---|---|
| `primary` / brand | `#0d2736` | Top bar, FAB chrome, active states |
| `onPrimary` | `#FFFFFF` | Text on primary |
| `primaryContainer` | `#BEDCEC` | Tonal surfaces (Net band, banners, tonal buttons) |
| `onPrimaryContainer` | `#001E2C` | Text on primary container |
| `secondary` | `#4B5C66` | Muted slate accents |
| `tertiary` | `#B07B3A` | Warm amber — Plan banners, recurring-due highlight |
| `tertiaryContainer` | `#FFDDB6` | Tonal amber surface |
| `error` / overage | `#B3261E` | Over-budget delta, destructive actions |
| `errorContainer` | `#FFDAD4` | Error tonal surface |
| `background` | `#F4F8FA` | Screen background |
| `surface` | `#FFFFFF` | Cards |
| `surfaceVariant` | `#E6ECEE` | Inputs, surface-2 |
| `outline` | `#6F7E87` | 1px borders |
| `onSurface` | `#0E141A` | Body text |
| `onSurfaceVariant` | `#404951` | Secondary text |

**Dark theme** (first-class, not a derivative)

| Token | Hex | Role |
|---|---|---|
| `primary` / brand-light | `#9CC8DE` | Active states in dark mode |
| `onPrimary` | `#003547` | Text on primary |
| `primaryContainer` | `#143548` | Net band, banners, tonal buttons |
| `onPrimaryContainer` | `#C5E5F9` | Text on primary container |
| `secondary` | `#B5C5CF` | Muted slate accents |
| `tertiary` | `#FFB68D` | Warm amber accent |
| `tertiaryContainer` | `#6A3B16` | Tonal amber surface |
| `error` / overage | `#FFB4A8` | Over-budget delta |
| `errorContainer` | `#93000A` | Error tonal surface |
| `background` | `#0A1218` | Screen background |
| `surface` | `#11191F` | Cards (top of subtle gradient) |
| `surface-2` | `#1A2229` | Inputs, secondary surfaces |
| `outline` | `#7C8A93` | 1px borders |
| `onSurface` | `#E0E7EC` | Body text |
| `onSurfaceVariant` | `#C0C9D0` | Secondary text |

**Semantic shortcuts** (separate from brand — never confused with chrome):

| Token | Light | Dark | Role |
|---|---|---|---|
| `income` | `#1F7A5A` | `#74D9B5` | Positive amounts, Inc kind chip, applied state, positive delta |
| `expense` | `--text` | `--text` | Neutral money out — uses text color, not a tint |
| `overage` | `#B3261E` | `#FFB4A8` | Over-budget delta only — distinct from "expense kind" |

**Inc / Exp kind chips** (group cards):

| Variant | Bg | Text |
|---|---|---|
| `.grp-kind.income` | `rgba(116, 217, 181, 0.14)` | `--income` |
| `.grp-kind.expense` | `rgba(255, 180, 168, 0.12)` | `--overage` |

**Seed group colors** (from PRODUCT_SPEC §6.7, used as 3dp accent bars and 8dp dots only, never as full backgrounds): Income `#10b981`, Bills `#ef4444`, Household `#f59e0b`, Debt `#dc2626`, Leisure `#8b5cf6`, Savings `#0ea5e9`, Other `#64748b`.

### 3.2 Typography

**Font family:** Inter (variable), system fallback (`-apple-system`, `system-ui`, `sans-serif`). **Tabular numerals** (`font-variant-numeric: tabular-nums`) are mandatory anywhere money appears.

| Style | Size / Line | Weight | Letter-spacing | Use |
|---|---|---|---|---|
| Display L | 36 / 44 | 600 | −0.022em | Empty states, onboarding |
| Headline M | 24 / 30 | 600 | −0.012em | Screen titles, narrative |
| Title L | 20 / 26 | 600 | 0 | Group headers, dialog titles |
| Title M | 16 / 22 | 600 | 0 | Row primary, card title |
| Body L | 16 / 24 | 400 | 0 | Narrative, description |
| Body M | 14 / 20 | 400 | 0 | Secondary text, date, note |
| Label L | 14 / 20 | 500 | 0 | Buttons, chips |
| Label M | 12 / 16 | 500 | +0.04em (upper) | Section headers, meta |
| Money | 18 (or context) | 500 | −0.01em | Every monetary amount; tabular nums |

### 3.3 Spacing & shape

**4dp base grid.** Tokens: `xs=4 · sm=8 · md=12 · lg=16 · xl=20 · 2xl=24 · 3xl=32 · 4xl=40`.

- Screen padding: 16dp.
- Between-section gap: 24dp.
- Row internal padding: 12dp.
- Touch targets ≥ 48dp (comfortable density); Settings exposes 44dp (compact).

**Radius scale:** `4` (chip-tight) · `8` (input) · `12` (row) · `16` (card) · `20` (sheet) · `999px` (pill / FAB).

**Shape language:**
- **Pill** — FAB, primary CTAs, bottom-nav active indicator, chips. Signature shape.
- **Card 16** — transaction list, group cards, narrative box, settings tiles.
- **Sheet 20** — bottom sheet (calculator, edit dialog, currency picker).
- **Input 8** — text fields, amount input, search.

### 3.4 Iconography

**Material Symbols Rounded** (variable font). Sizes: 24dp default, 20dp in dense rows, 28dp in headers. Filled for active nav state; outlined for inactive — standard M3.

Custom inline SVGs used in mockups for: bullseye (Plan / target), check (applied state), drag grip (reorder).

### 3.5 Motion

M3 emphasized easing. Snappy enough for fast logging, slow enough that "applied" feedback registers.

| Track | Duration | Easing | Use |
|---|---|---|---|
| Standard | 150ms | easeStandard | Toggle switches, chip selection, FAB scale, focus |
| Emphasized | 300ms | easeEmphasized | Screen transitions, bottom sheet enter, calculator open |
| Decelerate | 200ms | easeOut | Snackbar enter, narrative reveal, row insert after add |

### 3.6 Gradients (tasteful, restrained)

Gradients are used **only** for surfaces and interactive containers — never for data. Reference HTML: `docs/design-system/screens.html` §13 changelog.

| Surface | Gradient |
|---|---|
| Net band (hero) | `linear-gradient(135deg, #1B4561 → #143548 → #0F2A3D)` + radial top-left brand-light glow + inset top highlight + drop shadow |
| FAB | `linear-gradient(135deg, #2A5E80 → #1B4561 → #143548)` + inner highlight + navy-tinted drop shadow |
| Tonal button / bottom-nav active pill | `linear-gradient(180deg, #1B4561 → #143548)` + inset top highlight |
| Filled button (primary) | `linear-gradient(180deg, #B0DDED → #9CC8DE)` + inset top highlight |
| Card surfaces (txn / grp / rec / narrative / settings-group) | `linear-gradient(180deg, #131A20 → #0F1619)` (3% lift at top) |
| Banner info | `linear-gradient(135deg, #1B4561 → #143548)` |
| Banner amber | `linear-gradient(135deg, #7B4520 → #5C3013)` |
| Phone background | radial `rgba(28, 75, 105, 0.35)` ellipse from `(50%, −10%)`, fades to `--background` |
| Applied recurring | `linear-gradient(180deg, rgba(116, 217, 181, 0.10) → 0.04)` + green border tint |

---

## 4. Components

Full catalogue in `docs/design-system/components.html`. Highlights:

### Buttons
- **Filled** — primary CTA (gradient + inset highlight). Used for Save targets, Add transaction, Share.
- **Tonal** — softer secondary CTA (primary-container gradient). Used for Apply (Recurring), Excel/PDF export buttons.
- **Outline** — Cancel, dismissive.
- **Text** — Skip, low-priority dismissive.
- **Error** — destructive confirm only (Archive sheet).
- **Disabled** — flat surface-2 + muted text.
- **Icon button** — 40dp pill hit area; transparent default, surface-2 on hover.
- **FAB** — 18dp rounded with extended label ("+ Add"). 48dp icon-only variant for secondary screens.

### Chips & delta badges
- **Filter chip** — pill, 5px 10px padding, selected state uses primary-container.
- **Group color dot** — 8dp rounded-3dp square using the seed group color (never as background).
- **Kind chip** — `Inc` (green tint) / `Exp` (coral tint) — semantic identifier on group cards.
- **Delta badge** — `+₹500` over (overage red), `−₹500` under (income green), `0` flat (surface-2 + muted). Sign-prefixed text so color isn't load-bearing.

### Inputs
- **Text field** — surface-2 fill, 8dp radius, transparent border (focus → primary border + surface bg).
- **Amount field** — currency prefix glyph + tabular monospace input + integrated calculator trigger button.
- **Switch** — 28dp track with on-primary thumb.

### Data display
- **Net band** — hero element on Log / Plan / Report. 3-column grid (Income · Expense · Net). Plan / Report variants include a Plan sub-row with the bullseye (◎) icon.
- **Transaction card** — 16dp gradient surface, rows with 3dp leading color bar tied to group color, title + sub on left, amount on right.
- **Group card** — head (dot + name + Inc/Exp chip + subtotal), then context-dependent rows: target-vs-actual on Report, target input on Plan, name + reorder grip on Categories.
- **Target-vs-Actual row** (Report) — `1fr 72px 64px 64px` grid. Plan column muted with inline bullseye icon; Actual column high-contrast 600 weight; Δ column right-aligned delta badge. All columns land at identical x across every group card.
- **Plan input row** — `1fr 110px` grid with editable amount input on the right; empty = "no target."
- **Recurring template card** — 16dp gradient. Three states: **applied** (green-tinted bg + check pill on right), **actionable** (Apply tonal button), **inactive** (50% opacity + disabled pill).

### Chrome
- **Top app bar** — month nav (Log / Plan / Report) with chevrons + label; section title + Settings gear elsewhere.
- **Bottom navigation** — 5 tabs with pill active indicator (primary-container gradient + inset highlight).
- **Sticky save bar** — appears above bnav on Plan (Save targets).
- **Banner** — info (navy gradient) for pre-fill state, amber (warm gradient) for due-this-month nudges.
- **Narrative box** — card-styled with `Summary · {Month}` label and 1–3 sentence body. Color used only on the over/under figure.

### Modals / overlays
- **Bottom sheet** — 20dp top corners; handle bar; used for calculator, currency picker, transaction edit, destructive confirms.
- **Calculator popover** — 4×5 keypad with display showing live expression. Op keys use primary color; equals uses filled primary span-2 row.
- **Snackbar** — 12dp surface-3 pill, 360px max width. Standard text + optional action label.
- **Empty state** — thin glyph + title + sub + optional CTA. Warm tone, never chastising.

---

## 5. Screens

Full mockups in `docs/design-system/screens.html`.

### 5.1 Log
**Composition:** month nav top bar → Net band → filter chips (All / Income / Expense with counts) → one group card per date (header: `Jun 5 · Thu · Net −₹6.8k`, rows: 3dp color bar + title/sub + amount). FAB `+ Add`.

**Notes:**
- "Two columns" from PRODUCT_SPEC F1.2 reinterpreted as Income/Expense filter chips on a unified list — see Adaptations §6.
- Each date is its own card to match the Plan screen's group rhythm; cards stack and scroll.

### 5.2 Plan
**Composition:** month nav → carry-forward banner ("Pre-filled from May's targets") → target-totals Net band (`◎ Income / Expense / Net`) → one group card per group with editable amount inputs per category. Sticky `Save targets` bar above bnav.

### 5.3 Report
**Composition:** month nav (+ export icon) → narrative box → target-augmented Net band → recurring-due amber banner (if applicable) → per-group cards with Plan/Actual/Δ table rows (bullseye on Plan, bold high-contrast Actual). Export row at end: Excel · PDF · Share.

### 5.4 Categories
**Composition:** title bar (with search + Settings gear) → group filter chips (All / Income / Expense / Archived) → expanded group cards with reorder grip on the right side of each header and each row. FAB `+ New`.

### 5.5 Recurring
**Composition:** title bar → due-this-month amber banner → Active section (sort: applied first, then actionable) → Inactive section. Every card has exactly one element on the right: applied check-pill, Apply tonal button, or Inactive disabled pill. FAB `+ New`.

### 5.6 Settings
**Composition:** back-arrow + title → grouped tiles:
- **Money** — Currency (INR · ₹, opens ISO-4217 picker)
- **Appearance** — Theme (System), Dynamic color (switch, Android 12+), Density (Comfortable / Compact)
- **Data** — Export current month
- **About** — Version, Privacy

---

## 6. Adaptations from PRODUCT_SPEC

| # | PRODUCT_SPEC requirement | Design choice | Rationale |
|---|---|---|---|
| 1 | F1.2: Log lists transactions in **two columns** (Income / Expense) | Single unified list with `All / Income / Expense` filter chips + per-date group cards | Original pathforge had a wide web canvas; on portrait phone, side-by-side columns of money rows become cramped. Same information surface, mobile-idiomatic. |
| 2 | F2.6: Drag-to-reorder groups and categories | Reorder grip (six-dot icon) on the **right** side of each row and group header | Material 3 pattern; also keeps the leading edge clean for category color + name |
| 3 | F4: Report shows target-vs-actual per category | Plan/Actual/Δ table with bullseye icon ◎ on Plan column, bold actual, sign-coded delta | Eliminates "which number is which" ambiguity; icon doubles as legend |
| 4 | F5.4: Idempotency guard on Recurring apply | Whole-card green-tinted background + green check pill (no separate text) | State is communicated by surface + icon, not extra prose |
| 5 | F7: Single currency, locale-aware grouping | Display layer renders short notation (`₹85k`, `₹32.5k`) in Net bands and group subtotals; full notation in transaction rows and the narrative | Net band space-constrained; precision matters where you're auditing individual items |

These also extend the PRODUCT_SPEC §12 "Adaptations Log" — append rows 9–13 to that table when convenient.

---

## 7. Implementation notes

### 7.1 Compose mapping

- **Color tokens** → `MaterialTheme.colorScheme` with custom `ColorScheme` for both light/dark. Semantic shortcuts (`income`, `overage`, `expense`) live in a sibling `SemanticColors` object via `CompositionLocalProvider`.
- **Typography** → Custom `Typography` instance using Inter (bundled via Fonts API or a packaged TTF). `displayLarge / headlineMedium / titleLarge / titleMedium / bodyLarge / bodyMedium / labelLarge / labelMedium` per M3; add `money` style via extension property.
- **Shapes** → Custom `Shapes` with `extraSmall=4 / small=8 / medium=16 / large=20 / extraLarge=999.dp` (using `CircleShape` for pill).
- **Gradients** → Implement as `Brush.linearGradient` / `Brush.radialGradient` on `Modifier.background(brush, shape)`. Inset highlights via a second `drawWithContent` layer.

### 7.2 Density mode

A single `LocalDensityMode.current` `(Comfortable | Compact)` switches:
- Row min-height: 52 / 44dp
- Body text scale: 1.0 / 0.95
- Padding: 12 / 10dp

Settings → Appearance → Density flips it; stored in DataStore alongside currency.

### 7.3 Dynamic color toggle

Settings → Appearance → "Dynamic color" `(off | on)`. When on (Android 12+), use `dynamicLightColorScheme(LocalContext.current)` / `dynamicDarkColorScheme(...)`. When off, use the fixed navy schemes from §3.1. Semantic shortcuts (income green / overage red / Inc-Exp chips) remain fixed regardless — they're data signal, not chrome.

### 7.4 Accessibility

- All delta values carry sign prefix (`+` / `−`) and qualifier text ("over by", "under"), so color is never the only signal.
- Contrast tested: Inc chip on surface ≥ 4.5:1; Exp chip on surface ≥ 4.5:1.
- Touch targets ≥ 48dp on Comfortable. Drag grips have 24dp tap surface even though glyph is 14dp.
- Tabular numerals so screen-reader navigation lands consistently.

---

## 8. HTML reference deliverable

Three companion files live in `docs/design-system/`:

| File | Contents |
|---|---|
| `foundations.html` | Color palette (light + dark + tonal scale derived from brand), type scale, spacing / radius tokens, shape language tiles, iconography, motion tracks |
| `components.html` | Buttons / chips / inputs / Net band / transaction row / group card / target-vs-actual / plan input / recurring / banners / narrative / top bar / bottom nav / calculator / sheets / snackbars / empty states |
| `screens.html` | All six screens (Log / Plan / Report / Categories / Recurring / Settings) at phone proportions, with the iteration history visible in the changelog header |

Open any file in a browser to interact with the design. The HTML uses the same tokens that should be mirrored in Compose; the CSS gradient recipes copy 1:1 into `Brush` calls.

---

## 9. Open items / deferred

These are out of scope for this design system but worth flagging for downstream iteration:

- **Charts / trends** (PRODUCT_SPEC §13 deferred) — no design yet for multi-month visualizations. Add when the requirement is firmer.
- **Onboarding flow** — design treats first-launch as plain empty states. If a guided tour is wanted later, a `SuperpowersAnnotation`-style overlay would slot into the Sheet shape language.
- **Compact density visual** — defined in tokens but not mocked side-by-side. Implement, then design-review against Comfortable.
- **Pre-launched app shortcuts / home widget** (PRODUCT_SPEC §13) — would need a dedicated mini design pass.

---

## 10. Iteration history

Built iteratively in `docs/design-system/screens.html` from v1 → v15. Notable inflection points:

1. **v1** — Material 3 Expressive direction confirmed; navigation locked to 5-tab + Settings gear.
2. **v2** — Brand color set to `#0d2736` (navy); semantic green/red kept separate from brand.
3. **v4** — Log restructured to one card per date (matches Plan rhythm).
4. **v5** — Short money notation in Net bands; bullseye icon replaces "tgt" text; ellipsis safety everywhere.
5. **v6** — Applied recurring state restyled (green-tinted card surface).
6. **v7** — Inc / Exp pills colored semantically.
7. **v8** — Body scroll bug fix (flex children were shrinking instead of overflowing).
8. **v10–11** — Report Plan/Actual disambiguation via inline icon + visual hierarchy.
9. **v12** — Fixed-width Report columns for true column-alignment.
10. **v13** — Tasteful gradients on hero + CTAs + card surfaces.
11. **v14** — Categories rebalanced (drag handle right, per-row Inc/Exp chip dropped).
12. **v15** — Recurring rebalanced (3-dot overflow dropped, single trailing element per card).

Each iteration's changelog is preserved in the `screens.html` header for traceability.
