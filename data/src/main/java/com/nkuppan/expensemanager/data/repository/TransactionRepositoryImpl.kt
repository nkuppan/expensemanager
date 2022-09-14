package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.data.db.dao.TransactionDao
import com.nkuppan.expensemanager.data.db.entity.TransactionRelation
import com.nkuppan.expensemanager.data.mappers.AccountEntityDomainMapper
import com.nkuppan.expensemanager.data.mappers.CategoryEntityDomainMapper
import com.nkuppan.expensemanager.data.mappers.TransactionDomainEntityMapper
import com.nkuppan.expensemanager.data.mappers.TransactionEntityDomainMapper
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val transactionDomainEntityMapper: TransactionDomainEntityMapper,
    private val transactionEntityDomainMapper: TransactionEntityDomainMapper,
    private val categoryEntityDomainMapper: CategoryEntityDomainMapper,
    private val accountEntityDomainMapper: AccountEntityDomainMapper,
    private val dispatchers: AppCoroutineDispatchers
) : TransactionRepository {

    override suspend fun getAllTransaction(): Resource<List<Transaction>> =
        withContext(dispatchers.io) {
            return@withContext try {
                val transactionWithCategory = transactionDao.getAllTransaction()
                Resource.Success(convertTransactionAndCategory(transactionWithCategory))
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun findTransactionById(transactionId: String): Resource<Transaction> =
        withContext(dispatchers.io) {
            return@withContext try {
                val transaction = transactionDao.findById(transactionId)

                if (transaction != null) {
                    Resource.Success(transactionEntityDomainMapper.convert(transaction))
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun addOrUpdateTransaction(transaction: Transaction): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response =
                    transactionDao.insert(transactionDomainEntityMapper.convert(transaction))
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteTransaction(transaction: Transaction): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response =
                    transactionDao.delete(transactionDomainEntityMapper.convert(transaction))
                Resource.Success(response != -1)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }


    override suspend fun getTransactionsByAccountId(accountId: String): Resource<List<Transaction>> =
        withContext(dispatchers.io) {
            return@withContext try {
                val transaction = transactionDao.getTransactionsByAccountId(accountId)

                if (transaction != null) {
                    Resource.Success(transaction.map { transactionEntityDomainMapper.convert(it) })
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun getTransactionByAccountIdAndDateFilter(
        accountId: String,
        startDate: Long,
        endDate: Long
    ): Resource<List<Transaction>> = withContext(dispatchers.io) {
        return@withContext try {
            val transactionWithCategory = transactionDao.getTransactionsByAccountIdAndDateFilter(
                accountId,
                startDate,
                endDate
            )
            Resource.Success(convertTransactionAndCategory(transactionWithCategory))
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun getTransactionByDateFilter(
        startDate: Long,
        endDate: Long
    ): Resource<List<Transaction>> = withContext(dispatchers.io) {
        return@withContext try {
            val transactionWithCategory = transactionDao.getTransactionsByDateFilter(
                startDate, endDate
            )
            Resource.Success(convertTransactionAndCategory(transactionWithCategory))
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    private fun convertTransactionAndCategory(
        transactionWithCategory: List<TransactionRelation>?
    ): MutableList<Transaction> {

        val outputTransactions = mutableListOf<Transaction>()

        if (transactionWithCategory?.isNotEmpty() == true) {

            transactionWithCategory.forEach {
                val transaction = transactionEntityDomainMapper.convert(it.transactionEntity)
                transaction.category = categoryEntityDomainMapper.convert(it.categoryEntity)
                transaction.account = accountEntityDomainMapper.convert(it.accountEntity)
                outputTransactions.add(transaction)
            }
        }
        return outputTransactions
    }
}