package com.naveenapps.expensemanager.core.database.utils

import androidx.room.TypeConverter
import com.naveenapps.expensemanager.core.model.AccountType

object AccountTypeConverter {

    @TypeConverter
    fun ordinalToAccountType(value: Int?): AccountType? {
        return value?.let { AccountType.entries[value] }
    }

    @TypeConverter
    fun accountTypeToOrdinal(accountType: AccountType?): Int? {
        return accountType?.ordinal
    }
}
