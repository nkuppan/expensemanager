package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.naveenapps.expensemanager.core.database.entity.DebtEntryEntity
import com.naveenapps.expensemanager.core.database.entity.DebtPersonEntity
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.core.model.milliToDouble
import kotlinx.coroutines.flow.Flow

/**
 * The debt ledger. Entries linked to an account move that account's balance in the same
 * database transaction, so the ledger and the balance never get out of step: adding applies the
 * effect, editing reverses the old one and applies the new, deleting reverses it.
 */
@Dao
abstract class DebtDao {

    @Query("SELECT * FROM debt_person ORDER BY name COLLATE NOCASE ASC")
    abstract fun getPeople(): Flow<List<DebtPersonEntity>>

    @Query("SELECT * FROM debt_person WHERE id = :id")
    abstract suspend fun findPerson(id: String): DebtPersonEntity?

    @Query("SELECT * FROM debt_entry ORDER BY date DESC, created_on DESC")
    abstract fun getEntries(): Flow<List<DebtEntryEntity>>

    @Query("SELECT * FROM debt_entry WHERE person_id = :personId ORDER BY date DESC, created_on DESC")
    abstract fun getEntriesForPerson(personId: String): Flow<List<DebtEntryEntity>>

    @Query("SELECT * FROM debt_entry WHERE person_id = :personId")
    abstract suspend fun getEntriesForPersonOnce(personId: String): List<DebtEntryEntity>

    @Query("SELECT * FROM debt_entry")
    abstract suspend fun getEntriesOnce(): List<DebtEntryEntity>

    @Query("SELECT * FROM debt_entry WHERE id = :id")
    abstract suspend fun findEntry(id: String): DebtEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertPerson(person: DebtPersonEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertEntryRow(entry: DebtEntryEntity)

    @Update
    abstract suspend fun updateEntryRow(entry: DebtEntryEntity)

    @Query("DELETE FROM debt_entry WHERE id = :id")
    abstract suspend fun deleteEntryRow(id: String)

    @Query("DELETE FROM debt_person WHERE id = :id")
    abstract suspend fun deletePersonRow(id: String)

    @Query("UPDATE account SET amount = amount + :delta WHERE id = :accountId")
    abstract suspend fun adjustAccount(accountId: String, delta: Double)

    /** Moves the linked account (if any) by the entry's effect, or undoes it. */
    private suspend fun moveAccount(entry: DebtEntryEntity, undo: Boolean) {
        val accountId = entry.accountId ?: return
        val kind = DebtEntryKind.entries.find { it.name == entry.kind } ?: return
        val delta = kind.accountSign * entry.amountMilli.milliToDouble()
        adjustAccount(accountId, if (undo) -delta else delta)
    }

    @Transaction
    open suspend fun insertEntry(entry: DebtEntryEntity) {
        insertEntryRow(entry)
        moveAccount(entry, undo = false)
    }

    /** Replaces the stored entry with [entry]; false when it no longer exists. */
    @Transaction
    open suspend fun updateEntry(entry: DebtEntryEntity): Boolean {
        val old = findEntry(entry.id) ?: return false
        moveAccount(old, undo = true)
        updateEntryRow(entry)
        moveAccount(entry, undo = false)
        return true
    }

    @Transaction
    open suspend fun deleteEntry(id: String) {
        val old = findEntry(id) ?: return
        moveAccount(old, undo = true)
        deleteEntryRow(id)
    }

    /** Removes the person and their entries, giving back every linked account's money first. */
    @Transaction
    open suspend fun deletePerson(id: String) {
        getEntriesForPersonOnce(id).forEach { moveAccount(it, undo = true) }
        deletePersonRow(id)
    }
}
