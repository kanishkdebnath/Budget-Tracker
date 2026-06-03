package com.example.budgettracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A monthly recurring entry template, applied manually & idempotently. PRODUCT_SPEC §6.5 / §7.4.
 * `dayOfMonth` is 1–28 (avoids Feb 29+ edge cases).
 */
@Entity(
    tableName = "recurring_template",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
        ),
    ],
    indices = [
        Index(value = ["active"]),
        Index(value = ["categoryId"]),
    ],
)
data class RecurringTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,            // 1–120 chars
    val categoryId: Long,
    val amount: Long,             // minor units, >= 0
    val cadence: Cadence = Cadence.MONTHLY,
    val dayOfMonth: Int,          // 1–28
    val lastRunMonth: String? = null, // YYYY-MM, last applied month
    val active: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
)
