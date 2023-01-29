package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun findTransactionById(transactionId: String): Resource<Transaction>

    suspend fun addOrUpdateTransaction(transaction: Transaction): Resource<Boolean>

    suspend fun deleteTransaction(transaction: Transaction): Resource<Boolean>

    fun getTransactionsByAccountId(accountId: String): Flow<List<Transaction>?>

    fun getAllTransaction(): Flow<List<Transaction>?>

    fun getTransactionByAccountIdAndDateFilter(
        accountId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>>

    fun getTransactionByDateFilter(
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>>

    fun getTransactionAmount(
        accountId: String,
        categoryType: Int,
        startDate: Long,
        endDate: Long
    ): Flow<Double?>
}