package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.data.db.entity.AccountEntity
import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.PaymentMode

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