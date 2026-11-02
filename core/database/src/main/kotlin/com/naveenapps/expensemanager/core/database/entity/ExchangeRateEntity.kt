package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Temporary cache of the last-fetched rate for a base/target pair, per provider. Rows are
 * disposable (refreshed after the TTL, cleared when the provider changes); a rate that a
 * transaction actually uses is copied into [ExchangeRateSnapshotEntity] instead.
 */
@Entity(tableName = "exchange_rates", primaryKeys = ["provider", "base_code", "target_code"])
data class ExchangeRateEntity(
    @ColumnInfo(name = "provider")
    val provider: String,
    @ColumnInfo(name = "base_code")
    val baseCode: String,
    @ColumnInfo(name = "target_code")
    val targetCode: String,
    @ColumnInfo(name = "rate")
    val rate: Double,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long,
)
