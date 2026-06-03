package com.example.budgettracker.data.entity

/** Transaction / category direction (PRODUCT_SPEC §6 enums). Stored as TEXT via Converters. */
enum class Kind { INCOME, EXPENSE }

/** Recurring cadence. Only MONTHLY in v1; the enum is intentionally extensible (§13). */
enum class Cadence { MONTHLY }
