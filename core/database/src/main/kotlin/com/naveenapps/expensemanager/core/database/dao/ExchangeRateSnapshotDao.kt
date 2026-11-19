package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.ExchangeRateSnapshotEntity

@Dao
interface ExchangeRateSnapshotDao {

    /** Snapshots are immutable audit records: inserting an existing id is a bug, so abort. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(snapshot: ExchangeRateSnapshotEntity)

    @Query("SELECT * FROM exchange_rate_snapshot WHERE id = :id")
    suspend fun findById(id: String): ExchangeRateSnapshotEntity?
}
