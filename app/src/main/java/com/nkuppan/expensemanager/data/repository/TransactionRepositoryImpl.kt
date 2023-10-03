package com.nkuppan.expensemanager.data.repository

import android.util.Log
import com.nkuppan.expensemanager.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.data.db.dao.AccountDao
import com.nkuppan.expensemanager.data.db.dao.CategoryDao
import com.nkuppan.expensemanager.data.db.dao.TransactionDao
import com.nkuppan.expensemanager.data.db.entity.TransactionEntity
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
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
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
                    Resource.Success(convertTransactionCategoryRelation(transaction))
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun addTransaction(transaction: Transaction): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = transactionDao.insertTransaction(transaction.toEntityModel())
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateTransaction(transaction: Transaction): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                transactionDao.updateTransaction(transaction.toEntityModel())
                Resource.Success(true)
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
                val transaction = convertTransactionCategoryRelation(it)
                outputTransactions.add(transaction)
            }
        }
        return outputTransactions
    }

    private fun convertTransactionCategoryRelation(relation: TransactionRelation): Transaction {
        return relation.transactionEntity.toDomainModel().apply {
            category = relation.categoryEntity.toDomainModel()
            fromAccount = relation.fromAccountEntity.toDomainModel()
            toAccount = relation.toAccountEntity?.toDomainModel()
        }
    }

    private fun convertTransactionCategoryRelation(relation: TransactionEntity): Transaction {
        val transaction = relation.toDomainModel()
        categoryDao.findById(transaction.categoryId)?.let {
            transaction.category = it.toDomainModel()
        }
        accountDao.findById(transaction.fromAccountId)?.let {
            transaction.fromAccount = it.toDomainModel()
        }
        transaction.toAccountId?.let {
            accountDao.findById(transaction.toAccountId)?.let {
                transaction.toAccount = it.toDomainModel()
            }
        }
        return transaction
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