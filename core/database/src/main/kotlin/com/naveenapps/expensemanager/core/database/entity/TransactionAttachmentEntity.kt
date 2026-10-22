package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * A single photo (receipt, proof of payment, etc.) attached to a transaction. Unlike Category's
 * or Account's single `custom_image_path` column, a transaction can have any number of these, so
 * they live in their own child table rather than a column on `transaction` — the same one-to-many
 * shape already used for `budget_category_relation`/`budget_account_relation`.
 */
@Entity(
    tableName = "transaction_attachment",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("transaction_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["transaction_id"])],
)
data class TransactionAttachmentEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    @ColumnInfo(name = "image_path")
    val imagePath: String,
    @ColumnInfo(name = "created_on")
    val createdOn: Date,
)
