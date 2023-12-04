package com.naveenapps.expensemanager.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionRelation(
    @Embedded val transactionEntity: TransactionEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id",
    )
    val categoryEntity: CategoryEntity,
    @Relation(
        parentColumn = "from_account_id",
        entityColumn = "id",
    )
    val fromAccountEntity: AccountEntity,
    @Relation(
        parentColumn = "to_account_id",
        entityColumn = "id",
    )
    val toAccountEntity: AccountEntity?,
)
