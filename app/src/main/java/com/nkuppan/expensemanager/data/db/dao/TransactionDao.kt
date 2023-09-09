package com.nkuppan.expensemanager.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.nkuppan.expensemanager.data.db.entity.TransactionEntity
import com.nkuppan.expensemanager.data.db.entity.TransactionRelation
import kotlinx.coroutines.flow.Flow


@Dao
interface TransactionDao : BaseDao<TransactionEntity> {

    @Query("SELECT * from `transaction` WHERE id=:id")
    fun findById(id: String): TransactionEntity?

    @Query("SELECT * from `transaction` WHERE account_id=:accountId ORDER BY `transaction`.created_on DESC")
    fun getTransactionsByAccountId(accountId: String): Flow<List<TransactionEntity>?>

    @Query(
        """
        SELECT 
            `transaction`.*, 
            `category`.name, `category`.background_color, `category`.is_favorite,`category`.type,
            `account`.name, `account`.background_color, `account`.type
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id
        JOIN `account` ON account_id = `account`.id
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getAllTransaction(): Flow<List<TransactionRelation>?>

    @Query(
        """
        SELECT 
            `transaction`.*, 
            `category`.name, `category`.background_color, `category`.is_favorite,`category`.type,
            `account`.name, `account`.background_color, `account`.type
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id   
        JOIN `account` ON account_id = `account`.id
        WHERE `transaction`.created_on BETWEEN :fromDate AND :toDate
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getTransactionsByDateFilter(
        fromDate: Long,
        toDate: Long
    ): Flow<List<TransactionRelation>?>

    @Query(
        """
        SELECT 
            `transaction`.*, 
            `category`.name, `category`.background_color, `category`.is_favorite,`category`.type,
            `account`.name, `account`.background_color, `account`.type
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id
        JOIN `account` ON account_id = `account`.id
        WHERE `transaction`.account_id=:accountId 
        AND `transaction`.created_on BETWEEN :fromDate AND :toDate
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getTransactionsByAccountIdAndDateFilter(
        accountId: String,
        fromDate: Long,
        toDate: Long
    ): Flow<List<TransactionRelation>?>

    @Query(
        """
        SELECT 
            SUM(`transaction`.amount)
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id
        JOIN `account` ON account_id = `account`.id
        WHERE `transaction`.account_id=:accountId
        AND `transaction`.created_on BETWEEN :fromDate AND :toDate 
        AND `category`.type = :categoryType
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getTransactionTotalAmount(
        accountId: String,
        categoryType: Int,
        fromDate: Long,
        toDate: Long
    ): Flow<Double?>
}