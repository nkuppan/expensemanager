package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.model.Resource
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

interface DateRangeFilterRepository {

    suspend fun getAllDateRanges(): Resource<List<DateRangeModel>>

    fun getDateRangeFilterType(): Flow<DateRangeType>

    fun getDateRangeTimeFrame(): Flow<List<Long>?>

    suspend fun setDateRangeFilterType(dateRangeType: DateRangeType): Resource<Boolean>

    suspend fun getDateRangeFilterRangeName(dateRangeType: DateRangeType): String

    suspend fun getDateRangeFilterTypeString(dateRangeType: DateRangeType): DateRangeModel

    /** Stores a custom range: [start, end] as instants (persisted as epoch milliseconds). */
    suspend fun setDateRanges(dateRanges: List<Instant>): Resource<Boolean>

    suspend fun getTransactionGroupType(dateRangeType: DateRangeType): GroupType
}
