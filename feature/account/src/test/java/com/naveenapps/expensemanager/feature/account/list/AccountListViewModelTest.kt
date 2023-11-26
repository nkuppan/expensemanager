package com.naveenapps.expensemanager.feature.account.list

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Account
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
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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

    private val accountFlow = MutableStateFlow<List<Account>>(emptyList())
    private val currencyFlow = MutableStateFlow(Currency("1", "1"))

    override fun onCreate() {
        whenever(currencyRepository.getSelectedCurrency()).thenReturn(currencyFlow)
        whenever(currencyRepository.getFormattedCurrency(any())).thenReturn(Amount(100.0, "100$"))
        whenever(accountRepository.getAccounts()).thenReturn(accountFlow)

        accountListViewModel = AccountListViewModel(
            getAllAccountsUseCase,
            getCurrencyUseCase,
            getFormattedAmountUseCase,
            appComposeNavigator
        )
    }


    @Test
    fun accountSuccess() = runTest {

        val totalCount = 20

        accountFlow.value = getRandomAccountData(totalCount)

        accountListViewModel.accounts.test {
            val loadingState = awaitItem()
            Truth.assertThat(loadingState).isNotNull()
            Truth.assertThat(loadingState).isInstanceOf(UiState.Loading::class.java)

            val state = awaitItem()
            Truth.assertThat(state).isNotNull()
            Truth.assertThat(state).isInstanceOf(UiState.Success::class.java)
            val accounts = (state as UiState.Success).data
            Truth.assertThat(accounts).isNotEmpty()
            Truth.assertThat(accounts.size).isEqualTo(totalCount)
        }
    }


    @Test
    fun accountEmpty() = runTest {

        accountFlow.value = emptyList()

        accountListViewModel.accounts.test {
            val loadingState = awaitItem()
            Truth.assertThat(loadingState).isNotNull()
            Truth.assertThat(loadingState).isInstanceOf(UiState.Loading::class.java)

            val state = awaitItem()
            Truth.assertThat(state).isNotNull()
            Truth.assertThat(state).isInstanceOf(UiState.Empty::class.java)
        }
    }

    @Test
    fun accountSuccessAndTypeSwitch() = runTest {

        val totalCount = 20

        val randomAccountData = getRandomAccountData(totalCount)
        val expectedCreditAccountCount = randomAccountData.count { it.type.isCredit() }
        val expectedRegularAccountCount = randomAccountData.count { it.type.isRegular() }
        accountFlow.value = randomAccountData

        accountListViewModel.accounts.test {
            val loadingState = awaitItem()
            Truth.assertThat(loadingState).isNotNull()
            Truth.assertThat(loadingState).isInstanceOf(UiState.Loading::class.java)

            val state = awaitItem()
            Truth.assertThat(state).isNotNull()
            Truth.assertThat(state).isInstanceOf(UiState.Success::class.java)
            val accounts = (state as UiState.Success).data
            Truth.assertThat(accounts).isNotEmpty()
            Truth.assertThat(accounts.size).isEqualTo(totalCount)

            val actualCreditAccounts = accounts.count { it.type.isCredit() }
            Truth.assertThat(actualCreditAccounts).isEqualTo(expectedCreditAccountCount)

            val actualRegularAccounts = accounts.count { it.type.isRegular() }
            Truth.assertThat(actualRegularAccounts).isEqualTo(expectedRegularAccountCount)
        }
    }

    @Test
    fun checkOpenCreateNavigation() = runTest {
        accountListViewModel.openCreateScreen("")
        verify(appComposeNavigator, times(1)).navigate(ArgumentMatchers.anyString(), eq(null))
    }

    @Test
    fun checkOpenCreateNavigationAndCommands() = runTest {
        accountListViewModel.openCreateScreen("")
        verify(appComposeNavigator, times(1)).navigate(ArgumentMatchers.anyString(), eq(null))
        appComposeNavigator
    }

    @Test
    fun checkClosePageNavigation() = runTest {
        accountListViewModel.closePage()
        verify(appComposeNavigator, times(1)).popBackStack()
    }
}