package com.naveenapps.expensemanager.core.model

/**
 * What deleting an account would remove. Transactions reference accounts with
 * ON DELETE CASCADE, so every transaction from or to the account goes with it.
 */
data class AccountDeleteImpact(
    val transactionCount: Int,
    val isLastAccount: Boolean,
)

/** Raised when trying to delete the only remaining account; every transaction needs one. */
class LastAccountDeletionException : IllegalStateException("The last remaining account can't be deleted")
