package com.example.budgettracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A top-level grouping of categories (e.g. Bills, Leisure). PRODUCT_SPEC §6.1.
 * Name uniqueness (case-insensitive, among live groups) is enforced by CategoryRepository,
 * not a DB unique index — see the Phase 3 plan, decision #1.
 */
@Entity(
    tableName = "category_group",
    indices = [
        Index(value = ["archived", "order"]),
        Index(value = ["name"]),
    ],
)
data class CategoryGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: String,            // #RRGGBB
    val order: Int,               // display order, >= 0
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
