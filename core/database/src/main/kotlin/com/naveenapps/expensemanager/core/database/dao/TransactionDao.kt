package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionRelation
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.isTransfer
import kotlinx.coroutines.flow.Flow


@Dao
interface TransactionDao : BaseDao<TransactionEntity> {

    @Query("SELECT * from `transaction` WHERE id=:id")
    fun findById(id: String): TransactionEntity?

    @Query(
        """
        SELECT * from `transaction` 
        WHERE from_account_id IN(:accounts) 
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getTransactionsByAccounts(
        accounts: List<String>
    ): Flow<List<TransactionEntity>?>

    @Transaction
    @Query("SELECT * FROM `transaction`")
    fun getAllTransaction(): Flow<List<TransactionRelation>?>

    @Transaction
    @Query(
        """
        SELECT * FROM `transaction`
        WHERE created_on BETWEEN :fromDate AND :toDate
        ORDER BY created_on DESC
        """
    )
    fun getTransactionsByDateFilter(
        fromDate: Long,
        toDate: Long
    ): Flow<List<TransactionRelation>?>

    @Transaction
    @Query(
        """
        SELECT * FROM `transaction`
        WHERE `transaction`.from_account_id IN(:accounts) 
        AND `transaction`.created_on BETWEEN :fromDate AND :toDate
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getTransactionsByAccountIdAndDateFilter(
        accounts: List<String>,
        fromDate: Long,
        toDate: Long
    ): Flow<List<TransactionRelation>?>

    @Query(
        """
        SELECT SUM(`transaction`.amount) FROM `transaction`
        WHERE `transaction`.from_account_id IN(:accounts) 
        OR `transaction`.id IN(:categories)
        OR `transaction`.type IN(:transactionTypes)
        AND `transaction`.created_on BETWEEN :fromDate AND :toDate
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getTransactionTotalAmount(
        accounts: List<String>,
        categories: List<String>,
        transactionTypes: List<Int>,
        fromDate: Long,
        toDate: Long
    ): Flow<Double?>

    @Query(
        """
        SELECT * FROM `transaction`
        WHERE `transaction`.from_account_id IN(:accounts) 
        AND `transaction`.category_id IN(:categories)
        AND `transaction`.type IN(:transactionTypes)
        AND `transaction`.created_on BETWEEN :fromDate AND :toDate
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionTypes: List<Int>,
        fromDate: Long,
        toDate: Long
    ): Flow<List<TransactionRelation>?>

    @Update
    suspend fun updateAccount(accountEntity: AccountEntity)

    @Query("SELECT * FROM account WHERE id = :id")
    suspend fun findAccountById(id: String): AccountEntity?

    @Transaction
    suspend fun insertTransaction(
        transactionEntity: TransactionEntity,
        amountToDetect: Double,
        isTransfer: Boolean
    ): Long {
        val id = insert(transactionEntity)
        if (id != -1L) {
            val accountEntity = findAccountById(transactionEntity.fromAccountId)
            if (accountEntity != null) {
                updateAccount(
                    accountEntity.copy(
                        amount = accountEntity.amount + amountToDetect
                    )
                )
            }
            if (isTransfer && transactionEntity.toAccountId?.isNotBlank() == true) {
                val toAccountEntity = findAccountById(
                    transactionEntity.toAccountId!!
                )
                if (toAccountEntity != null) {
                    updateAccount(
                        toAccountEntity.copy(
                            amount = toAccountEntity.amount + (amountToDetect * -1)
                        )
                    )
                }
            }
        }
        return id
    }

    suspend fun removePreviousEnteredAmount(transactionEntity: TransactionEntity) {

        val previousTransaction = findById(transactionEntity.id)

        if (previousTransaction != null) {
            val previousAmountToDetect = if (previousTransaction.type == TransactionType.INCOME) {
                previousTransaction.amount * -1
            } else {
                previousTransaction.amount
            }
            val previousSelectedFromAccount = findAccountById(previousTransaction.fromAccountId)
            if (previousSelectedFromAccount != null) {
                updateAccount(
                    previousSelectedFromAccount.copy(
                        amount = previousSelectedFromAccount.amount + previousAmountToDetect
                    )
                )
            }

            if (previousTransaction.type.isTransfer() && previousTransaction.toAccountId?.isNotBlank() == true) {
                val toAccountEntity = findAccountById(
                    previousTransaction.toAccountId!!
                )
                if (toAccountEntity != null) {
                    updateAccount(
                        toAccountEntity.copy(
                            amount = toAccountEntity.amount + (previousAmountToDetect * -1)
                        )
                    )
                }
            }
        }
    }

    @Transaction
    suspend fun updateTransaction(
        transactionEntity: TransactionEntity,
        amountToDetect: Double,
        isTransfer: Boolean
    ) {

        update(transactionEntity)

        val accountEntity = findAccountById(transactionEntity.fromAccountId)
        if (accountEntity != null) {
            updateAccount(
                accountEntity.copy(
                    amount = accountEntity.amount + amountToDetect
                )
            )
        }
        if (isTransfer && transactionEntity.toAccountId?.isNotBlank() == true) {
            val toAccountEntity = findAccountById(
                transactionEntity.toAccountId!!
            )
            if (toAccountEntity != null) {
                updateAccount(
                    toAccountEntity.copy(
                        amount = toAccountEntity.amount + (amountToDetect * -1)
                    )
                )
            }
        }
    }
}