package com.naveenapps.expensemanager.core.domain.usecase.receipt

import java.util.Locale

/**
 * Finds the account a receipt was paid from using what the receipt itself says: the card or
 * account's last digits ("XX1234" matches an account named "HDFC 1234") or a distinctive word of
 * the account's name ("HDFC Bank" on the receipt matches "HDFC Savings"). Returns null unless one
 * account is the clear best match, so a wrong guess is rare.
 */
object ReceiptAccountMatcher {

    fun <T> match(
        receiptText: String,
        payee: String?,
        accountDigits: List<String>,
        accounts: List<T>,
        name: (T) -> String,
    ): T? {
        val text = receiptText.lowercase(Locale.ROOT)
        // Words of the payee's name say who was paid, not which account paid.
        val payeeWords = words(payee.orEmpty()).toSet()
        val scored = accounts.map { account ->
            val accountName = name(account).lowercase(Locale.ROOT)
            val digitsMatch = accountDigits.any { it in accountName }
            val wordMatch = words(accountName)
                .filter { it.length >= MIN_WORD && it !in GENERIC_WORDS && it !in payeeWords }
                .any { Regex("""\b${Regex.escape(it)}\b""").containsMatchIn(text) }
            account to (if (digitsMatch) DIGITS_SCORE else 0) + (if (wordMatch) WORD_SCORE else 0)
        }
        val best = scored.maxOfOrNull { it.second } ?: return null
        if (best == 0) return null
        return scored.filter { it.second == best }.singleOrNull()?.first
    }

    private val WORD_SEPARATOR = Regex("""[^\p{L}\p{N}]+""")

    private fun words(text: String) = text.lowercase(Locale.ROOT).split(WORD_SEPARATOR).filter { it.isNotBlank() }

    private const val MIN_WORD = 3
    private const val DIGITS_SCORE = 2
    private const val WORD_SCORE = 1

    // Words in many account names that say nothing about which bank or card it is.
    private val GENERIC_WORDS = setOf(
        "bank", "account", "acct", "savings", "saving", "current", "card", "credit", "debit", "salary",
        "wallet", "cash", "upi", "main", "personal", "the", "and", "pay", "payment", "payments", "joint",
    )
}
