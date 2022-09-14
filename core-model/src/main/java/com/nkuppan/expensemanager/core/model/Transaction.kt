package com.nkuppan.expensemanager.core.model

import java.io.Serializable
import java.util.*

data class Transaction(
    val id: String,
    val notes: String,
    val categoryId: String,
    val accountId: String,
    val amount: Double,
    val imagePath: String,
    val createdOn: Date,
    val updatedOn: Date,
    var category: Category = Category(
        "", "", CategoryType.INCOME,
        false, "",
        Date(), Date()
    ),
    var account: Account = Account(
        "", "", PaymentMode.WALLET,
        "",
        Date(), Date()
    )
) : Serializable