package com.naveenapps.expensemanager.feature.onboarding

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Country

sealed class OnboardingAction {

    /** "Explore the app first": finish setup and land on Home. */
    data object Next : OnboardingAction()

    /** Primary CTA: finish setup and open the new-transaction screen straight away. */
    data object AddFirstExpense : OnboardingAction()

    /** Accounts come preloaded; this opens the full list to rename, add or remove. */
    data object OpenAccounts : OnboardingAction()

    data class AccountCreate(val account: AccountUiModel?) : OnboardingAction()

    data object ShowCurrencySelection : OnboardingAction()

    data object DismissCurrencySelection : OnboardingAction()

    data class SelectCurrency(val country: Country) : OnboardingAction()
}
