package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.PaymentMode
import com.nkuppan.expensemanager.data.db.entity.AccountEntity

fun Account.toEntityModel(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type.ordinal,
        backgroundColor = backgroundColor,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun AccountEntity.toDomainModel(): Account {
    return Account(
        id = id,
        name = name,
        type = PaymentMode.values()[type],
        backgroundColor = backgroundColor,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}