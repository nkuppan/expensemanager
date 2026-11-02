package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountDeleteImpact
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun getAccounts(): Flow<List<Account>>

    suspend fun findAccount(accountId: String): Resource<Account>

    suspend fun addAccount(account: Account): Resource<Boolean>

    /** Persists only the appearance (icon, color, custom image): everything else is immutable. */
    suspend fun updateAccount(account: Account): Resource<Boolean>

    /** Persists only each account's list order ([Account.sequence]). */
    suspend fun updateAllAccount(accounts: List<Account>): Resource<Boolean>

    suspend fun setAccountArchived(accountId: String, archived: Boolean): Resource<Boolean>

    /** Whether any transaction uses the account (as source or transfer destination). */
    suspend fun hasTransactions(accountId: String): Boolean

    suspend fun deleteAccount(account: Account): Resource<Boolean>

    suspend fun getAccountDeleteImpact(accountId: String): Resource<AccountDeleteImpact>
}
