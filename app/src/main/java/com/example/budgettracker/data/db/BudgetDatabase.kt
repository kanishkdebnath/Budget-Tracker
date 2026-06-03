package com.example.budgettracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.budgettracker.data.dao.CategoryDao
import com.example.budgettracker.data.dao.CategoryGroupDao
import com.example.budgettracker.data.dao.RecurringTemplateDao
import com.example.budgettracker.data.dao.TargetDao
import com.example.budgettracker.data.dao.TransactionDao
import com.example.budgettracker.data.entity.Category
import com.example.budgettracker.data.entity.CategoryGroup
import com.example.budgettracker.data.entity.RecurringTemplate
import com.example.budgettracker.data.entity.Target
import com.example.budgettracker.data.entity.TransactionEntity

@Database(
    entities = [
        CategoryGroup::class,
        Category::class,
        TransactionEntity::class,
        Target::class,
        RecurringTemplate::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun categoryGroupDao(): CategoryGroupDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun targetDao(): TargetDao
    abstract fun recurringTemplateDao(): RecurringTemplateDao
}
