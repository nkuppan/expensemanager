package com.naveenapps.expensemanager.data.db.utils

import androidx.room.TypeConverter
import com.naveenapps.expensemanager.domain.model.AccountType


object AccountTypeConverter {

    @TypeConverter
    fun ordinalToAccountType(value: Int?): AccountType? {
        return value?.let { AccountType.values()[value] }
    }

    @TypeConverter
    fun accountTypeToOrdinal(accountType: AccountType?): Int? {
        return accountType?.ordinal
    }
}