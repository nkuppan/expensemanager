package com.naveenapps.expensemanager.data.mappers

import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.model.Transaction

fun Transaction.toEntityModel(): TransactionEntity {
    return TransactionEntity(
        id = id,
        notes = notes,
        categoryId = categoryId,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        type = type,
        amount = amount,
        imagePath = imagePath,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        notes = notes,
        categoryId = categoryId,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        type = type,
        amount = amount,
        imagePath = imagePath,
        createdOn = createdOn,
        updatedOn = updatedOn
    )
}