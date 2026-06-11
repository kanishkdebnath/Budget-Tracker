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
