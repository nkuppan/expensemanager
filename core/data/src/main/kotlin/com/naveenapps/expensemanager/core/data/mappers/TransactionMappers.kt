package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Transaction

fun Transaction.toEntityModel(): TransactionEntity = TransactionEntity(
    id = id,
    notes = notes,
    categoryId = categoryId,
    fromAccountId = fromAccountId,
    toAccountId = toAccountId,
    type = type,
    amount = amount.amount,
    imagePath = imagePath,
    createdOn = createdOn,
    updatedOn = updatedOn,
)

fun TransactionEntity.toDomainModel(): Transaction = Transaction(
    id = id,
    notes = notes,
    categoryId = categoryId,
    fromAccountId = fromAccountId,
    toAccountId = toAccountId,
    type = type,
    amount = Amount(amount),
    imagePath = imagePath,
    createdOn = createdOn,
    updatedOn = updatedOn,
)
