package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.Resource

interface AccountRepository {

    suspend fun getAllAccount(): Resource<List<Account>>

    suspend fun findAccount(accountId: String): Resource<Account>

    suspend fun addAccount(account: Account): Resource<Boolean>

    suspend fun updateAccount(account: Account): Resource<Boolean>

    suspend fun deleteAccount(account: Account): Resource<Boolean>
}