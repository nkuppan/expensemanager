package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.naveenapps.expensemanager.core.model.ExchangeRateSource

/**
 * The permanent record of the rate a cross-currency transaction used: 1 [baseCode] =
 * [rate] [quoteCode]. Always the same shape whether it was cloned from a fetched rate
 * ([ExchangeRateSource.FETCHED], with the [provider] and fetch time) or entered by the user
 * ([ExchangeRateSource.USER]). Never deleted, so a transaction's conversion stays auditable.
 */
@Entity(tableName = "exchange_rate_snapshot")
data class ExchangeRateSnapshotEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "source")
    val source: ExchangeRateSource,
    @ColumnInfo(name = "provider")
    val provider: String?,
    @ColumnInfo(name = "base_code")
    val baseCode: String,
    @ColumnInfo(name = "quote_code")
    val quoteCode: String,
    @ColumnInfo(name = "rate")
    val rate: Double,
    @ColumnInfo(name = "captured_at")
    val capturedAt: Long,
)
