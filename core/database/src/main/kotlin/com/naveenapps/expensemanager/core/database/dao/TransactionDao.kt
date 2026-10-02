package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionAttachmentEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionRelation
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.isTransfer
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao : BaseDao<TransactionEntity> {

    @Query("SELECT * from `transaction` WHERE id=:id")
    fun findById(id: String): TransactionEntity?

    // @Transaction is required here (not just on writes) because TransactionRelation embeds
    // multiple @Relation fields (category, fromAccount, toAccount, attachments). Room fetches
    // each relation via a separate sub-query and stitches the results back onto the parent rows
    // using a key->list map; without @Transaction that stitching isn't atomic with the main
    // query, so a concurrent write (e.g. attachments being deleted/reinserted on save) between
    // the main query and a relation sub-query can leave a row's key missing from the map and
    // throw NoSuchElementException from the generated getValue(...) call.
    @Transaction
    @Query("SELECT * FROM `transaction`")
    fun getAllTransaction(): Flow<List<TransactionRelation>?>

    @Transaction
    @Query(
        """
        SELECT * FROM `transaction`
        WHERE `transaction`.from_account_id IN(:accounts)
        AND `transaction`.category_id IN(:categories)
        AND `transaction`.type IN(:transactionTypes)
        ORDER BY `transaction`.created_on DESC
        """,
    )
    fun getAllFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionTypes: List<Int>,
    ): Flow<List<TransactionRelation>?>

    @Transaction
    @Query(
        """
        SELECT * FROM `transaction`
        WHERE `transaction`.from_account_id IN(:accounts)
        AND `transaction`.category_id IN(:categories)
        AND `transaction`.type IN(:transactionTypes)
        AND `transaction`.created_on BETWEEN :fromDate AND :toDate
        ORDER BY `transaction`.created_on DESC
        """,
    )
    fun getFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionTypes: List<Int>,
        fromDate: Long,
        toDate: Long,
    ): Flow<List<TransactionRelation>?>

    @Update
    suspend fun updateAccount(accountEntity: AccountEntity)

    @Query("SELECT * FROM account WHERE id = :id")
    suspend fun findAccountById(id: String): AccountEntity?

    @Query(
        "SELECT * FROM transaction_attachment WHERE transaction_id = :transactionId ORDER BY created_on ASC",
    )
    suspend fun getTransactionAttachments(transactionId: String): List<TransactionAttachmentEntity>

    @Query("DELETE FROM transaction_attachment WHERE transaction_id = :transactionId")
    suspend fun removeTransactionAttachments(transactionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionAttachment(attachmentEntity: TransactionAttachmentEntity): Long

    @Transaction
    suspend fun insertTransaction(
        transactionEntity: TransactionEntity,
        amountToDetect: Double,
        isTransfer: Boolean,
        attachmentPaths: List<String> = emptyList(),
    ): Long {
        val id = insert(transactionEntity)
        if (id != -1L) {
            attachmentPaths.forEach { path ->
                insertTransactionAttachment(
                    TransactionAttachmentEntity(
                        id = UUID.randomUUID().toString(),
                        transactionId = transactionEntity.id,
                        imagePath = path,
                        createdOn = Date(),
                    ),
                )
            }
            val accountEntity = findAccountById(transactionEntity.fromAccountId)
            if (accountEntity != null) {
                updateAccount(
                    accountEntity.copy(
                        amount = accountEntity.amount + amountToDetect,
                    ),
                )
            }
            if (isTransfer && transactionEntity.toAccountId?.isNotBlank() == true) {
                val toAccountEntity = findAccountById(
                    transactionEntity.toAccountId!!,
                )
                if (toAccountEntity != null) {
                    updateAccount(
                        toAccountEntity.copy(
                            amount = toAccountEntity.amount + (amountToDetect * -1),
                        ),
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
                        amount = previousSelectedFromAccount.amount + previousAmountToDetect,
                    ),
                )
            }

            if (previousTransaction.type.isTransfer() && previousTransaction.toAccountId?.isNotBlank() == true) {
                val toAccountEntity = findAccountById(
                    previousTransaction.toAccountId!!,
                )
                if (toAccountEntity != null) {
                    updateAccount(
                        toAccountEntity.copy(
                            amount = toAccountEntity.amount + (previousAmountToDetect * -1),
                        ),
                    )
                }
            }
        }
    }

    @Transaction
    suspend fun updateTransaction(
        transactionEntity: TransactionEntity,
        amountToDetect: Double,
        isTransfer: Boolean,
        attachmentPaths: List<String> = emptyList(),
    ) {
        update(transactionEntity)

        removeTransactionAttachments(transactionEntity.id)
        attachmentPaths.forEach { path ->
            insertTransactionAttachment(
                TransactionAttachmentEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = transactionEntity.id,
                    imagePath = path,
                    createdOn = Date(),
                ),
            )
        }

        val accountEntity = findAccountById(transactionEntity.fromAccountId)
        if (accountEntity != null) {
            updateAccount(
                accountEntity.copy(
                    amount = accountEntity.amount + amountToDetect,
                ),
            )
        }
        if (isTransfer && transactionEntity.toAccountId?.isNotBlank() == true) {
            val toAccountEntity = findAccountById(
                transactionEntity.toAccountId!!,
            )
            if (toAccountEntity != null) {
                updateAccount(
                    toAccountEntity.copy(
                        amount = toAccountEntity.amount + (amountToDetect * -1),
                    ),
                )
            }
        }
    }
}
