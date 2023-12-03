package com.naveenapps.expensemanager.feature.account.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.toDoubleOrNullWithLocale
import com.naveenapps.expensemanager.core.common.utils.toStringWithLocale
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
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
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.feature.account.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private val _message = MutableSharedFlow<UiText>()
    val message = _message.asSharedFlow()

    private val _showDelete = MutableStateFlow(false)
    val showDelete = _showDelete.asStateFlow()

    var accountType = MutableStateFlow(AccountType.REGULAR)
        private set

    var name = MutableStateFlow("")
        private set

    var nameErrorMessage = MutableStateFlow<UiText?>(null)
        private set

    var currentBalance = MutableStateFlow("")
        private set

    var currentBalanceErrorMessage = MutableStateFlow<UiText?>(null)
        private set

    var creditLimit = MutableStateFlow("")
        private set

    var creditLimitErrorMessage = MutableStateFlow<UiText?>(null)
        private set

    var currencyIcon = MutableStateFlow<String?>(null)
        private set

    var colorValue = MutableStateFlow(DEFAULT_COLOR)
        private set

    var icon = MutableStateFlow(DEFAULT_ICON)
        private set

    var availableCreditLimit = MutableStateFlow<Amount?>(null)
        private set

    var availableCreditLimitColor =
        MutableStateFlow(com.naveenapps.expensemanager.core.common.R.color.green_500)
        private set

    private var account: Account? = null

    private var currency: Currency = getDefaultCurrencyUseCase.invoke()

    init {
        readAccountInfo(savedStateHandle.get<String>(ExpenseManagerScreens.AccountCreate.KEY_ACCOUNT_ID))

        getCurrencyUseCase.invoke().onEach {
            currency = it
            currencyIcon.value = it.symbol
            updateAvailableCreditLimit(0.0, 0.0)
        }.launchIn(viewModelScope)
    }

    private fun updateAccountInfo(account: Account?) {

        this.account = account

        this.account?.let { accountItem ->
            name.value = accountItem.name
            currentBalance.value = accountItem.amount.toStringWithLocale()
            creditLimit.value = accountItem.creditLimit.toStringWithLocale()
            accountType.value = accountItem.type
            colorValue.value = accountItem.storedIcon.backgroundColor
            icon.value = accountItem.storedIcon.name
            updateAccountValue(accountItem)
            _showDelete.value = true
        }
    }

    private fun updateAccountValue(accountItem: Account?) {
        updateAvailableCreditLimit(
            if (accountType.value == AccountType.CREDIT) {
                accountItem?.creditLimit ?: 0.0
            } else {
                0.0
            },
            accountItem?.amount ?: 0.0
        )
    }

    private fun updateAvailableCreditLimit(
        creditLimit: Double,
        amount: Double
    ) {
        val totalAmount = creditLimit + amount

        availableCreditLimit.value = getFormattedAmountUseCase.invoke(
            amount = totalAmount,
            currency = currency
        )

        availableCreditLimitColor.value = if (totalAmount < 0) {
            com.naveenapps.expensemanager.core.common.R.color.red_500
        } else {
            com.naveenapps.expensemanager.core.common.R.color.green_500
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
                    is Resource.Error -> {
                        _message.emit(
                            UiText.StringResource(R.string.account_delete_error_message)
                        )
                    }

                    is Resource.Success -> {
                        _message.emit(
                            UiText.StringResource(R.string.account_delete_success_message)
                        )
                        composeNavigator.popBackStack()
                    }
                }
            }
        }
    }

    fun saveOrUpdateAccount() {

        val name: String = name.value
        val currentBalance: String = currentBalance.value
        val creditLimit: String = creditLimit.value
        val color: String = colorValue.value

        var isError = false

        if (name.isBlank()) {
            nameErrorMessage.value = UiText.StringResource(R.string.account_name_error)
            isError = true
        }

        if (currentBalance.isBlank()) {
            currentBalanceErrorMessage.value = UiText.StringResource(R.string.current_balance_error)
            isError = true
        }

        if (currentBalance.toDoubleOrNullWithLocale() == null) {
            currentBalanceErrorMessage.value = UiText.StringResource(R.string.current_balance_value_error)
            isError = true
        }

        if (isError) {
            return
        }

        val accountType = accountType.value

        val account = Account(
            id = account?.id ?: UUID.randomUUID().toString(),
            name = name,
            type = accountType,
            storedIcon = StoredIcon(
                name = icon.value,
                backgroundColor = color,
            ),
            amount = currentBalance.toDoubleOrNullWithLocale() ?: 0.0,
            creditLimit = if (accountType == AccountType.CREDIT) {
                creditLimit.toDoubleOrNullWithLocale() ?: 0.0
            } else {
                0.0
            },
            createdOn = Calendar.getInstance().time,
            updatedOn = Calendar.getInstance().time
        )

        viewModelScope.launch {
            val response = if (this@AccountCreateViewModel.account != null) {
                updateAccountUseCase(account)
            } else {
                addAccountUseCase(account)
            }
            when (response) {
                is Resource.Error -> {
                    _message.emit(UiText.StringResource(R.string.account_create_error))
                }

                is Resource.Success -> {
                    _message.emit(UiText.StringResource(R.string.account_create_success))
                    composeNavigator.popBackStack()
                }
            }
        }
    }

    fun closePage() {
        composeNavigator.popBackStack()
    }

    fun setColorValue(colorValue: Int) {
        this.colorValue.value = String.format("#%06X", 0xFFFFFF and colorValue)
    }

    fun setAccountType(accountType: AccountType) {
        this.accountType.value = accountType
        updateAccountValue(account)
    }

    fun setIcon(icon: String) {
        this.icon.value = icon
    }

    fun setNameChange(name: String) {
        this.name.value = name
        if (name.isBlank()) {
            nameErrorMessage.value = UiText.StringResource(R.string.account_name_error)
        } else {
            nameErrorMessage.value = null
        }
    }

    fun setCurrentBalanceChange(currentBalance: String) {
        this.currentBalance.value = currentBalance
        if (currentBalance.isBlank()) {
            currentBalanceErrorMessage.value = UiText.StringResource(R.string.current_balance_error)
        } else {
            currentBalanceErrorMessage.value = null
        }

        if (currentBalance.toDoubleOrNullWithLocale() == null) {
            currentBalanceErrorMessage.value = UiText.StringResource(R.string.current_balance_value_error)
        } else {
            currentBalanceErrorMessage.value = null
        }

        updateAvailableCreditLimit(
            this.creditLimit.value.toDoubleOrNullWithLocale() ?: 0.0,
            this.currentBalance.value.toDoubleOrNullWithLocale() ?: 0.0,
        )
    }

    fun setCreditLimitChange(creditLimit: String) {
        this.creditLimit.value = creditLimit

        updateAvailableCreditLimit(
            this.creditLimit.value.toDoubleOrNullWithLocale() ?: 0.0,
            this.currentBalance.value.toDoubleOrNullWithLocale() ?: 0.0,
        )
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "account_balance"
    }
}