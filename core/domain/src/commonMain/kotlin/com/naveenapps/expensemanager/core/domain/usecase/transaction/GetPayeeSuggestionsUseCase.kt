package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.TransactionRepository

/** What the user picked before for a payee: their categories (most used first) and last account. */
data class PayeeSuggestions(
    val categoryIds: List<String> = emptyList(),
    val accountId: String? = null,
)

/**
 * Suggestions from earlier [TransactionType] entries with the same notes, e.g. a receipt paid to
 * "Swiggy" gets the categories and account of the previous "Swiggy" entries. Notes are compared
 * trimmed and ignoring case.
 */
class GetPayeeSuggestionsUseCase(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(type: TransactionType, payee: String?): PayeeSuggestions {
        if (payee.isNullOrBlank() || type == TransactionType.TRANSFER) return PayeeSuggestions()
        return runCatching {
            PayeeSuggestions(
                categoryIds = repository.findCategoryIdsByNotes(type, payee, MAX_CATEGORIES),
                accountId = repository.findLatestAccountIdByNotes(type, payee),
            )
        }.getOrDefault(PayeeSuggestions())
    }

    companion object {
        const val MAX_CATEGORIES = 3
    }
}
