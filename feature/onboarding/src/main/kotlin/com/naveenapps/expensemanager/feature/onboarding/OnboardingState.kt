package com.naveenapps.expensemanager.feature.onboarding

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Currency

data class OnboardingState(
    val currency: Currency,
    val accounts: List<AccountUiModel>,
    val showCurrencySelection: Boolean
)