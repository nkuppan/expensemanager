package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.RecurringTransaction
import java.util.Date
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {

    /** All rules, soonest due first. Display fields (category, accounts) are not filled in. */
    fun getAll(): Flow<List<RecurringTransaction>>

    suspend fun getDue(dueBefore: Date): List<RecurringTransaction>

    /** A single rule, or null if it no longer exists. Display fields are not filled in. */
    suspend fun findById(id: String): RecurringTransaction?

    suspend fun add(recurring: RecurringTransaction)

    suspend fun update(recurring: RecurringTransaction)

    suspend fun delete(id: String)

    /** Updates only the schedule progress of a rule; the template is left as stored. */
    suspend fun advance(id: String, nextOccurrence: Int, nextDueDate: Date, updatedOn: Date)

    /** Atomically deletes [oldId] and adds [replacement]. */
    suspend fun replace(oldId: String, replacement: RecurringTransaction)
}
