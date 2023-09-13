package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.data.db.entity.AccountEntity
import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.AccountType

fun Account.toEntityModel(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type.ordinal,
        iconBackgroundColor = iconBackgroundColor,
        iconName = iconName,
        amount = amount,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun AccountEntity.toDomainModel(): Account {
    return Account(
        id = id,
        name = name,
        type = AccountType.values()[type],
        iconBackgroundColor = iconBackgroundColor,
        iconName = iconName,
        amount = amount,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}