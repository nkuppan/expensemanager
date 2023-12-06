package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.StoredIcon

fun Account.toEntityModel(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type,
        iconBackgroundColor = storedIcon.backgroundColor,
        iconName = storedIcon.name,
        amount = amount,
        creditLimit = creditLimit,
        sequence = sequence,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun AccountEntity.toDomainModel(): Account {
    return Account(
        id = id,
        name = name,
        type = type,
        storedIcon = StoredIcon(
            name = iconName,
            backgroundColor = iconBackgroundColor,
        ),
        amount = amount,
        creditLimit = creditLimit,
        sequence = sequence,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}
