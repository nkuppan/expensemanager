package com.naveenapps.expensemanager.feature.onboarding

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Country

sealed class OnboardingAction {

    data object Next : OnboardingAction()

    data class AccountCreate(val account: AccountUiModel?) : OnboardingAction()

    data object ShowCurrencySelection : OnboardingAction()

    data object DismissCurrencySelection : OnboardingAction()

    data class SelectCurrency(val country: Country) : OnboardingAction()
}