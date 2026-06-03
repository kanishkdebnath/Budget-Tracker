# Phase 9 — Recurring (F5) — Implementation Plan

> REQUIRED SUB-SKILL: superpowers:executing-plans.

**Goal:** Recurring templates — list with one-tap idempotent apply, create/edit, active toggle (the atomic `RecurringRepository.apply` from Phase 3 gets its UI).

**Architecture:** `RecurringViewModel` combines templates + categories into `RecurringSections` via a pure `buildRecurringRows` (classifies each template applied/actionable/inactive vs the **current real month** and sorts active applied-first). `apply` calls the atomic `RecurringRepository.apply(id, currentMonth, zone)` (§7.4). Screen shows a due banner, Active/Inactive sections, 3-state cards, and a create/edit sheet.

## Files
- `data/repository/RecurringRepository.kt` — `update` + `setActive`.
- `ui/screens/recurring/RecurringViewModel.kt` — `RecurringState`, `RecurringRow`, `RecurringSections`, pure `buildRecurringRows`, VM (apply/create/update/toggleActive/delete; current month from injected clock).
- `ui/screens/recurring/RecurringComponents.kt` — `RecurringDueBanner`, `SectionHeader`, `RecurringCard` (applied = green tint + check pill; actionable = Apply; inactive = dimmed + pill).
- `ui/screens/recurring/RecurringSheet.kt` — scrollable create/edit form (label, category, amount, day 1–28, active switch, delete).
- `ui/screens/recurring/RecurringScreen.kt` — assembled screen.
- `ui/AppViewModelProvider.kt` — `RecurringViewModel` initializer.
- test: `RecurringRowsTest` (classification + sort + due count).

## Verification (done)
- `./gradlew test` green; `:app:assembleDebug` green.
- Emulator: created "Salary · Day 1 · ₹5,000" → due banner + Apply; tapped Apply → card went green **✓ Applied**, banner cleared (idempotency — no re-apply); Log showed a new **Salary +₹5,000 on Jun 1** and Net band → ₹6,500.

## Notes
- Found & fixed a sheet bug: the form was taller than the sheet and not scrollable (Save off-screen) → added `verticalScroll`.

## Self-Review
- **Spec coverage:** F5.1–F5.5 (templates, sort, one-tap apply to current month, idempotency guard via the Phase 3 atomic op, create/edit/delete, active toggle). Apply uses the current real month, not the viewed month (§7.4). Single trailing element per card (design §5.5).
