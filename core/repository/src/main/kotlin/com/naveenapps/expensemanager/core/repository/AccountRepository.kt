package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun getAccounts(): Flow<List<Account>>

    suspend fun findAccount(accountId: String): Resource<Account>

    suspend fun addAccount(account: Account): Resource<Boolean>

    suspend fun updateAccount(account: Account): Resource<Boolean>

    suspend fun updateAllAccount(accounts: List<Account>): Resource<Boolean>

    suspend fun deleteAccount(account: Account): Resource<Boolean>
}
