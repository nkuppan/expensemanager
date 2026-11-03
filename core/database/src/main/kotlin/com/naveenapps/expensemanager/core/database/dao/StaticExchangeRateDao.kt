package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.naveenapps.expensemanager.core.database.entity.StaticExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaticExchangeRateDao {

    @Query("SELECT * FROM static_exchange_rate ORDER BY base_code, quote_code")
    fun getAll(): Flow<List<StaticExchangeRateEntity>>

    /** The pair's record, whichever way round it was entered. */
    @Query(
        "SELECT * FROM static_exchange_rate WHERE " +
            "(base_code = :firstCode AND quote_code = :secondCode) OR " +
            "(base_code = :secondCode AND quote_code = :firstCode) LIMIT 1",
    )
    suspend fun findPair(firstCode: String, secondCode: String): StaticExchangeRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rate: StaticExchangeRateEntity)

    @Query(
        "DELETE FROM static_exchange_rate WHERE " +
            "(base_code = :firstCode AND quote_code = :secondCode) OR " +
            "(base_code = :secondCode AND quote_code = :firstCode)",
    )
    suspend fun deletePair(firstCode: String, secondCode: String)

    /** A pair has exactly one record: the reversed one is removed before saving. */
    @Transaction
    suspend fun replacePair(rate: StaticExchangeRateEntity) {
        deletePair(rate.baseCode, rate.quoteCode)
        insert(rate)
    }
}
