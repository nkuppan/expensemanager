package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface DateRangeFilterRepository {

    suspend fun getAllDateRanges(): Resource<List<DateRangeModel>>

    fun getDateRangeFilterType(): Flow<DateRangeType>

    fun getDateRangeTimeFrame(): Flow<List<Long>?>

    suspend fun setDateRangeFilterType(dateRangeType: DateRangeType): Resource<Boolean>

    suspend fun getDateRangeFilterRangeName(dateRangeType: DateRangeType): String

    suspend fun getDateRangeFilterTypeString(dateRangeType: DateRangeType): DateRangeModel

    suspend fun setDateRanges(dateRanges: List<Date>): Resource<Boolean>

    suspend fun getTransactionGroupType(dateRangeType: DateRangeType): GroupType
}