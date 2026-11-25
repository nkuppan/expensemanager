package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant

/**
 * One lent / borrowed / repaid entry (see core:model DebtEntry). Deleting the person deletes
 * their entries (DebtDao.deletePerson reverses account effects first). Deleting a linked
 * account keeps the entry and only clears the link.
 *
 * `amount_milli` is exact (thousandths of the currency unit), unlike the older REAL columns.
 * `kind` is stored by name so the enum can be reordered safely.
 */
@Entity(
    tableName = "debt_entry",
    foreignKeys = [
        ForeignKey(
            entity = DebtPersonEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("person_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("account_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["person_id"]),
        Index(value = ["account_id"]),
        Index(value = ["due_date"]),
    ],
)
data class DebtEntryEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "person_id")
    val personId: String,
    @ColumnInfo(name = "kind")
    val kind: String,
    @ColumnInfo(name = "amount_milli")
    val amountMilli: Long,
    @ColumnInfo(name = "account_id")
    val accountId: String?,
    @ColumnInfo(name = "date")
    val date: Instant,
    @ColumnInfo(name = "due_date")
    val dueDate: Instant?,
    @ColumnInfo(name = "note")
    val note: String,
    @ColumnInfo(name = "created_on")
    val createdOn: Instant,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Instant,
)
