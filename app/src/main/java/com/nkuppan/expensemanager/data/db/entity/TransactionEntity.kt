package com.nkuppan.expensemanager.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.nkuppan.expensemanager.domain.model.TransactionType
import java.util.Date


@Entity(
    tableName = "transaction",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("from_account_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("to_account_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("category_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "notes")
    val notes: String,
    @ColumnInfo(name = "category_id")
    var categoryId: String,
    @ColumnInfo(name = "from_account_id")
    var fromAccountId: String,
    @ColumnInfo(name = "type")
    var type: TransactionType,
    @ColumnInfo(name = "amount")
    var amount: Double,
    @ColumnInfo(name = "image_path")
    var imagePath: String,
    @ColumnInfo(name = "created_on")
    var createdOn: Date,
    @ColumnInfo(name = "updated_on")
    var updatedOn: Date,
    @ColumnInfo(name = "to_account_id")
    var toAccountId: String?,
)