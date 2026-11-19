package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/** A person in the debt ledger (see core:model DebtPerson). */
@Entity(tableName = "debt_person")
data class DebtPersonEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "created_on")
    val createdOn: Date,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Date,
)
