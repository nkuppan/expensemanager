package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun getAccounts(): Flow<List<Account>>

    suspend fun findAccount(accountId: String): Resource<Account>

    suspend fun addAccount(account: Account): Resource<Boolean>

    suspend fun updateAccount(account: Account): Resource<Boolean>

    suspend fun deleteAccount(account: Account): Resource<Boolean>
}