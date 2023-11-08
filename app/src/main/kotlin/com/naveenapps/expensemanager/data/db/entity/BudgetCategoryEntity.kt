package com.naveenapps.expensemanager.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "budget_category_relation",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("budget_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("category_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class BudgetCategoryEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "budget_id")
    val budgetId: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "created_on")
    val createdOn: Date,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Date
)