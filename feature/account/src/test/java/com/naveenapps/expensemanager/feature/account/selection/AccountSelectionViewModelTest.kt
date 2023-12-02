package com.naveenapps.expensemanager.feature.account.selection

import androidx.compose.ui.util.fastAny
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AccountSelectionViewModelTest : BaseCoroutineTest() {

    private val accountRepository: AccountRepository = mock()
    private val currencyRepository: CurrencyRepository = mock()

    private val getCurrencyUseCase = GetCurrencyUseCase(currencyRepository)
    private val getFormattedAmountUseCase = GetFormattedAmountUseCase(currencyRepository)
    private val getAllAccountsUseCase = GetAllAccountsUseCase(accountRepository)

    private lateinit var accountSelectionViewModel: AccountSelectionViewModel

    private val accountFlow = MutableStateFlow(getRandomAccountData(10))
    private val currencyFlow = MutableStateFlow(Currency("1", "1"))

    override fun onCreate() {
        super.onCreate()

        whenever(currencyRepository.getSelectedCurrency()).thenReturn(currencyFlow)
        whenever(currencyRepository.getFormattedCurrency(any())).thenReturn(Amount(100.0, "100$"))
        whenever(accountRepository.getAccounts()).thenReturn(accountFlow)

        accountSelectionViewModel = AccountSelectionViewModel(
            getCurrencyUseCase,
            getFormattedAmountUseCase,
            getAllAccountsUseCase
        )
    }

    @Test
    fun whenAccountsAvailableState() = runTest {
        accountSelectionViewModel.accounts.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isEmpty()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isNotEmpty()
            Truth.assertThat(secondItem).hasSize(10)
        }
    }

    @Test
    fun whenSelectedAccountsAvailableState() = runTest {
        accountSelectionViewModel.selectedAccounts.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isEmpty()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isNotEmpty()
            Truth.assertThat(secondItem).hasSize(10)
        }
    }

    @Test
    fun whenClearAllShouldRemoveAllSelectedAccounts() = runTest {
        accountSelectionViewModel.selectedAccounts.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isEmpty()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isNotEmpty()
            Truth.assertThat(secondItem).hasSize(10)

            accountSelectionViewModel.clearChanges()

            val thirdItem = awaitItem()
            Truth.assertThat(thirdItem).isNotNull()
            Truth.assertThat(thirdItem).isEmpty()
        }
    }

    @Test
    fun whenDeSelectingTheAccountShouldReflectInSelectedAccounts() = runTest {
        accountSelectionViewModel.selectedAccounts.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isEmpty()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isNotEmpty()
            Truth.assertThat(secondItem).hasSize(10)

            val removedAccount = accountSelectionViewModel.selectedAccounts.value.first()

            accountSelectionViewModel.selectThisAccount(removedAccount, selected = false)

            val thirdItem = awaitItem()
            Truth.assertThat(thirdItem).isNotNull()
            Truth.assertThat(thirdItem).isNotEmpty()
            Truth.assertThat(thirdItem).hasSize(9)
            Truth.assertThat(thirdItem.any { it.id == removedAccount.id }).isFalse()

            accountSelectionViewModel.selectThisAccount(removedAccount, selected = true)

            val fourthItem = awaitItem()
            Truth.assertThat(fourthItem).isNotNull()
            Truth.assertThat(fourthItem).isNotEmpty()
            Truth.assertThat(fourthItem).hasSize(10)
            Truth.assertThat(fourthItem.fastAny { it.id == removedAccount.id }).isTrue()
        }
    }

    @Test
    fun whenSelectingAccountsAtBeginningShouldReflect() = runTest {
        accountSelectionViewModel.selectedAccounts.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isEmpty()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isNotEmpty()
            Truth.assertThat(secondItem).hasSize(10)

            val selectThisAccountsOnly = accountSelectionViewModel.selectedAccounts.value.take(5)

            accountSelectionViewModel.selectAllThisAccount(selectThisAccountsOnly)

            //Clearing the selection
            val thirdItem = awaitItem()
            Truth.assertThat(thirdItem).isNotNull()
            Truth.assertThat(thirdItem).isEmpty()

            //Applying the new selection
            val fourthItem = awaitItem()
            Truth.assertThat(fourthItem).isNotNull()
            Truth.assertThat(fourthItem).isNotEmpty()
            Truth.assertThat(fourthItem).hasSize(5)
        }
    }

    @Test
    fun whenSelectingEmptyAccountsAtBeginningShouldNotReflect() = runTest {
        accountSelectionViewModel.selectedAccounts.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isEmpty()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isNotEmpty()
            Truth.assertThat(secondItem).hasSize(10)

            accountSelectionViewModel.selectAllThisAccount(emptyList())
        }
    }
}