package com.naveenapps.expensemanager.core.database.utils

import androidx.room.TypeConverter
import com.naveenapps.expensemanager.core.model.TransactionType

object TransactionTypeConverter {

    @TypeConverter
    fun ordinalToTransactionType(value: Int?): TransactionType? {
        return value?.let { TransactionType.entries[it] }
    }

    @TypeConverter
    fun transactionTypeToOrdinal(transactionType: TransactionType?): Int? {
        return transactionType?.ordinal
    }
}
