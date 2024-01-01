package com.naveenapps.expensemanager.feature.account.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.R
import com.naveenapps.expensemanager.core.common.utils.toDoubleOrNullWithLocale
import com.naveenapps.expensemanager.core.common.utils.toStringWithLocale
import com.naveenapps.expensemanager.core.domain.usecase.account.AddAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.DeleteAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.FindAccountByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.UpdateAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AccountCreateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCurrencyUseCase: GetCurrencyUseCase,
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val findAccountByIdUseCase: FindAccountByIdUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val composeNavigator: AppComposeNavigator,
) : ViewModel() {

    var showDelete = MutableStateFlow(false)
        private set

    var accountTypeField = MutableStateFlow(
        TextFieldValue(
            value = AccountType.REGULAR,
            valueError = false,
            onValueChange = this::setAccountTypeChange
        )
    )
        private set

    var nameField = MutableStateFlow(
        TextFieldValue(
            value = "",
            valueError = false,
            onValueChange = this::setNameChange
        )
    )
        private set

    var currentBalanceField = MutableStateFlow(
        TextFieldValue(
            value = "",
            valueError = false,
            onValueChange = this::setCurrentBalanceChange
        )
    )
        private set

    var creditLimitField = MutableStateFlow(
        TextFieldValue(
            value = "",
            valueError = false,
            onValueChange = this::setCreditLimitChange
        )
    )
        private set

    var currencyIconField = MutableStateFlow(
        TextFieldValue(
            value = "",
            valueError = false,
            onValueChange = null
        )
    )
        private set

    var colorValueField = MutableStateFlow(
        TextFieldValue(
            value = DEFAULT_COLOR,
            valueError = false,
            onValueChange = this::setColorValue
        )
    )
        private set

    var iconValueField = MutableStateFlow(
        TextFieldValue(
            value = DEFAULT_ICON,
            valueError = false,
            onValueChange = this::setIconValue
        )
    )
        private set

    var availableCreditLimit = MutableStateFlow<TextFieldValue<Amount?>>(
        TextFieldValue(
            value = null,
            valueError = false,
            onValueChange = null
        )
    )
        private set

    var availableCreditLimitColor = MutableStateFlow(
        TextFieldValue(
            value = R.color.green_500,
            valueError = false,
            onValueChange = null
        )
    )
        private set

    private var account: Account? = null

    private var currency: Currency = getDefaultCurrencyUseCase.invoke()

    init {
        getCurrencyUseCase.invoke().onEach { updatedCurrency ->
            currency = updatedCurrency
            currencyIconField.update { it.copy(updatedCurrency.symbol) }
            updateAvailableCreditLimit(0.0, 0.0)
        }.launchIn(viewModelScope)

        readAccountInfo(savedStateHandle.get<String>(ExpenseManagerScreens.AccountCreate.KEY_ACCOUNT_ID))
    }

    private fun updateAccountInfo(account: Account?) {
        this.account = account

        this.account?.let { accountItem ->
            nameField.update { it.copy(value = accountItem.name) }
            currentBalanceField.update { it.copy(value = accountItem.amount.toStringWithLocale()) }
            creditLimitField.update { it.copy(value = accountItem.creditLimit.toStringWithLocale()) }
            accountTypeField.update { it.copy(value = accountItem.type) }
            colorValueField.update { it.copy(value = accountItem.storedIcon.backgroundColor) }
            iconValueField.update { it.copy(value = accountItem.storedIcon.name) }
            updateAccountValue(accountItem)
            showDelete.value = true
        }
    }

    private fun updateAccountValue(accountItem: Account?) {
        updateAvailableCreditLimit(
            if (accountTypeField.value.value == AccountType.CREDIT) {
                accountItem?.creditLimit ?: 0.0
            } else {
                0.0
            },
            accountItem?.amount ?: 0.0,
        )
    }

    private fun updateAvailableCreditLimit(
        creditLimit: Double,
        amount: Double,
    ) {
        val totalAmount = creditLimit + amount

        availableCreditLimit.update {
            it.copy(
                value = getFormattedAmountUseCase.invoke(
                    amount = totalAmount,
                    currency = currency,
                )
            )
        }

        availableCreditLimitColor.update {
            it.copy(
                value = if (totalAmount < 0) {
                    R.color.red_500
                } else {
                    R.color.green_500
                }
            )
        }
    }

    private fun readAccountInfo(accountId: String?) {
        accountId ?: return
        viewModelScope.launch {
            when (val response = findAccountByIdUseCase.invoke(accountId)) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    updateAccountInfo(response.data)
                }
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            account?.let { account ->
                when (deleteAccountUseCase.invoke(account)) {
                    is Resource.Error -> Unit
                    is Resource.Success -> {
                        closePage()
                    }
                }
            }
        }
    }

    fun saveOrUpdateAccount() {
        val name: String = nameField.value.value
        val currentBalance: String = currentBalanceField.value.value
        val creditLimit: String = creditLimitField.value.value
        val color: String = colorValueField.value.value
        val accountType = accountTypeField.value.value

        var isError = false

        if (name.isBlank()) {
            nameField.value = nameField.value.copy(valueError = true)
            isError = true
        }

        if (currentBalance.isBlank() || currentBalance.toDoubleOrNullWithLocale() == null) {
            currentBalanceField.value = currentBalanceField.value.copy(valueError = true)
            isError = true
        }

        if (accountType == AccountType.CREDIT) {
            if (creditLimit.isBlank() || creditLimit.toDoubleOrNullWithLocale() == null) {
                creditLimitField.value = creditLimitField.value.copy(valueError = true)
                isError = true
            }
        }

        if (isError) {
            return
        }

        val account = Account(
            id = account?.id ?: UUID.randomUUID().toString(),
            name = name,
            type = accountType,
            storedIcon = StoredIcon(
                name = iconValueField.value.value,
                backgroundColor = color,
            ),
            amount = currentBalance.toDoubleOrNullWithLocale() ?: 0.0,
            creditLimit = if (accountType == AccountType.CREDIT) {
                creditLimit.toDoubleOrNullWithLocale() ?: 0.0
            } else {
                0.0
            },
            createdOn = Calendar.getInstance().time,
            updatedOn = Calendar.getInstance().time,
        )

        viewModelScope.launch {
            val response = if (this@AccountCreateViewModel.account != null) {
                updateAccountUseCase(account)
            } else {
                addAccountUseCase(account)
            }
            when (response) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    composeNavigator.popBackStack()
                }
            }
        }
    }

    fun closePage() {
        composeNavigator.popBackStack()
    }

    private fun setColorValue(colorValue: String) {
        colorValueField.update { it.copy(value = colorValue) }
    }

    private fun setAccountTypeChange(accountType: AccountType) {
        accountTypeField.update { it.copy(value = accountType) }
        updateAccountValue(account)
    }

    private fun setIconValue(icon: String) {
        iconValueField.update { it.copy(value = icon) }
    }

    private fun setNameChange(name: String) {
        nameField.update { it.copy(value = name, valueError = name.isBlank()) }
    }

    private fun setCurrentBalanceChange(currentBalance: String) {
        currentBalanceField.update {
            it.copy(value = currentBalance, valueError = currentBalance.isBlank())
        }

        updateAvailableCreditLimit(
            this.creditLimitField.value.value.toDoubleOrNullWithLocale() ?: 0.0,
            this.currentBalanceField.value.value.toDoubleOrNullWithLocale() ?: 0.0,
        )
    }

    private fun setCreditLimitChange(creditLimit: String) {
        creditLimitField.update {
            it.copy(value = creditLimit, valueError = false)
        }

        updateAvailableCreditLimit(
            this.creditLimitField.value.value.toDoubleOrNullWithLocale() ?: 0.0,
            this.currentBalanceField.value.value.toDoubleOrNullWithLocale() ?: 0.0,
        )
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "account_balance"
    }
}
