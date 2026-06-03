package com.example.budgettracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A monthly spending/earning target for a category. PRODUCT_SPEC §6.4.
 * At most one target per (month, category) — a true DB unique index (powers upsert-on-conflict).
 */
@Entity(
    tableName = "target",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["month", "categoryId"], unique = true),
        Index(value = ["categoryId", "month"]),
    ],
)
data class Target(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val month: String,            // YYYY-MM
    val amount: Long,             // minor units, >= 0
    val createdAt: Long,
    val updatedAt: Long,
)
