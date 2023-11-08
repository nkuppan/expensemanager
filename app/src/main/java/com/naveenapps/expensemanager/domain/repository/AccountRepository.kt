package com.naveenapps.expensemanager.domain.repository

import com.naveenapps.expensemanager.domain.model.Account
import com.naveenapps.expensemanager.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun getAccounts(): Flow<List<Account>>

    suspend fun findAccount(accountId: String): Resource<Account>

    suspend fun addAccount(account: Account): Resource<Boolean>

    suspend fun updateAccount(account: Account): Resource<Boolean>

    suspend fun deleteAccount(account: Account): Resource<Boolean>
}