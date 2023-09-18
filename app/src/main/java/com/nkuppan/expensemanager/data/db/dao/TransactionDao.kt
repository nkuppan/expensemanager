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

    @Query("SELECT * from `transaction` WHERE from_account_id=:accountId ORDER BY `transaction`.created_on DESC")
    fun getTransactionsByAccountId(accountId: String): Flow<List<TransactionEntity>?>

    @Query(
        """
        SELECT 
            `transaction`.*, 
            `category`.name, `category`.icon_background_color, `category`.icon_name,`category`.type,
            `fromAccount`.name, `fromAccount`.icon_background_color, `fromAccount`.type, `fromAccount`.icon_name
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id   
        JOIN `account` as fromAccount ON from_account_id = `fromAccount`.id
        ORDER BY `transaction`.created_on DESC
        """
    )
    fun getAllTransaction(): Flow<List<TransactionRelation>?>

    @Query(
        """
        SELECT 
            `transaction`.*, 
            `category`.name, `category`.icon_background_color, `category`.icon_name,`category`.type,
            `fromAccount`.name, `fromAccount`.icon_background_color, `fromAccount`.type, `fromAccount`.icon_name
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id   
        JOIN `account` as fromAccount ON from_account_id = `fromAccount`.id
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
            `category`.name, `category`.icon_background_color, `category`.icon_name,`category`.type,
            `fromAccount`.name, `fromAccount`.icon_background_color, `fromAccount`.type, `fromAccount`.icon_name
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id   
        JOIN `account` as fromAccount ON from_account_id = `fromAccount`.id
        WHERE `transaction`.from_account_id=:accountId 
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
        JOIN `account` ON from_account_id = `account`.id
        WHERE `transaction`.from_account_id=:accountId
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