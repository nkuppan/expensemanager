package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.data.db.entity.BudgetEntity
import com.nkuppan.expensemanager.domain.model.Budget

fun Budget.toEntityModel(): BudgetEntity {
    return BudgetEntity(
        id = id,
        name = name,
        type = type,
        iconBackgroundColor = iconBackgroundColor,
        iconName = iconName,
        amount = amount,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun BudgetEntity.toDomainModel(): Budget {
    return Budget(
        id = id,
        name = name,
        type = type,
        iconBackgroundColor = iconBackgroundColor,
        iconName = iconName,
        amount = amount,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}