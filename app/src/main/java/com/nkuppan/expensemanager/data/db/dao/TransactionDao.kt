package com.nkuppan.expensemanager.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nkuppan.expensemanager.data.db.entity.AccountEntity
import com.nkuppan.expensemanager.data.db.entity.TransactionEntity
import com.nkuppan.expensemanager.data.db.entity.TransactionRelation
import kotlinx.coroutines.flow.Flow


@Dao
interface TransactionDao : BaseDao<TransactionEntity> {

    @Query("SELECT * from `transaction` WHERE id=:id")
    fun findById(id: String): TransactionEntity?

    @Query("SELECT * from `transaction` WHERE from_account_id IN(:accounts) ORDER BY `transaction`.created_on DESC")
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
        SELECT 
            `transaction`.*, 
            `category`.name, `category`.icon_background_color, `category`.icon_name,`category`.type,
            `fromAccount`.name, `fromAccount`.icon_background_color, `fromAccount`.type, `fromAccount`.icon_name
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id   
        JOIN `account` as fromAccount ON from_account_id = `fromAccount`.id
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
        SELECT 
            SUM(`transaction`.amount)
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id
        JOIN `account` ON from_account_id = `account`.id
        WHERE `transaction`.from_account_id IN(:accounts) 
        OR `category`.id IN(:categories)
        OR `category`.type IN(:categoryTypes)
        AND `transaction`.created_on BETWEEN :fromDate AND :toDate
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getTransactionTotalAmount(
        accounts: List<String>,
        categories: List<String>,
        categoryTypes: List<Int>,
        fromDate: Long,
        toDate: Long
    ): Flow<Double?>

    @Query(
        """
        SELECT 
            `transaction`.*, 
            `category`.name, `category`.icon_background_color, `category`.icon_name,`category`.type,
            `fromAccount`.name, `fromAccount`.icon_background_color, `fromAccount`.type, `fromAccount`.icon_name
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id
        JOIN `account` as fromAccount ON from_account_id = `fromAccount`.id
        WHERE `transaction`.from_account_id IN(:accounts) 
        OR `category`.id IN(:categories)
        OR `category`.type IN(:categoryTypes)
        AND `transaction`.created_on BETWEEN :fromDate AND :toDate
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        categoryTypes: List<Int>,
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