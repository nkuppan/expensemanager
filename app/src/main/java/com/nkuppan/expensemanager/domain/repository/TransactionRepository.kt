package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun findTransactionById(transactionId: String): Resource<Transaction>

    suspend fun addTransaction(transaction: Transaction): Resource<Boolean>

    suspend fun updateTransaction(transaction: Transaction): Resource<Boolean>

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