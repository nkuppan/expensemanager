package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date

/**
 * A repeating transaction rule (see core:model RecurringTransaction). Deleting the account or
 * category it uses deletes the rule too, the same way it deletes that account's transactions.
 */
@Entity(
    tableName = "recurring_transaction",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("from_account_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("to_account_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("category_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["from_account_id"]),
        Index(value = ["to_account_id"]),
        Index(value = ["category_id"]),
        Index(value = ["next_due_date"]),
    ],
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "notes")
    val notes: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "from_account_id")
    val fromAccountId: String,
    @ColumnInfo(name = "to_account_id")
    val toAccountId: String?,
    @ColumnInfo(name = "type")
    val type: TransactionType,
    @ColumnInfo(name = "amount")
    val amount: Double,
    /** RecurringFrequency name. Stored as text so new frequencies never need a migration. */
    @ColumnInfo(name = "frequency")
    val frequency: String,
    @ColumnInfo(name = "start_date")
    val startDate: Date,
    @ColumnInfo(name = "next_occurrence")
    val nextOccurrence: Int,
    @ColumnInfo(name = "next_due_date")
    val nextDueDate: Date,
    @ColumnInfo(name = "created_on")
    val createdOn: Date,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Date,
)
