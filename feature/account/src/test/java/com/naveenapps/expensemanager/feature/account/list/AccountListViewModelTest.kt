package com.naveenapps.expensemanager.feature.account.list

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.isCredit
import com.naveenapps.expensemanager.core.model.isRegular
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AccountListViewModelTest : BaseCoroutineTest() {

    private val accountRepository: AccountRepository = mock()
    private val currencyRepository: CurrencyRepository = mock()

    private val getCurrencyUseCase = GetCurrencyUseCase(currencyRepository)
    private val getFormattedAmountUseCase = GetFormattedAmountUseCase(currencyRepository)
    private val getAllAccountsUseCase = GetAllAccountsUseCase(accountRepository)

    private val appComposeNavigator: AppComposeNavigator = mock()

    private lateinit var accountListViewModel: AccountListViewModel

    private val accountFlow = MutableStateFlow(emptyList<Account>())
    private val currencyFlow = MutableStateFlow(Currency("1", "1"))

    override fun onCreate() {
        whenever(currencyRepository.getSelectedCurrency()).thenReturn(currencyFlow)
        whenever(currencyRepository.getFormattedCurrency(any())).thenReturn(Amount(100.0, "100$"))
        whenever(accountRepository.getAccounts()).thenReturn(accountFlow)

        accountListViewModel = AccountListViewModel(
            getAllAccountsUseCase,
            getCurrencyUseCase,
            getFormattedAmountUseCase,
            appComposeNavigator,
        )
    }

    @Test
    fun accountSuccess() = runTest {
        val totalCount = 20

        accountFlow.value = getRandomAccountData(totalCount)

        accountListViewModel.state.test {
            val firstState = awaitItem()
            Truth.assertThat(firstState).isNotNull()
            Truth.assertThat(firstState.accounts).isEmpty()

            val secondState = awaitItem()
            Truth.assertThat(secondState).isNotNull()
            Truth.assertThat(secondState.accounts).isNotEmpty()
            Truth.assertThat(secondState.accounts).hasSize(totalCount)
        }
    }

    @Test
    fun summaryShouldSplitAssetsLiabilitiesAndNetTotal() = runTest {
        accountFlow.value = listOf(
            getAccountData(1, AccountType.REGULAR, amount = 500.0, creditLimit = 0.0),
            getAccountData(2, AccountType.REGULAR, amount = 250.0, creditLimit = 0.0),
            getAccountData(3, AccountType.CREDIT, amount = -150.0, creditLimit = 1000.0),
        )

        accountListViewModel.state.test {
            awaitItem()

            val state = awaitItem()
            Truth.assertThat(state.accounts).hasSize(3)
            Truth.assertThat(state.totalAmount).isNotNull()
            Truth.assertThat(state.assetsAmount).isNotNull()
            Truth.assertThat(state.liabilitiesAmount).isNotNull()
            // Assets 500 + 250, liabilities 150 (credit spend), net total 600.
            verify(currencyRepository).getFormattedCurrency(
                org.mockito.kotlin.argThat { amount == 750.0 },
            )
            verify(currencyRepository).getFormattedCurrency(
                org.mockito.kotlin.argThat { amount == 150.0 },
            )
            verify(currencyRepository).getFormattedCurrency(
                org.mockito.kotlin.argThat { amount == 600.0 },
            )
        }
    }

    @Test
    fun accountEmpty() = runTest {
        accountFlow.value = emptyList()

        accountListViewModel.state.test {
            val firstState = awaitItem()
            Truth.assertThat(firstState).isNotNull()
            Truth.assertThat(firstState.accounts).isEmpty()
        }
    }

    @Test
    fun accountSuccessAndTypeSwitch() = runTest {
        val totalCount = 20

        val randomAccountData = getRandomAccountData(totalCount)
        val expectedCreditAccountCount = randomAccountData.count { it.type.isCredit() }
        val expectedRegularAccountCount = randomAccountData.count { it.type.isRegular() }
        accountFlow.value = randomAccountData

        accountListViewModel.state.test {
            val firstState = awaitItem()
            Truth.assertThat(firstState).isNotNull()
            Truth.assertThat(firstState.accounts).isEmpty()

            val secondState = awaitItem()
            Truth.assertThat(secondState).isNotNull()
            val accounts = secondState.accounts
            Truth.assertThat(accounts).isNotEmpty()
            Truth.assertThat(accounts).hasSize(totalCount)

            val actualCreditAccounts = accounts.count { it.type.isCredit() }
            Truth.assertThat(actualCreditAccounts).isEqualTo(expectedCreditAccountCount)

            val actualRegularAccounts = accounts.count { it.type.isRegular() }
            Truth.assertThat(actualRegularAccounts).isEqualTo(expectedRegularAccountCount)
        }
    }

    @Test
    fun checkOpenCreateNavigation() = runTest {
        accountListViewModel.processAction(
            AccountListAction.EditAccount(getRandomAccountUiModel(1).first()),
        )
        verify(appComposeNavigator, times(1)).navigate(any())
    }

    @Test
    fun checkOpenCreateNavigationAndCommands() = runTest {
        accountListViewModel.processAction(AccountListAction.CreateAccount)
        verify(appComposeNavigator, times(1)).navigate(any())
    }

    @Test
    fun checkClosePageNavigation() = runTest {
        accountListViewModel.processAction(AccountListAction.ClosePage)
        verify(appComposeNavigator, times(1)).popBackStack()
    }
}
