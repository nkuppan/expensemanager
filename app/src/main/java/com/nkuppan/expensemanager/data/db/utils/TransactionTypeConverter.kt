package com.nkuppan.expensemanager.data.db.utils

import androidx.room.TypeConverter
import com.nkuppan.expensemanager.domain.model.TransactionType

object TransactionTypeConverter {

    @TypeConverter
    fun ordinalToTransactionType(value: Int?): TransactionType? {
        return value?.let { TransactionType.values()[it] }
    }

    @TypeConverter
    fun transactionTypeToOrdinal(transactionType: TransactionType?): Int? {
        return transactionType?.ordinal
    }
}