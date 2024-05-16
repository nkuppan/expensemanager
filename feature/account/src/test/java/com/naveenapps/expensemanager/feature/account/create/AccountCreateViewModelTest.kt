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
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_ACCOUNT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AccountCreateViewModelTest : BaseCoroutineTest() {

    private val accountRepository: AccountRepository = mock()
    private val currencyRepository: CurrencyRepository = mock()
    private val appComposeNavigator: AppComposeNavigator = mock()

    private val checkAccountValidationUseCase = CheckAccountValidationUseCase()

    private val getDefaultCurrencyUseCase = GetDefaultCurrencyUseCase(currencyRepository)
    private val getCurrencyUseCase = GetCurrencyUseCase(currencyRepository)
    private val getFormattedAmountUseCase = GetFormattedAmountUseCase(currencyRepository)
    private val findAccountByIdUseCase = FindAccountByIdUseCase(accountRepository)
    private val addAccountUseCase =
        AddAccountUseCase(accountRepository, checkAccountValidationUseCase)
    private val deleteAccountUseCase =
        DeleteAccountUseCase(accountRepository, checkAccountValidationUseCase)
    private val updateAccountUseCase =
        UpdateAccountUseCase(accountRepository, checkAccountValidationUseCase)

    private lateinit var accountCreateViewModel: AccountCreateViewModel

    private val currencyFlow = MutableStateFlow(Currency("$", "US Dollars"))

    override fun onCreate() {
        super.onCreate()
        whenever(currencyRepository.getSelectedCurrency()).thenReturn(currencyFlow)
        whenever(currencyRepository.getFormattedCurrency(any())).thenReturn(Amount(100.0, "100$"))

        accountCreateViewModel = AccountCreateViewModel(
            SavedStateHandle(),
            getCurrencyUseCase,
            getDefaultCurrencyUseCase,
            getFormattedAmountUseCase,
            findAccountByIdUseCase,
            addAccountUseCase,
            updateAccountUseCase,
            deleteAccountUseCase,
            appComposeNavigator,
        )
    }

    @Test
    fun whenAccountIconSetItShouldReflect() = runTest {
        val iconName = "icon_name"

        accountCreateViewModel.iconValueField.test {
            val firstIconName = awaitItem()
            Truth.assertThat(firstIconName).isNotNull()
            Truth.assertThat(firstIconName.value).isNotEmpty()
            Truth.assertThat(firstIconName.value).isEqualTo("account_balance")

            accountCreateViewModel.iconValueField.value.onValueChange?.invoke(iconName)

            val secondIconName = awaitItem()
            Truth.assertThat(secondIconName).isNotNull()
            Truth.assertThat(secondIconName.value).isNotEmpty()
            Truth.assertThat(secondIconName.value).isEqualTo(iconName)
        }
    }

    @Test
    fun whenAccountColorSetItShouldReflect() = runTest {
        val selectedColor = "#000064"

        accountCreateViewModel.colorValueField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.value).isNotEmpty()
            Truth.assertThat(firstItem.value).isEqualTo("#43A546")

            accountCreateViewModel.colorValueField.value.onValueChange?.invoke(selectedColor)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem.value).isNotEmpty()
            Truth.assertThat(secondItem.value).isEqualTo(selectedColor)
        }
    }

    @Test
    fun whenCurrencyIconItShouldReflect() = runTest {
        accountCreateViewModel.currencyIconField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.value).isNotEmpty()
            Truth.assertThat(firstItem.value).isEqualTo("$")
        }
    }

    @Test
    fun whenChangingAccountTypeItShouldReflect() = runTest {
        accountCreateViewModel.accountTypeField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.value).isEqualTo(AccountType.REGULAR)

            accountCreateViewModel.accountTypeField.value.onValueChange?.invoke(AccountType.CREDIT)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem.value).isEqualTo(AccountType.CREDIT)
        }
    }

    @Test
    fun whenChangingNameItShouldReflect() = runTest {
        accountCreateViewModel.nameField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem.value).isEmpty()

            val changedName = "Name"

            accountCreateViewModel.nameField.value.onValueChange?.invoke(changedName)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem.value).isEqualTo(changedName)

            accountCreateViewModel.nameField.value.onValueChange?.invoke(" ")

            val thirdItem = awaitItem()
            Truth.assertThat(thirdItem).isNotNull()
            Truth.assertThat(thirdItem.valueError).isTrue()
        }
    }

    @Test
    fun whenChangingCurrentBalanceItShouldReflect() = runTest {
        accountCreateViewModel.currentBalanceField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem.value).isEmpty()

            val changedCurrentBalance = "1000"

            accountCreateViewModel.currentBalanceField.value.onValueChange?.invoke(
                changedCurrentBalance
            )

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem.value).isEqualTo(changedCurrentBalance)

            accountCreateViewModel.currentBalanceField.value.onValueChange?.invoke("")

            val thirdItem = awaitItem()
            Truth.assertThat(thirdItem).isNotNull()
            Truth.assertThat(thirdItem.valueError).isTrue()
        }
    }

    @Test
    fun whenAccountIsAvailableOnSavedStateItShouldReflect() = runTest {

        val accountId = "accountId"

        whenever(accountRepository.findAccount(accountId)).thenReturn(
            Resource.Success(FAKE_ACCOUNT)
        )

        accountCreateViewModel = AccountCreateViewModel(
            SavedStateHandle(mapOf(ExpenseManagerArgsNames.ID to accountId)),
            getCurrencyUseCase,
            getDefaultCurrencyUseCase,
            getFormattedAmountUseCase,
            findAccountByIdUseCase,
            addAccountUseCase,
            updateAccountUseCase,
            deleteAccountUseCase,
            appComposeNavigator,
        )

        accountCreateViewModel.nameField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem.value).isEmpty()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem.value).isNotNull()
            Truth.assertThat(secondItem.value).isEqualTo(FAKE_ACCOUNT.name)
        }

        accountCreateViewModel.currentBalanceField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem.value).isNotEmpty()
            Truth.assertThat(firstItem.value).isEqualTo("0")
        }

        accountCreateViewModel.iconValueField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.value).isNotEmpty()
            Truth.assertThat(firstItem.value).isEqualTo(FAKE_ACCOUNT.storedIcon.name)
        }

        accountCreateViewModel.colorValueField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.value).isNotEmpty()
            Truth.assertThat(firstItem.value).isEqualTo(FAKE_ACCOUNT.storedIcon.backgroundColor)
        }

        accountCreateViewModel.accountTypeField.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.value).isEqualTo(FAKE_ACCOUNT.type)
        }
    }

    @Test
    fun whenSaveAccountWithoutEditModeItShouldReflect() = runTest {

        whenever(accountRepository.addAccount(any())).thenReturn(Resource.Success(true))

        accountCreateViewModel.nameField.value.onValueChange?.invoke("Account")
        accountCreateViewModel.iconValueField.value.onValueChange?.invoke("account_balance")
        accountCreateViewModel.colorValueField.value.onValueChange?.invoke("#FFFFFF")
        accountCreateViewModel.currentBalanceField.value.onValueChange?.invoke("100")

        accountCreateViewModel.saveOrUpdateAccount()

        advanceUntilIdle()

        verify(appComposeNavigator, times(1)).popBackStack()
    }

    @Test
    fun whenSaveAccountWithoutProperValueItShouldReflectError() = runTest {

        accountCreateViewModel.nameField.value.onValueChange?.invoke("")
        accountCreateViewModel.iconValueField.value.onValueChange?.invoke("account_balance")
        accountCreateViewModel.colorValueField.value.onValueChange?.invoke("#FFFFFF")
        accountCreateViewModel.currentBalanceField.value.onValueChange?.invoke("")
        accountCreateViewModel.accountTypeField.value.onValueChange?.invoke(AccountType.CREDIT)
        accountCreateViewModel.creditLimitField.value.onValueChange?.invoke("")

        accountCreateViewModel.saveOrUpdateAccount()

        advanceUntilIdle()

        Truth.assertThat(accountCreateViewModel.nameField.value.valueError).isTrue()
        Truth.assertThat(accountCreateViewModel.currentBalanceField.value.valueError).isTrue()
        Truth.assertThat(accountCreateViewModel.creditLimitField.value.valueError).isTrue()
    }

    @Test
    fun whenChangeAccountTypeCreditWithMinusValueShouldReflectAvailableBalanceErrorBG() = runTest {
        accountCreateViewModel.accountTypeField.value.onValueChange?.invoke(AccountType.CREDIT)
        accountCreateViewModel.currentBalanceField.value.onValueChange?.invoke("-1500")
        accountCreateViewModel.creditLimitField.value.onValueChange?.invoke("1000")

        advanceUntilIdle()

        Truth.assertThat(accountCreateViewModel.availableCreditLimitColor.value.value)
            .isEqualTo(R.color.red_500)
        Truth.assertThat(accountCreateViewModel.availableCreditLimit.value.value)
            .isNotNull()
        Truth.assertThat(accountCreateViewModel.availableCreditLimit.value.value?.amount)
            .isEqualTo(100.0)
    }

    @Test
    fun checkClosePageNavigation() = runTest {
        accountCreateViewModel.closePage()
        verify(appComposeNavigator, times(1)).popBackStack()
    }



    @Test
    fun whenAccountIsAvailableOnDeleteItShouldReflect() = runTest {

        val accountId = "accountId"

        whenever(accountRepository.findAccount(accountId)).thenReturn(
            Resource.Success(FAKE_ACCOUNT)
        )

        whenever(accountRepository.deleteAccount(any())).thenReturn(Resource.Success(true))

        accountCreateViewModel = AccountCreateViewModel(
            SavedStateHandle(mapOf(ExpenseManagerArgsNames.ID to accountId)),
            getCurrencyUseCase,
            getDefaultCurrencyUseCase,
            getFormattedAmountUseCase,
            findAccountByIdUseCase,
            addAccountUseCase,
            updateAccountUseCase,
            deleteAccountUseCase,
            appComposeNavigator,
        )

        advanceUntilIdle()

        accountCreateViewModel.deleteAccount()

        advanceUntilIdle()

        verify(appComposeNavigator, times(1)).popBackStack()
    }
}
