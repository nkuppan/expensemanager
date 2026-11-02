package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.ExchangeRateSnapshotEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.ExchangeRateSnapshot
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
    fromCurrencyCode = fromCurrencyCode,
    toCurrencyCode = toCurrencyCode,
    rateSnapshotId = rateSnapshot?.id,
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
    fromCurrencyCode = fromCurrencyCode,
    toCurrencyCode = toCurrencyCode,
)

fun ExchangeRateSnapshot.toEntityModel(): ExchangeRateSnapshotEntity = ExchangeRateSnapshotEntity(
    id = id,
    source = source,
    provider = provider,
    baseCode = baseCode,
    quoteCode = quoteCode,
    rate = rate,
    capturedAt = capturedAt,
)

fun ExchangeRateSnapshotEntity.toDomainModel(): ExchangeRateSnapshot = ExchangeRateSnapshot(
    id = id,
    source = source,
    provider = provider,
    baseCode = baseCode,
    quoteCode = quoteCode,
    rate = rate,
    capturedAt = capturedAt,
)
