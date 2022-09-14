package com.nkuppan.expensemanager.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.nkuppan.expensemanager.data.db.entity.TransactionEntity
import com.nkuppan.expensemanager.data.db.entity.TransactionRelation


@Dao
interface TransactionDao : BaseDao<TransactionEntity> {

    @Query("SELECT * from `transaction` WHERE id=:id")
    fun findById(id: String): TransactionEntity?

    @Query("SELECT * from `transaction` WHERE account_id=:accountId")
    fun getTransactionsByAccountId(accountId: String): List<TransactionEntity>?

    @Query(
        """
        SELECT 
            `transaction`.*, 
            `category`.name, `category`.background_color, `category`.is_favorite,`category`.type,
            `account`.name, `account`.background_color, `account`.type
        FROM `transaction`
        JOIN `category` ON category_id = `category`.id
        JOIN `account` ON account_id = `account`.id
        """
    )
    fun getAllTransaction(): List<TransactionRelation>?

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
        """
    )
    fun getTransactionsByDateFilter(
        fromDate: Long,
        toDate: Long
    ): List<TransactionRelation>?

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
        """
    )
    fun getTransactionsByAccountIdAndDateFilter(
        accountId: String,
        fromDate: Long,
        toDate: Long
    ): List<TransactionRelation>?
}