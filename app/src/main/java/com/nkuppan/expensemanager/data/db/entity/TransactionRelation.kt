package com.nkuppan.expensemanager.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionRelation(
    @Embedded val transactionEntity: TransactionEntity,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val categoryEntity: CategoryEntity,
    @Relation(
        parentColumn = "account_id",
        entityColumn = "id"
    )
    val accountEntity: AccountEntity
)
