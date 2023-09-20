package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.data.db.entity.AccountEntity
import com.nkuppan.expensemanager.domain.model.Account

fun Account.toEntityModel(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type,
        iconBackgroundColor = iconBackgroundColor,
        iconName = iconName,
        amount = amount,
        creditLimit = creditLimit,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun AccountEntity.toDomainModel(): Account {
    return Account(
        id = id,
        name = name,
        type = type,
        iconBackgroundColor = iconBackgroundColor,
        iconName = iconName,
        amount = amount,
        creditLimit = creditLimit,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}