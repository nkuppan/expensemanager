package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "icon_background_color")
    val iconBackgroundColor: String,
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    /**
     * Selected month format will MM yyyy  e-x: 07 2023
     */
    @ColumnInfo(name = "selected_month")
    val selectedMonth: String,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "all_accounts_selected")
    val isAllAccountsSelected: Boolean,
    @ColumnInfo(name = "all_categories_selected")
    val isAllCategoriesSelected: Boolean,
    @ColumnInfo(name = "created_on")
    val createdOn: Date,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Date,
)
