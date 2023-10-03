package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.data.db.dao.AccountDao
import com.nkuppan.expensemanager.data.mappers.toDomainModel
import com.nkuppan.expensemanager.data.mappers.toEntityModel
import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.AccountRepository
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

    override suspend fun getAllAccount(): Resource<List<Account>> = withContext(dispatchers.io) {
        return@withContext try {
            val account = accountDao.getAllValues()

            if (account != null) {
                Resource.Success(account.map { it.toDomainModel() })
            } else {
                Resource.Error(KotlinNullPointerException())
            }
        } catch (e: Exception) {
            Resource.Error(e)
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