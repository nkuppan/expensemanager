package com.nkuppan.expensemanager.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.nkuppan.expensemanager.data.db.entity.AccountEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface AccountDao : BaseDao<AccountEntity> {

    @Query("SELECT * FROM account ORDER BY created_on DESC")
    fun getAccounts(): Flow<List<AccountEntity>?>

    @Query("SELECT * FROM account ORDER BY created_on DESC")
    fun getAllValues(): List<AccountEntity>?

    @Query("SELECT * FROM account WHERE id = :id")
    fun findById(id: String): AccountEntity?

    @Query("SELECT COUNT(*) FROM account")
    fun getCount(): Long
}