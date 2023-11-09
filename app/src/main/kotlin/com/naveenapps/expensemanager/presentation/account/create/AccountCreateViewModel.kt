package com.naveenapps.expensemanager.presentation.account.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.usecase.account.AddAccountUseCase
import com.naveenapps.expensemanager.domain.usecase.account.DeleteAccountUseCase
import com.naveenapps.expensemanager.domain.usecase.account.FindAccountByIdUseCase
import com.naveenapps.expensemanager.domain.usecase.account.UpdateAccountUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.getCurrencyIcon
import com.naveenapps.expensemanager.ui.utils.UiText
import com.naveenapps.expensemanager.ui.utils.getCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val findAccountByIdUseCase: FindAccountByIdUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<UiText>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _accountUpdated = MutableSharedFlow<Boolean>()
    val accountUpdated = _accountUpdated.asSharedFlow()

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

    var currencyIcon = MutableStateFlow<Int?>(null)
        private set

    var colorValue = MutableStateFlow(DEFAULT_COLOR)
        private set

    var icon = MutableStateFlow(DEFAULT_ICON)
        private set

    var availableCreditLimit = MutableStateFlow<UiText?>(null)
        private set

    var availableCreditLimitColor = MutableStateFlow(R.color.green_500)
        private set

    private var account: Account? = null

    private var currency: Currency = Currency(
        R.string.dollar_type,
        R.string.dollar_name,
        R.drawable.currency_dollar
    )

    init {
        readAccountInfo(savedStateHandle.get<String>(CATEGORY_ID))

        getCurrencyUseCase.invoke().onEach {
            currency = it
            currencyIcon.value = it.getCurrencyIcon()
            updateAvailableCreditLimit(0.0, 0.0)
        }.launchIn(viewModelScope)
    }

    private fun updateAccountInfo(account: Account?) {

        this.account = account

        this.account?.let { accountItem ->
            name.value = accountItem.name
            currentBalance.value = accountItem.amount.toString()
            creditLimit.value = accountItem.creditLimit.toString()
            accountType.value = accountItem.type
            colorValue.value = accountItem.iconBackgroundColor
            icon.value = accountItem.iconName
            updateAccountValue(accountItem)
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

        availableCreditLimit.value = getCurrency(
            currency = currency,
            amount = totalAmount
        )

        availableCreditLimitColor.value = if (totalAmount < 0) {
            R.color.red_500
        } else {
            R.color.green_500
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
                        _errorMessage.emit(
                            UiText.StringResource(R.string.account_delete_error_message)
                        )
                    }

                    is Resource.Success -> {
                        _accountUpdated.emit(true)
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

        if (isError) {
            return
        }

        val accountType = accountType.value

        val account = Account(
            id = account?.id ?: UUID.randomUUID().toString(),
            name = name,
            type = accountType,
            iconBackgroundColor = color,
            iconName = icon.value,
            amount = currentBalance.toDoubleOrNull() ?: 0.0,
            creditLimit = if (accountType == AccountType.CREDIT) {
                creditLimit.toDoubleOrNull() ?: 0.0
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
                    _errorMessage.emit(UiText.StringResource(R.string.account_create_error))
                }

                is Resource.Success -> {
                    _accountUpdated.emit(true)
                }
            }
        }
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

        updateAvailableCreditLimit(
            this.creditLimit.value.toDoubleOrNull() ?: 0.0,
            this.currentBalance.value.toDoubleOrNull() ?: 0.0,
        )
    }

    fun setCreditLimitChange(creditLimit: String) {
        this.creditLimit.value = creditLimit

        updateAvailableCreditLimit(
            this.creditLimit.value.toDoubleOrNull() ?: 0.0,
            this.currentBalance.value.toDoubleOrNull() ?: 0.0,
        )
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "account_balance"
        private const val CATEGORY_ID = "accountId"
    }
}