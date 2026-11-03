package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.isTransfer
import com.naveenapps.expensemanager.core.repository.AccountRepository
import java.util.Calendar
import java.util.Date

/**
 * The single source of truth for what a storable transaction looks like, checked against the
 * accounts as they are stored (never against whatever the UI believes):
 *
 * - its currencies are exactly its accounts' currencies (accounts can't change currency, so
 *   this holds forever once saved);
 * - a rate snapshot exists if and only if the two currencies differ, and it is for that exact
 *   pair with a positive rate;
 * - a fetched rate may only be used for a transaction dated the day it was fetched: backdated
 *   or re-dated transfers need a user-entered rate (the user's own fixed rate has no date and
 *   is accepted for any day);
 * - archived accounts are history only: nothing new may touch them.
 */
class CheckTransactionValidationUseCase(
    private val accountRepository: AccountRepository,
) {

    suspend operator fun invoke(transaction: Transaction): Resource<Boolean> {
        if (transaction.id.isBlank()) return error("ID shouldn't be blank")
        if (transaction.categoryId.isBlank()) return error("Category shouldn't be blank")
        if (transaction.amount.amount <= 0.0) return error("Amount should be greater than 0")

        val fromAccount = findAccount(transaction.fromAccountId)
            ?: return error("The selected account doesn't exist")
        if (fromAccount.isArchived) return error("\"${fromAccount.name}\" is archived")
        if (transaction.fromCurrencyCode != fromAccount.currencyCode) {
            return error("Transaction currency must be the account's currency")
        }

        if (transaction.type.isTransfer()) {
            val toAccountId = transaction.toAccountId
                ?: return error("A transfer needs a destination account")
            if (toAccountId == transaction.fromAccountId) {
                return error("Can't transfer to the same account")
            }
            val toAccount = findAccount(toAccountId)
                ?: return error("The destination account doesn't exist")
            if (toAccount.isArchived) return error("\"${toAccount.name}\" is archived")
            if (transaction.toCurrencyCode != toAccount.currencyCode) {
                return error("Destination currency must be the destination account's currency")
            }
        } else if (transaction.toCurrencyCode != transaction.fromCurrencyCode) {
            return error("Only transfers can involve two currencies")
        }

        val snapshot = transaction.rateSnapshot
        if (transaction.fromCurrencyCode == transaction.toCurrencyCode) {
            if (snapshot != null) return error("A same-currency transaction has no exchange rate")
            return Resource.Success(true)
        }

        snapshot ?: return error(
            "Enter the exchange rate from ${transaction.fromCurrencyCode} " +
                "to ${transaction.toCurrencyCode}",
        )
        if (snapshot.baseCode != transaction.fromCurrencyCode ||
            snapshot.quoteCode != transaction.toCurrencyCode
        ) {
            return error("The exchange rate is for a different currency pair")
        }
        if (!snapshot.rate.isFinite() || snapshot.rate <= 0.0) {
            return error("The exchange rate must be greater than 0")
        }
        if (snapshot.source == ExchangeRateSource.FETCHED &&
            !isSameDay(Date(snapshot.capturedAt), transaction.createdOn)
        ) {
            return error("A fetched rate can only be used for today's date: enter the rate manually")
        }
        return Resource.Success(true)
    }

    private suspend fun findAccount(id: String): Account? = (accountRepository.findAccount(id) as? Resource.Success)?.data

    private fun error(message: String) = Resource.Error(Exception(message))

    private fun isSameDay(first: Date, second: Date): Boolean {
        val a = Calendar.getInstance().apply { time = first }
        val b = Calendar.getInstance().apply { time = second }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }
}
