package com.nkuppan.expensemanager.data.db.utils

import androidx.room.TypeConverter
import com.nkuppan.expensemanager.domain.model.TransactionType

class TransactionTypeConverter {

    @TypeConverter
    fun ordinalToTransactionType(value: Int?): TransactionType {
        return if (value == null) TransactionType.EXPENSE else TransactionType.values()[value]
    }

    @TypeConverter
    fun transactionTypeToOrdinal(transactionType: TransactionType?): Int {
        return transactionType?.ordinal ?: TransactionType.EXPENSE.ordinal
    }
}