package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction

interface TransactionRepository {

    suspend fun findTransactionById(transactionId: String): Resource<Transaction>

    suspend fun addOrUpdateTransaction(transaction: Transaction): Resource<Boolean>

    suspend fun deleteTransaction(transaction: Transaction): Resource<Boolean>


    suspend fun getTransactionsByAccountId(accountId: String): Resource<List<Transaction>>

    suspend fun getAllTransaction(): Resource<List<Transaction>>

    suspend fun getTransactionByAccountIdAndDateFilter(
        accountId: String,
        startDate: Long,
        endDate: Long
    ): Resource<List<Transaction>>

    suspend fun getTransactionByDateFilter(
        startDate: Long,
        endDate: Long
    ): Resource<List<Transaction>>
}