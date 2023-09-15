package com.nkuppan.expensemanager.data.db.utils

import androidx.room.TypeConverter
import com.nkuppan.expensemanager.domain.model.CategoryType

class CategoryTypeConverter {

    @TypeConverter
    fun ordinalToCategoryType(value: Int?): CategoryType {
        return if (value == null) CategoryType.EXPENSE else CategoryType.values()[value]
    }

    @TypeConverter
    fun categoryTypeToOrdinal(categoryType: CategoryType?): Int {
        return categoryType?.ordinal ?: CategoryType.EXPENSE.ordinal
    }
}