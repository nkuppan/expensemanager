package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao : BaseDao<AccountEntity> {

    @Query("SELECT * FROM account ORDER BY sequence ASC")
    fun getAccounts(): Flow<List<AccountEntity>?>

    @Query("SELECT * FROM account WHERE id = :id")
    suspend fun findById(id: String): AccountEntity?

    @Query("SELECT COUNT(*) FROM account")
    suspend fun getAccountCount(): Int

    // Both sides count: transfers into this account are cascade-deleted with it too.
    @Query("SELECT COUNT(*) FROM `transaction` WHERE from_account_id = :id OR to_account_id = :id")
    suspend fun getTransactionCount(id: String): Int
}
