package com.naveenapps.expensemanager.feature.onboarding

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Country

sealed class OnboardingAction {

    /**
     * The one "Get started" button: finish setup and open the new-transaction keypad on top of
     * Home. Closing it shows Home, so exploring is still one tap away. (A separate "Explore the
     * app first" button sent users to an empty Home, where many never logged anything.)
     */
    data object GetStarted : OnboardingAction()

    /** Accounts come preloaded; this opens the full list to rename, add or remove. */
    data object OpenAccounts : OnboardingAction()

    data class AccountCreate(val account: AccountUiModel?) : OnboardingAction()

    data object ShowCurrencySelection : OnboardingAction()

    data object DismissCurrencySelection : OnboardingAction()

    data class SelectCurrency(val country: Country) : OnboardingAction()
}
