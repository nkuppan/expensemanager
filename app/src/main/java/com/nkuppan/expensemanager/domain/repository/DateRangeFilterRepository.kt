package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.DateRangeFilterType
import com.nkuppan.expensemanager.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface DateRangeFilterRepository {

    fun getDateRangeFilterType(): Flow<DateRangeFilterType>

    suspend fun setDateRangeFilterType(dateRangeFilterType: DateRangeFilterType): Resource<Boolean>

    suspend fun getDateRangeFilterRangeName(dateRangeFilterType: DateRangeFilterType): String

    suspend fun getDateRangeFilterTypeString(dateRangeFilterType: DateRangeFilterType): String

    suspend fun getDateRanges(dateRangeFilterType: DateRangeFilterType): List<Long>

    suspend fun setCustomDateRanges(customDateRanges: List<Date>): Resource<Boolean>

}