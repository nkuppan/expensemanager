package com.naveenapps.expensemanager.feature.account.create

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.R
import com.naveenapps.expensemanager.core.domain.usecase.account.AddAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.CheckAccountValidationUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.DeleteAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.FindAccountByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.UpdateAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.settings.data.repository.NumberFormatRepositoryImpl
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AccountCreateViewModelTest : BaseCoroutineTest() {

    private val accountRepository: AccountRepository = mock()
    private val currencyRepository: CurrencyRepository = mock()
    private val appComposeNavigator: AppComposeNavigator = mock()

    private val validate = CheckAccountValidationUseCase()

    private val getDefaultCurrencyUseCase = GetDefaultCurrencyUseCase(currencyRepository)
    private val getCurrencyUseCase = GetCurrencyUseCase(currencyRepository)
    private val getFormattedAmountUseCase = GetFormattedAmountUseCase(currencyRepository)
    private val findAccountByIdUseCase = FindAccountByIdUseCase(accountRepository)
    private val addAccountUseCase = AddAccountUseCase(accountRepository, validate)
    private val deleteAccountUseCase = DeleteAccountUseCase(accountRepository, validate)
    private val updateAccountUseCase = UpdateAccountUseCase(accountRepository, validate)
    private val numberFormatRepository = NumberFormatRepositoryImpl(
        coroutineScope = CoroutineScope(testCoroutineDispatcher.dispatcher),
        numberFormatSettingRepository = mock {
            whenever(it.getNumberFormatType()).thenReturn(flowOf(NumberFormatType.WITHOUT_ANY_SEPARATOR))
        }
    )

    private lateinit var accountCreateViewModel: AccountCreateViewModel

    private val defaultCurrency = Currency("$", "US Dollars")
    private val newCurrency = Currency("₹", "Indian Rupees")

    private val currencyFlow = MutableStateFlow(newCurrency)

    override fun onCreate() {
        super.onCreate()
        whenever(currencyRepository.getDefaultCurrency()).thenReturn(defaultCurrency)
        whenever(currencyRepository.getSelectedCurrency()).thenReturn(currencyFlow)

        whenever(currencyRepository.getFormattedCurrency(any())).thenAnswer {
            val amount = it.arguments[0] as Amount
            val amountString = "${amount.currency?.symbol} ${amount.amount}"
            return@thenAnswer Amount(
                amount.amount,
                amountString = if (amount.amount < 0) {
                    "-${amountString.replace("-", "")}"
                } else {
                    amountString
                }
            )
        }

        accountCreateViewModel = AccountCreateViewModel(
            savedStateHandle = SavedStateHandle(),
            getCurrencyUseCase = getCurrencyUseCase,
            getDefaultCurrencyUseCase = getDefaultCurrencyUseCase,
            getFormattedAmountUseCase = getFormattedAmountUseCase,
            findAccountByIdUseCase = findAccountByIdUseCase,
            addAccountUseCase = addAccountUseCase,
            updateAccountUseCase = updateAccountUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            composeNavigator = appComposeNavigator,
            numberFormatRepository = numberFormatRepository,
        )
    }

    @Test
    fun `when we select a icon it should reflect in the state`() = runTest {
        val iconName = "icon_name"

        accountCreateViewModel.state.test {
            val firstState = awaitItem()
            Truth.assertThat(firstState).isNotNull()
            Truth.assertThat(firstState.icon.value).isNotEmpty()
            Truth.assertThat(firstState.icon.value).isEqualTo("account_balance")

            accountCreateViewModel.state.value.icon.onValueChange?.invoke(iconName)

            val secondState = awaitItem()
            Truth.assertThat(secondState).isNotNull()
            Truth.assertThat(secondState.icon.value).isNotEmpty()
            Truth.assertThat(secondState.icon.value).isEqualTo(iconName)
        }
    }

    @Test
    fun `when we select a color it should reflect in the state`() = runTest {
        val selectedColor = "#000064"

        accountCreateViewModel.state.test {
            val firstState = awaitItem()
            Truth.assertThat(firstState).isNotNull()
            Truth.assertThat(firstState.color.value).isNotEmpty()
            Truth.assertThat(firstState.color.value).isEqualTo("#43A546")

            accountCreateViewModel.state.value.color.onValueChange?.invoke(selectedColor)

            val secondState = awaitItem()
            Truth.assertThat(secondState).isNotNull()
            Truth.assertThat(secondState.color.value).isNotEmpty()
            Truth.assertThat(secondState.color.value).isEqualTo(selectedColor)
        }
    }

    @Test
    fun `when the name change it should reflect in the state`() = runTest {
        val changedName = "Test"

        accountCreateViewModel.state.test {
            val firstState = awaitItem()
            Truth.assertThat(firstState).isNotNull()
            Truth.assertThat(firstState.name.value).isEmpty()
            Truth.assertThat(firstState.name.valueError).isFalse()

            accountCreateViewModel.state.value.name.onValueChange?.invoke(changedName)

            val secondState = awaitItem()
            Truth.assertThat(secondState).isNotNull()
            Truth.assertThat(secondState.name.value).isNotEmpty()
            Truth.assertThat(secondState.name.value).isEqualTo(changedName)
            Truth.assertThat(secondState.name.valueError).isFalse()

            accountCreateViewModel.state.value.name.onValueChange?.invoke("")

            val thirdState = awaitItem()
            Truth.assertThat(thirdState).isNotNull()
            Truth.assertThat(thirdState.name.value).isEmpty()
            Truth.assertThat(thirdState.name.valueError).isTrue()
        }
    }

    @Test
    fun `when the amount change it should reflect in the state`() = runTest {
        val changedAmount = "0.0"

        accountCreateViewModel.state.test {
            val firstState = awaitItem()
            Truth.assertThat(firstState).isNotNull()
            Truth.assertThat(firstState.amount.value).isEmpty()
            Truth.assertThat(firstState.amount.valueError).isFalse()

            accountCreateViewModel.state.value.amount.onValueChange?.invoke(changedAmount)

            val secondState = awaitItem()
            Truth.assertThat(secondState).isNotNull()
            Truth.assertThat(secondState.amount.value).isNotEmpty()
            Truth.assertThat(secondState.amount.value).isEqualTo(changedAmount)
            Truth.assertThat(secondState.amount.valueError).isFalse()

            accountCreateViewModel.state.value.amount.onValueChange?.invoke("")

            val thirdState = awaitItem()
            Truth.assertThat(thirdState).isNotNull()
            Truth.assertThat(thirdState.amount.value).isEmpty()
            Truth.assertThat(thirdState.amount.valueError).isTrue()
        }
    }

    @Test
    fun `when the amount and credit limit change it should reflect in the amount and amount background`() =
        runTest {
            accountCreateViewModel.state.test {
                val firstState = awaitItem()
                Truth.assertThat(firstState).isNotNull()

                val changedAmount = "10.0"
                accountCreateViewModel.state.value.amount.onValueChange?.invoke(changedAmount)

                val secondState = awaitItem()
                Truth.assertThat(secondState).isNotNull()
                Truth.assertThat(secondState.totalAmount).isNotEmpty()
                Truth.assertThat(secondState.totalAmount)
                    .isEqualTo("${newCurrency.symbol} $changedAmount")
                Truth.assertThat(secondState.totalAmountBackgroundColor)
                    .isEqualTo(R.color.green_500)

                val newNegativeAmount = "-10.0"
                accountCreateViewModel.state.value.amount.onValueChange?.invoke(newNegativeAmount)
                val thirdState = awaitItem()
                Truth.assertThat(thirdState).isNotNull()
                Truth.assertThat(thirdState.totalAmount).isNotEmpty()
                Truth.assertThat(thirdState.totalAmount)
                    .isEqualTo("-${newCurrency.symbol} ${(newNegativeAmount.replace("-", ""))}")
                Truth.assertThat(thirdState.totalAmountBackgroundColor).isEqualTo(R.color.red_500)

                val creditLimitChange = "30.0"
                val totalValueExpected = "30.0".toDouble() + newNegativeAmount.toDouble()
                accountCreateViewModel.state.value.creditLimit.onValueChange?.invoke(
                    creditLimitChange
                )
                val fourthState = awaitItem()
                Truth.assertThat(fourthState).isNotNull()
                Truth.assertThat(fourthState.totalAmount).isNotEmpty()
                Truth.assertThat(fourthState.totalAmount)
                    .isEqualTo("${newCurrency.symbol} ${(totalValueExpected)}")
                Truth.assertThat(fourthState.totalAmountBackgroundColor)
                    .isEqualTo(R.color.green_500)
            }
        }

    @Test
    fun `when the credit limit change it should reflect in the state`() = runTest {
        val changedAmount = "0.0"

        accountCreateViewModel.state.test {
            val firstState = awaitItem()
            Truth.assertThat(firstState).isNotNull()
            Truth.assertThat(firstState.creditLimit.value).isEmpty()
            Truth.assertThat(firstState.creditLimit.valueError).isFalse()

            accountCreateViewModel.state.value.creditLimit.onValueChange?.invoke(changedAmount)

            val secondState = awaitItem()
            Truth.assertThat(secondState).isNotNull()
            Truth.assertThat(secondState.creditLimit.value).isNotEmpty()
            Truth.assertThat(secondState.creditLimit.value).isEqualTo(changedAmount)
            Truth.assertThat(secondState.creditLimit.valueError).isFalse()

            accountCreateViewModel.state.value.creditLimit.onValueChange?.invoke("")

            val thirdState = awaitItem()
            Truth.assertThat(thirdState).isNotNull()
            Truth.assertThat(thirdState.creditLimit.value).isEmpty()
            Truth.assertThat(thirdState.creditLimit.valueError).isTrue()
        }
    }

    @Test
    fun `when currency is available it should be reflected in the state`() = runTest {
        accountCreateViewModel.state.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.currency).isNotNull()
            Truth.assertThat(firstItem.currency).isEqualTo(newCurrency)
        }
    }

    @Test
    fun `when calling the close page action then it should call the pop backstack`() = runTest {
        accountCreateViewModel.processAction(AccountCreateAction.ClosePage)
        verify(appComposeNavigator, times(1)).popBackStack()
    }


    @Test
    fun `when account is available and call delete action it should delete and pop backstack page`() =
        runTest {

            val accountId = "accountId"

            whenever(accountRepository.findAccount(accountId)).thenReturn(
                Resource.Success(FAKE_ACCOUNT)
            )

            whenever(accountRepository.deleteAccount(any())).thenReturn(Resource.Success(true))

            accountCreateViewModel = AccountCreateViewModel(
                savedStateHandle = SavedStateHandle(initialState = mapOf(ExpenseManagerArgsNames.ID to accountId)),
                getCurrencyUseCase = getCurrencyUseCase,
                getDefaultCurrencyUseCase = getDefaultCurrencyUseCase,
                getFormattedAmountUseCase = getFormattedAmountUseCase,
                findAccountByIdUseCase = findAccountByIdUseCase,
                addAccountUseCase = addAccountUseCase,
                updateAccountUseCase = updateAccountUseCase,
                deleteAccountUseCase = deleteAccountUseCase,
                composeNavigator = appComposeNavigator,
                numberFormatRepository = numberFormatRepository,
            )

            advanceUntilIdle()

            accountCreateViewModel.processAction(AccountCreateAction.Delete)

            advanceUntilIdle()

            verify(accountRepository, times(1)).deleteAccount(eq(FAKE_ACCOUNT))
            verify(appComposeNavigator, times(1)).popBackStack()
        }
}
