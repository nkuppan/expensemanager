package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import java.util.Date
import kotlinx.coroutines.flow.Flow

/**
 * Accounts are immutable after creation except for their appearance, list order and archived
 * flag, so those are the only columns the update queries below can touch. The balance
 * (`amount`) is only ever changed by [TransactionDao] as transactions are applied/reverted.
 */
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

    @Query(
        "UPDATE account SET icon_name = :iconName, icon_background_color = :iconBackgroundColor, " +
            "custom_image_path = :customImagePath, updated_on = :updatedOn WHERE id = :id",
    )
    suspend fun updateAppearance(
        id: String,
        iconName: String,
        iconBackgroundColor: String,
        customImagePath: String?,
        updatedOn: Date,
    ): Int

    @Query("UPDATE account SET sequence = :sequence WHERE id = :id")
    suspend fun updateSequence(id: String, sequence: Int): Int

    @Transaction
    suspend fun updateSequences(sequences: Map<String, Int>) {
        sequences.forEach { (id, sequence) -> updateSequence(id, sequence) }
    }

    @Query("UPDATE account SET is_archived = :archived, updated_on = :updatedOn WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, updatedOn: Date): Int

    @Query(
        "SELECT EXISTS(SELECT 1 FROM `transaction` " +
            "WHERE from_account_id = :id OR to_account_id = :id)",
    )
    suspend fun hasTransactions(id: String): Boolean
}
