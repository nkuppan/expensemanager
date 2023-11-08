package com.naveenapps.expensemanager.domain.repository

import com.naveenapps.expensemanager.domain.model.DateRangeModel
import com.naveenapps.expensemanager.domain.model.DateRangeType
import com.naveenapps.expensemanager.domain.model.Resource
import com.naveenapps.expensemanager.domain.usecase.transaction.GroupType
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface DateRangeFilterRepository {

    suspend fun getAllDateRanges(): Resource<List<DateRangeModel>>

    fun getDateRangeFilterType(): Flow<DateRangeType>

    suspend fun setDateRangeFilterType(dateRangeType: DateRangeType): Resource<Boolean>

    suspend fun getDateRangeFilterRangeName(dateRangeType: DateRangeType): String

    suspend fun getDateRangeFilterTypeString(dateRangeType: DateRangeType): DateRangeModel

    suspend fun getAllDateRanges(dateRangeType: DateRangeType): List<Long>

    suspend fun setDateRanges(dateRanges: List<Date>): Resource<Boolean>

    suspend fun getTransactionGroupType(dateRangeType: DateRangeType): GroupType
}