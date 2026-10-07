package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.RecurringTransactionEntity
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.RecurringTransaction

fun RecurringTransaction.toEntityModel(): RecurringTransactionEntity = RecurringTransactionEntity(
    id = id,
    notes = notes,
    categoryId = categoryId,
    fromAccountId = fromAccountId,
    toAccountId = toAccountId,
    type = type,
    amount = amount,
    frequency = frequency.name,
    startDate = startDate,
    nextOccurrence = nextOccurrence,
    nextDueDate = nextDueDate,
    createdOn = createdOn,
    updatedOn = updatedOn,
)

/** Null for a frequency this app version doesn't know (e.g. restored from a newer backup). */
fun RecurringTransactionEntity.toDomainModel(): RecurringTransaction? {
    val frequency = RecurringFrequency.entries.firstOrNull { it.name == frequency } ?: return null
    return RecurringTransaction(
        id = id,
        notes = notes,
        categoryId = categoryId,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        type = type,
        amount = amount,
        frequency = frequency,
        startDate = startDate,
        nextOccurrence = nextOccurrence,
        nextDueDate = nextDueDate,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}
