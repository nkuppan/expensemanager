package com.naveenapps.expensemanager.core.domain.usecase.account

import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.toAccountUiModel

/** An account ready for display, with its balance (and credit limit) in its own currency. */
class GetAccountUiModelUseCase(
    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase,
) {
    suspend operator fun invoke(account: Account): AccountUiModel = account.toAccountUiModel(
        formatAmountInCurrencyUseCase(account.amount, account.currencyCode),
        if (account.type == AccountType.CREDIT) {
            formatAmountInCurrencyUseCase(account.getAvailableCreditLimit(), account.currencyCode)
        } else {
            null
        },
    )
}
