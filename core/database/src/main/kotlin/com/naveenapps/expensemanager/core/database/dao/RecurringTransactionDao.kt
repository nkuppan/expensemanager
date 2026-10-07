package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface RecurringTransactionDao : BaseDao<RecurringTransactionEntity> {

    @Query("SELECT * FROM recurring_transaction ORDER BY next_due_date ASC")
    fun getAll(): Flow<List<RecurringTransactionEntity>>

    /** A Date (not a raw Long) so Room applies DateConverter, matching how the column is stored. */
    @Query("SELECT * FROM recurring_transaction WHERE next_due_date <= :dueBefore ORDER BY next_due_date ASC")
    suspend fun getDue(dueBefore: Date): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transaction WHERE id = :id")
    suspend fun findById(id: String): RecurringTransactionEntity?

    @Query("DELETE FROM recurring_transaction WHERE id = :id")
    suspend fun deleteById(id: String)
}
