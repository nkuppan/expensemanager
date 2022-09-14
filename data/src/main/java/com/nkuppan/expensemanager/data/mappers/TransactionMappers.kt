package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.data.db.entity.TransactionEntity

class TransactionDomainEntityMapper : Mapper<Transaction, TransactionEntity> {
    override fun convert(fromObject: Transaction): TransactionEntity {
        return TransactionEntity(
            id = fromObject.id,
            notes = fromObject.notes,
            categoryId = fromObject.categoryId,
            accountId = fromObject.accountId,
            amount = fromObject.amount,
            imagePath = fromObject.imagePath,
            createdOn = fromObject.createdOn,
            updatedOn = fromObject.updatedOn,
        )
    }
}

class TransactionEntityDomainMapper : Mapper<TransactionEntity, Transaction> {
    override fun convert(fromObject: TransactionEntity): Transaction {
        return Transaction(
            id = fromObject.id,
            notes = fromObject.notes,
            categoryId = fromObject.categoryId,
            accountId = fromObject.accountId,
            amount = fromObject.amount,
            imagePath = fromObject.imagePath,
            createdOn = fromObject.createdOn,
            updatedOn = fromObject.updatedOn
        )
    }
}