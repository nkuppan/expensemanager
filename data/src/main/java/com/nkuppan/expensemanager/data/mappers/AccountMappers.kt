package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.core.model.Account
import com.nkuppan.expensemanager.core.model.PaymentMode
import com.nkuppan.expensemanager.data.db.entity.AccountEntity


class AccountDomainEntityMapper : Mapper<Account, AccountEntity> {
    override fun convert(fromObject: Account): AccountEntity {
        return AccountEntity(
            id = fromObject.id,
            name = fromObject.name,
            type = fromObject.type.ordinal,
            backgroundColor = fromObject.backgroundColor,
            createdOn = fromObject.createdOn,
            updatedOn = fromObject.updatedOn,
        )
    }
}

class AccountEntityDomainMapper : Mapper<AccountEntity, Account> {
    override fun convert(fromObject: AccountEntity): Account {
        return Account(
            id = fromObject.id,
            name = fromObject.name,
            type = PaymentMode.values()[fromObject.type],
            backgroundColor = fromObject.backgroundColor,
            createdOn = fromObject.createdOn,
            updatedOn = fromObject.updatedOn,
        )
    }
}