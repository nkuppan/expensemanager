package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.naveenapps.expensemanager.core.database.entity.ExchangeRateEntity

@Dao
interface ExchangeRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<ExchangeRateEntity>)

    @Query("DELETE FROM exchange_rates WHERE provider = :provider AND base_code = :baseCode")
    suspend fun deleteBase(provider: String, baseCode: String)

    /** Replaces every cached rate for [baseCode], so pairs the provider dropped don't linger. */
    @Transaction
    suspend fun replaceBase(provider: String, baseCode: String, rates: List<ExchangeRateEntity>) {
        deleteBase(provider, baseCode)
        insertAll(rates)
    }

    @Query(
        "SELECT * FROM exchange_rates " +
            "WHERE provider = :provider AND base_code = :baseCode AND target_code = :targetCode",
    )
    suspend fun findRate(provider: String, baseCode: String, targetCode: String): ExchangeRateEntity?

    /** When [baseCode] was last fetched from [provider], or null if never (or cleared). */
    @Query(
        "SELECT MAX(fetched_at) FROM exchange_rates " +
            "WHERE provider = :provider AND base_code = :baseCode",
    )
    suspend fun lastFetchedAt(provider: String, baseCode: String): Long?

    /** Drops every cached rate not from [provider]; called when the user switches provider. */
    @Query("DELETE FROM exchange_rates WHERE provider != :provider")
    suspend fun deleteOtherProviders(provider: String)
}
