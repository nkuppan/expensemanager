package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.naveenapps.expensemanager.core.database.entity.RecurringTransactionEntity
import java.util.Date
import kotlinx.coroutines.flow.Flow

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

    /**
     * Moves a rule on to its next occurrence without touching the template columns, so a user
     * edit saved while the background job is running is never overwritten by the job's stale copy.
     */
    @Query(
        "UPDATE recurring_transaction SET next_occurrence = :nextOccurrence, " +
            "next_due_date = :nextDueDate, updated_on = :updatedOn WHERE id = :id",
    )
    suspend fun advance(id: String, nextOccurrence: Int, nextDueDate: Date, updatedOn: Date)

    /** Ends [oldId] and starts [replacement] in one step (used when the schedule changes). */
    @Transaction
    suspend fun replace(oldId: String, replacement: RecurringTransactionEntity) {
        deleteById(oldId)
        insert(replacement)
    }
}
