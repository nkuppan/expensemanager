package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * The user's fixed rate for a currency pair: 1 [baseCode] is bought for [buy] and sold for
 * [sell] [quoteCode]. One row per pair; the repository keeps the reversed pair from existing.
 */
@Entity(tableName = "static_exchange_rate", primaryKeys = ["base_code", "quote_code"])
data class StaticExchangeRateEntity(
    @ColumnInfo(name = "base_code")
    val baseCode: String,
    @ColumnInfo(name = "quote_code")
    val quoteCode: String,
    @ColumnInfo(name = "buy")
    val buy: Double,
    @ColumnInfo(name = "sell")
    val sell: Double,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Long,
)
