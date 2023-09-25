package com.nkuppan.expensemanager.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nkuppan.expensemanager.domain.model.AccountType
import java.util.Date


@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: AccountType,
    @ColumnInfo(name = "icon_background_color")
    val iconBackgroundColor: String,
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "created_on")
    val createdOn: Date,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Date
)