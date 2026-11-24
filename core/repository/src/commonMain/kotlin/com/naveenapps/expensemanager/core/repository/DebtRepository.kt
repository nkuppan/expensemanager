package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.DebtEntry
import com.naveenapps.expensemanager.core.model.DebtPerson
import kotlinx.coroutines.flow.Flow

/**
 * The debt ledger: people and what was lent, borrowed and paid back. Adding, editing or deleting
 * an entry linked to an account also moves that account's balance, atomically.
 */
interface DebtRepository {

    /** Everyone, by name. */
    fun getPeople(): Flow<List<DebtPerson>>

    /** Every entry, newest first. */
    fun getEntries(): Flow<List<DebtEntry>>

    /** One person's entries, newest first. */
    fun getEntries(personId: String): Flow<List<DebtEntry>>

    suspend fun getAllEntries(): List<DebtEntry>

    suspend fun findPerson(id: String): DebtPerson?

    suspend fun findEntry(id: String): DebtEntry?

    /** Adds or renames a person. */
    suspend fun savePerson(person: DebtPerson)

    /** Deletes the person and their entries, giving linked accounts their money back. */
    suspend fun deletePerson(id: String)

    suspend fun addEntry(entry: DebtEntry)

    /** False when the entry no longer exists. */
    suspend fun updateEntry(entry: DebtEntry): Boolean

    suspend fun deleteEntry(id: String)
}
