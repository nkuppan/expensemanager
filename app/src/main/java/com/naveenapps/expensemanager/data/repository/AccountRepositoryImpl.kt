package com.naveenapps.expensemanager.data.repository

import com.naveenapps.expensemanager.data.db.dao.AccountDao
import com.naveenapps.expensemanager.data.mappers.toDomainModel
import com.naveenapps.expensemanager.data.mappers.toEntityModel
import com.naveenapps.expensemanager.domain.model.Account
import com.naveenapps.expensemanager.domain.model.Resource
import com.naveenapps.expensemanager.domain.repository.AccountRepository
import com.naveenapps.expensemanager.utils.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val dispatchers: AppCoroutineDispatchers
) : AccountRepository {

    override fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAccounts().map { accounts ->
            return@map accounts?.map { it.toDomainModel() } ?: emptyList()
        }
    }

    override suspend fun findAccount(accountId: String): Resource<Account> =
        withContext(dispatchers.io) {
            return@withContext try {
                val account = accountDao.findById(accountId)

                if (account != null) {
                    Resource.Success(account.toDomainModel())
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun addAccount(account: Account): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = accountDao.insert(account.toEntityModel())
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateAccount(account: Account): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                accountDao.update(account.toEntityModel())
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteAccount(account: Account): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = accountDao.delete(account.toEntityModel())
                Resource.Success(response != -1)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }
}