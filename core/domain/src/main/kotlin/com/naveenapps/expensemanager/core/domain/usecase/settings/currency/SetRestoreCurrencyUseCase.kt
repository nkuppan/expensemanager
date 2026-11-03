package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.repository.CurrencyRepository

/**
 * Records which currency a backup's amounts are in before it's restored (null clears it after
 * the next start). Only used when the backup predates multi-currency and records none.
 */
class SetRestoreCurrencyUseCase(private val repository: CurrencyRepository) {
    suspend operator fun invoke(currencyCode: String?) {
        repository.setRestoreCurrencyCode(currencyCode?.takeIf { it.isNotBlank() })
    }
}
