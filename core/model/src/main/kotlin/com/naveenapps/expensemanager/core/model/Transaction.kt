package com.naveenapps.expensemanager.core.model

import java.util.Date

data class Transaction(
    val id: String,
    val notes: String,
    val categoryId: String,
    val fromAccountId: String,
    val toAccountId: String?,
    val amount: Amount,
    val imagePath: String,
    val type: TransactionType,
    val createdOn: Date,
    val updatedOn: Date,
    /** Currency of [amount]: the from-account's currency. */
    val fromCurrencyCode: String,
    /** Currency credited to the to-account; equals [fromCurrencyCode] unless a cross-currency transfer. */
    val toCurrencyCode: String,
    /** The rate used for a cross-currency transfer (to-account gets `amount * rate`); null = 1.0. */
    val rateSnapshot: ExchangeRateSnapshot? = null,
    var category: Category = Category(
        "",
        "",
        CategoryType.INCOME,
        StoredIcon("", ""),
        Date(),
        Date(),
    ),
    var fromAccount: Account = Account(
        "",
        "",
        AccountType.REGULAR,
        StoredIcon("", ""),
        Date(),
        Date(),
        currencyCode = "",
    ),
    var toAccount: Account? = null,
    var attachments: List<String> = emptyList(),
)
