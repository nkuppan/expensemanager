package com.nkuppan.expensemanager.data.repository

import android.util.Log
import com.nkuppan.expensemanager.core.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.data.db.dao.TransactionDao
import com.nkuppan.expensemanager.data.db.entity.TransactionRelation
import com.nkuppan.expensemanager.data.mappers.toDomainModel
import com.nkuppan.expensemanager.data.mappers.toEntityModel
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val dispatchers: AppCoroutineDispatchers
) : TransactionRepository {

    override fun getAllTransaction(): Flow<List<Transaction>?> =
        transactionDao.getAllTransaction().map {
            convertTransactionAndCategory(it)
        }

    override suspend fun findTransactionById(transactionId: String): Resource<Transaction> =
        withContext(dispatchers.io) {
            return@withContext try {
                val transaction = transactionDao.findById(transactionId)

                if (transaction != null) {
                    Resource.Success(transaction.toDomainModel())
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
                    transactionDao.insert(transaction.toEntityModel())
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteTransaction(transaction: Transaction): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response =
                    transactionDao.delete(transaction.toEntityModel())
                Resource.Success(response != -1)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }


    override fun getTransactionsByAccountId(accountId: String): Flow<List<Transaction>?> =
        transactionDao.getTransactionsByAccountId(accountId).map { transaction ->
            transaction?.map { it.toDomainModel() }
        }

    override fun getTransactionByAccountIdAndDateFilter(
        accountId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> =
        transactionDao.getTransactionsByAccountIdAndDateFilter(accountId, startDate, endDate).map {
            Log.i("TAG", "getTransactionByAccountIdAndDateFilter: ${it?.size}")
            convertTransactionAndCategory(it)
        }

    override fun getTransactionByDateFilter(
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateFilter(startDate, endDate).map {
            convertTransactionAndCategory(it)
        }

    private fun convertTransactionAndCategory(
        transactionWithCategory: List<TransactionRelation>?
    ): MutableList<Transaction> {

        val outputTransactions = mutableListOf<Transaction>()

        if (transactionWithCategory?.isNotEmpty() == true) {

            transactionWithCategory.forEach {
                val transaction = it.transactionEntity.toDomainModel()
                transaction.category = it.categoryEntity.toDomainModel()
                transaction.account = it.fromAccountEntity.toDomainModel()
                outputTransactions.add(transaction)
            }
        }
        return outputTransactions
    }

    override fun getTransactionAmount(
        accountId: String,
        categoryType: Int,
        startDate: Long,
        endDate: Long
    ): Flow<Double?> {
        return transactionDao.getTransactionTotalAmount(
            accountId, categoryType, startDate, endDate
        )
    }
}