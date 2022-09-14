package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.db.dao.AccountDao
import com.nkuppan.expensemanager.data.mappers.AccountDomainEntityMapper
import com.nkuppan.expensemanager.data.mappers.AccountEntityDomainMapper
import kotlinx.coroutines.withContext

class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val accountDomainEntityMapper: AccountDomainEntityMapper,
    private val accountEntityDomainMapper: AccountEntityDomainMapper,
    private val dispatchers: AppCoroutineDispatchers
) : AccountRepository {

    override suspend fun getAllAccount(): Resource<List<Account>> = withContext(dispatchers.io) {
        return@withContext try {
            val account = accountDao.getAllValues()

            if (account != null) {
                Resource.Success(account.map { accountEntityDomainMapper.convert(it) })
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
                    Resource.Success(accountEntityDomainMapper.convert(account))
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
                val response = accountDao.insert(accountDomainEntityMapper.convert(account))
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateAccount(account: Account): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                accountDao.update(accountDomainEntityMapper.convert(account))
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteAccount(account: Account): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = accountDao.delete(accountDomainEntityMapper.convert(account))
                Resource.Success(response != -1)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }
}