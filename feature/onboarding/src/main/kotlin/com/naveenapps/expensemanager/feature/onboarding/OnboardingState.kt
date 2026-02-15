package com.naveenapps.expensemanager.feature.onboarding

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Currency

@Stable
data class OnboardingState(
    val currency: Currency,
    val accounts: List<AccountUiModel>,
    val showCurrencySelection: Boolean
)