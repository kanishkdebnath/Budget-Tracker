package com.example.budgettracker.data

/** Result of a guarded write that can fail a business rule (vs. throwing). */
sealed interface OpResult {
    data class Success(val id: Long = 0L) : OpResult
    data class Failure(val reason: String) : OpResult
}
