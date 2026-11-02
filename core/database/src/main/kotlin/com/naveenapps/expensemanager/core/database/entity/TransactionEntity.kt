package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date

@Entity(
    tableName = "transaction",
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
            entity = ExchangeRateSnapshotEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("rate_snapshot_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("category_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    // Every list, filter and total query filters or sorts on these; see MIGRATION_9_10.
    // rate_snapshot_id backs the transaction -> exchange_rate_snapshot foreign key.
    indices = [
        Index(value = ["created_on"]),
        Index(value = ["from_account_id"]),
        Index(value = ["to_account_id"]),
        Index(value = ["category_id"]),
        Index(value = ["rate_snapshot_id"]),
    ],
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
    /** Currency of [amount]: always the from-account's currency. */
    @ColumnInfo(name = "from_currency_code")
    var fromCurrencyCode: String,
    /**
     * Currency credited to the to-account. Equal to [fromCurrencyCode] for income/expense and
     * same-currency transfers; only a cross-currency transfer differs.
     */
    @ColumnInfo(name = "to_currency_code")
    var toCurrencyCode: String,
    /**
     * The rate used when [fromCurrencyCode] != [toCurrencyCode]; null means no conversion (1.0).
     * The to-account is credited `amount * snapshot.rate`.
     */
    @ColumnInfo(name = "rate_snapshot_id")
    var rateSnapshotId: String? = null,
)
