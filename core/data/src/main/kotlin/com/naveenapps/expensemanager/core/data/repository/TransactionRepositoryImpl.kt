package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.fromLocalToUTCTimeStamp
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.database.dao.TransactionDao
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionRelation
import com.naveenapps.expensemanager.core.foundation.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.model.FrequentTransaction
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.isTransfer
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val dispatchers: AppCoroutineDispatchers,
) : TransactionRepository {

    override fun getAllTransaction(): Flow<List<Transaction>?> = transactionDao.getAllTransaction().map {
        convertTransactionAndCategory(it)
    }

    override suspend fun findTransactionById(transactionId: String): Resource<Transaction> = withContext(dispatchers.io) {
        return@withContext try {
            val transaction = transactionDao.findEntityById(transactionId)

            if (transaction != null) {
                Resource.Success(convertTransactionCategoryRelation(transaction))
            } else {
                Resource.Error(KotlinNullPointerException())
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Resource<Boolean> = withContext(dispatchers.io) {
        return@withContext try {
            val response = transactionDao.insertTransaction(
                transaction.toEntityModel(),
                if (transaction.type == TransactionType.INCOME) {
                    transaction.amount.amount
                } else {
                    transaction.amount.amount * -1
                },
                transaction.type.isTransfer(),
                transaction.attachments,
            )
            Resource.Success(response != -1L)
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Resource<Boolean> = withContext(dispatchers.io) {
        return@withContext try {
            val transactionEntity = transaction.toEntityModel()
            // Reversal of the old amount happens inside this DAO transaction.
            transactionDao.updateTransaction(
                transactionEntity,
                if (transaction.type.isIncome()) {
                    transaction.amount.amount
                } else {
                    transaction.amount.amount * -1
                },
                transaction.type.isTransfer(),
                transaction.attachments,
            )
            Resource.Success(true)
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction): Resource<Boolean> = withContext(dispatchers.io) {
        return@withContext try {
            val transactionEntity = transaction.toEntityModel()
            val response = transactionDao.deleteTransaction(transactionEntity)
            Resource.Success(response > 0)
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    private fun convertTransactionAndCategory(
        transactionWithCategory: List<TransactionRelation>?,
    ): MutableList<Transaction> {
        val outputTransactions = mutableListOf<Transaction>()

        if (transactionWithCategory?.isNotEmpty() == true) {
            transactionWithCategory.forEach {
                convertTransactionCategoryRelation(it)?.let { transaction ->
                    outputTransactions.add(transaction)
                }
            }
        }
        return outputTransactions
    }

    private fun convertTransactionCategoryRelation(relation: TransactionRelation): Transaction? {
        val category = relation.categoryEntity?.toDomainModel() ?: return null
        val fromAccount = relation.fromAccountEntity?.toDomainModel() ?: return null
        return relation.transactionEntity.toDomainModel().copy(
            category = category,
            fromAccount = fromAccount,
            toAccount = relation.toAccountEntity?.toDomainModel(),
            attachments = relation.attachmentEntities.map { it.imagePath },
        )
    }

    private suspend fun convertTransactionCategoryRelation(relation: TransactionEntity): Transaction {
        val transaction = relation.toDomainModel()
        return transaction.copy(
            category = categoryDao.findById(transaction.categoryId)?.toDomainModel() ?: transaction.category,
            fromAccount = accountDao.findById(transaction.fromAccountId)?.toDomainModel() ?: transaction.fromAccount,
            toAccount = transaction.toAccountId?.let { accountDao.findById(it)?.toDomainModel() },
            attachments = transactionDao.getTransactionAttachments(transaction.id).map { it.imagePath },
        )
    }

    override fun getAllFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionType: List<Int>,
    ): Flow<List<Transaction>?> = transactionDao.getAllFilteredTransaction(
        accounts,
        categories,
        transactionType,
    ).map {
        convertTransactionAndCategory(it)
    }

    override fun getFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionType: List<Int>,
        startDate: Long,
        endDate: Long,
    ): Flow<List<Transaction>?> = transactionDao.getFilteredTransaction(
        accounts,
        categories,
        transactionType,
        startDate.fromLocalToUTCTimeStamp(),
        endDate.fromLocalToUTCTimeStamp(),
    ).map {
        convertTransactionAndCategory(it)
    }

    override fun getFrequentTransactions(
        transactionTypes: List<TransactionType>,
        since: Long,
        minCount: Int,
        limit: Int,
    ): Flow<List<FrequentTransaction>> = transactionDao.getFrequentTransactions(
        transactionTypes.map { it.ordinal },
        since.fromLocalToUTCTimeStamp(),
        minCount,
        limit,
    ).map { rows ->
        rows.map {
            FrequentTransaction(
                type = it.type,
                categoryId = it.categoryId,
                fromAccountId = it.fromAccountId,
                amount = it.amount,
                notes = it.notes,
                useCount = it.useCount,
            )
        }
    }

    override suspend fun findCategoryIdsByNotes(
        type: TransactionType,
        notes: String,
        limit: Int,
    ): List<String> = withContext(dispatchers.io) {
        transactionDao.findCategoryIdsByNotes(type.ordinal, notes.trim().lowercase(), limit)
    }

    override suspend fun findLatestAccountIdByNotes(
        type: TransactionType,
        notes: String,
    ): String? = withContext(dispatchers.io) {
        transactionDao.findLatestAccountIdByNotes(type.ordinal, notes.trim().lowercase())
    }
}
