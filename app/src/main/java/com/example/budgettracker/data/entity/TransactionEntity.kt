package com.example.budgettracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An income or expense entry. PRODUCT_SPEC §6.3. Table is `transactions` (TRANSACTION is a SQLite
 * keyword); class is TransactionEntity to avoid colliding with Room's @Transaction.
 * Hard-delete only. `amount` is Long minor units, > 0.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
        ),
        ForeignKey(
            entity = RecurringTemplate::class,
            parentColumns = ["id"],
            childColumns = ["recurringTemplateId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["categoryId", "date"]),
        Index(value = ["recurringTemplateId"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,                       // epoch millis; month derived in local TZ
    val categoryId: Long,
    val amount: Long,                     // minor units, > 0
    val description: String? = null,      // <= 500 chars
    val recurringTemplateId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
