package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import com.naveenapps.expensemanager.core.model.TransactionType

/** One group of identical transactions, as returned by TransactionDao.getFrequentTransactions. */
data class FrequentTransactionRow(
    @ColumnInfo(name = "type")
    val type: TransactionType,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "from_account_id")
    val fromAccountId: String,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "notes")
    val notes: String,
    @ColumnInfo(name = "use_count")
    val useCount: Int,
)
