package com.nkuppan.expensemanager.domain.model

import java.io.Serializable
import java.util.Date

data class Transaction(
    val id: String,
    val notes: String,
    val categoryId: String,
    val fromAccountId: String,
    val toAccountId: String?,
    val amount: Double,
    val imagePath: String,
    val type: TransactionType,
    val createdOn: Date,
    val updatedOn: Date,
    var category: Category = Category(
        "", "", CategoryType.INCOME,
        "", "",
        Date(), Date()
    ),
    var fromAccount: Account = Account(
        "", "", AccountType.CASH, "",
        "",
        Date(), Date()
    ),
    var toAccount: Account? = null
) : Serializable