package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun findTransactionById(transactionId: String): Resource<Transaction>

    suspend fun addTransaction(transaction: Transaction): Resource<Boolean>

    suspend fun updateTransaction(transaction: Transaction): Resource<Boolean>

    suspend fun deleteTransaction(transaction: Transaction): Resource<Boolean>

    fun getTransactionsByAccountId(
        accounts: List<String>
    ): Flow<List<Transaction>?>

    fun getAllTransaction(): Flow<List<Transaction>?>

    fun getTransactionByAccountIdAndDateFilter(
        accounts: List<String>,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>>

    fun getTransactionByDateFilter(
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>>

    fun getTransactionAmount(
        accounts: List<String>,
        categories: List<String>,
        categoryType: List<Int>,
        startDate: Long,
        endDate: Long
    ): Flow<Double?>

    fun getFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        categoryType: List<Int>,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>?>
}