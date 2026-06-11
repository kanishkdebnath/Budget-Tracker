package com.example.budgettracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A category within a group, with a kind and optional color. PRODUCT_SPEC §6.2.
 * Name uniqueness (case-insensitive, among live categories) is enforced by CategoryRepository.
 */
@Entity(
    tableName = "category",
    foreignKeys = [
        ForeignKey(
            entity = CategoryGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["groupId", "archived", "order"]),
        Index(value = ["name"]),
        Index(value = ["archived", "kind"]),
    ],
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val name: String,
    val kind: Kind,
    val color: String? = null,    // optional #RRGGBB
    val icon: String? = null,     // optional CategoryIcons registry key, e.g. "restaurant"
    val order: Int,               // per-group order
    val archived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
