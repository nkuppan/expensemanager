package com.naveenapps.expensemanager.feature.account.create

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
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
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
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

        accountCreateViewModel.icon.test {
            val firstIconName = awaitItem()
            Truth.assertThat(firstIconName).isNotNull()
            Truth.assertThat(firstIconName).isNotEmpty()
            Truth.assertThat(firstIconName).isEqualTo("account_balance")

            accountCreateViewModel.setIcon(iconName)

            val secondIconName = awaitItem()
            Truth.assertThat(secondIconName).isNotNull()
            Truth.assertThat(secondIconName).isNotEmpty()
            Truth.assertThat(secondIconName).isEqualTo(iconName)

        }
    }

    @Test
    fun whenAccountColorSetItShouldReflect() = runTest {

        val selectedColor = "#000064"
        val colorValue = 100

        accountCreateViewModel.colorValue.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isNotEmpty()
            Truth.assertThat(firstItem).isEqualTo("#43A546")

            accountCreateViewModel.setColorValue(colorValue)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isNotEmpty()
            Truth.assertThat(secondItem).isEqualTo(selectedColor)
        }
    }

    @Test
    fun whenChangingAccountTypeItShouldReflect() = runTest {

        accountCreateViewModel.accountType.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isEqualTo(AccountType.REGULAR)

            accountCreateViewModel.setAccountType(AccountType.CREDIT)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isEqualTo(AccountType.CREDIT)
        }
    }

    @Test
    fun whenChangingNameItShouldReflect() = runTest {

        accountCreateViewModel.name.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isEmpty()

            val changedName = "Name"

            accountCreateViewModel.setNameChange(changedName)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isEqualTo(changedName)
        }
    }

    @Test
    fun whenChangingEmptyNameItShouldReflectInErrorMessage() = runTest {

        accountCreateViewModel.nameErrorMessage.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNull()

            val changedName = " "

            accountCreateViewModel.setNameChange(changedName)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isInstanceOf(UiText::class.java)
            Truth.assertThat(secondItem as UiText.StringResource).isNotNull()
        }
    }

    @Test
    fun whenChangingValidNameItShouldReflectInErrorMessage() = runTest {

        accountCreateViewModel.nameErrorMessage.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNull()

            val changedName = " "

            accountCreateViewModel.setNameChange(changedName)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isInstanceOf(UiText::class.java)
            Truth.assertThat(secondItem as UiText.StringResource).isNotNull()

            accountCreateViewModel.setNameChange("Valid")

            val thirdItem = awaitItem()
            Truth.assertThat(thirdItem).isNull()
        }
    }
}