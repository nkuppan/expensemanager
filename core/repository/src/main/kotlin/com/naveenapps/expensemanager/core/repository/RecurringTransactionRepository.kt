package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.RecurringTransaction
import java.util.Date
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {

    /** All rules, soonest due first. Display fields (category, accounts) are not filled in. */
    fun getAll(): Flow<List<RecurringTransaction>>

    suspend fun getDue(dueBefore: Date): List<RecurringTransaction>

    suspend fun add(recurring: RecurringTransaction)

    suspend fun update(recurring: RecurringTransaction)

    suspend fun delete(id: String)
}
