package com.naveenapps.expensemanager.data.mappers

import com.naveenapps.expensemanager.core.database.entity.BudgetEntity
import com.naveenapps.expensemanager.core.model.Budget

fun Budget.toEntityModel(): BudgetEntity {
    return BudgetEntity(
        id = id,
        name = name,
        iconBackgroundColor = iconBackgroundColor,
        iconName = iconName,
        amount = amount,
        selectedMonth = selectedMonth,
        isAllCategoriesSelected = isAllCategoriesSelected,
        isAllAccountsSelected = isAllAccountsSelected,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun BudgetEntity.toDomainModel(categories: List<String>, accounts: List<String>): Budget {
    return Budget(
        id = id,
        name = name,
        iconBackgroundColor = iconBackgroundColor,
        iconName = iconName,
        amount = amount,
        selectedMonth = selectedMonth,
        categories = categories,
        accounts = accounts,
        isAllCategoriesSelected = isAllCategoriesSelected,
        isAllAccountsSelected = isAllAccountsSelected,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}