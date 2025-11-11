package com.naveenapps.expensemanager.feature.account.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.R
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
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID


class AccountCreateViewModel(
    savedStateHandle: SavedStateHandle,
    getCurrencyUseCase: GetCurrencyUseCase,
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val findAccountByIdUseCase: FindAccountByIdUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val composeNavigator: AppComposeNavigator,
    private val numberFormatRepository: NumberFormatRepository,
) : ViewModel() {

    private var _state = MutableStateFlow(
        AccountCreateState(
            name = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setNameChange
            ),
            type = TextFieldValue(
                value = AccountType.REGULAR,
                valueError = false,
                onValueChange = this::setAccountTypeChange
            ),
            amount = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setAmount
            ),
            color = TextFieldValue(
                value = DEFAULT_COLOR,
                valueError = false,
                onValueChange = this::setColorValue
            ),
            icon = TextFieldValue(
                value = DEFAULT_ICON,
                valueError = false,
                onValueChange = this::setIconValue
            ),
            creditLimit = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setCreditLimitChange
            ),
            totalAmountBackgroundColor = R.color.green_500,
            currency = getDefaultCurrencyUseCase.invoke(),
            totalAmount = "",
            showDeleteButton = false,
            showDeleteDialog = false,
        )
    )
    val state = _state.asStateFlow()

    private var account: Account? = null

    init {
        getCurrencyUseCase.invoke().onEach { currency ->

            val totalAmount =
                getTotalAmount(getCreditAmount().toString(), _state.value.amount.value)

            _state.update {
                it.copy(
                    currency = currency,
                    totalAmount = getAmountValue(totalAmount, currency).amountString
                        ?: "",
                    totalAmountBackgroundColor = getBalanceBackgroundColor(totalAmount)
                )
            }
        }.launchIn(viewModelScope)

        readAccountInfo(savedStateHandle.get<String>(ExpenseManagerArgsNames.ID))
    }

    private fun getAmountValue(
        amount: Double,
        currency: Currency? = null
    ): Amount {

        val selectedCurrency = currency ?: _state.value.currency

        return getFormattedAmountUseCase.invoke(
            amount = amount,
            currency = selectedCurrency,
        )
    }

    private fun updateAccountInfo(account: Account?) {
        this.account = account

        this.account?.let { accountItem ->

            val totalAmount =
                getTotalAmount(getCreditAmount().toString(), _state.value.amount.value)

            _state.update {
                it.copy(
                    name = it.name.copy(value = accountItem.name),
                    type = it.type.copy(value = accountItem.type),
                    color = it.color.copy(value = accountItem.storedIcon.backgroundColor),
                    icon = it.icon.copy(value = accountItem.storedIcon.name),
                    amount = it.amount.copy(
                        value = numberFormatRepository.formatForEditing(accountItem.amount)
                    ),
                    creditLimit = it.creditLimit.copy(
                        value = numberFormatRepository.formatForEditing(accountItem.creditLimit)
                    ),
                    totalAmount = getAmountValue(totalAmount, _state.value.currency).amountString
                        ?: "",
                    totalAmountBackgroundColor = getBalanceBackgroundColor(totalAmount),
                    showDeleteButton = true
                )
            }
        }
    }

    private fun getBalanceBackgroundColor(totalAmount: Double): Int {
        return if (totalAmount < 0) {
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

    private fun deleteAccount() {
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

    private fun saveOrUpdateAccount() {
        val name: String = state.value.name.value
        val currentBalance: String = state.value.amount.value
        val creditLimit: String = state.value.creditLimit.value
        val color: String = state.value.color.value
        val icon: String = state.value.icon.value
        val accountType = state.value.type.value

        var isError = false

        if (name.isBlank()) {
            _state.update { it.copy(name = it.name.copy(valueError = true)) }
            isError = true
        }

        if (currentBalance.isBlank() || numberFormatRepository.parseToDouble(currentBalance) == null) {
            _state.update { it.copy(name = it.amount.copy(valueError = true)) }
            isError = true
        }

        if (accountType == AccountType.CREDIT && (creditLimit.isBlank() || numberFormatRepository.parseToDouble(
                creditLimit
            ) == null)
        ) {
            _state.update { it.copy(name = it.creditLimit.copy(valueError = true)) }
            isError = true
        }

        if (isError) {
            return
        }

        val account = Account(
            id = account?.id ?: UUID.randomUUID().toString(),
            name = name,
            type = accountType,
            storedIcon = StoredIcon(
                name = icon,
                backgroundColor = color,
            ),
            amount = numberFormatRepository.parseToDouble(currentBalance) ?: 0.0,
            creditLimit = if (accountType == AccountType.CREDIT) {
                numberFormatRepository.parseToDouble(creditLimit) ?: 0.0
            } else {
                0.0
            },
            createdOn = Calendar.getInstance().time,
            updatedOn = Calendar.getInstance().time,
            sequence = account?.sequence ?: Int.MAX_VALUE
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

    private fun closePage() {
        composeNavigator.popBackStack()
    }

    private fun setColorValue(color: String) {
        _state.update { it.copy(color = it.color.copy(value = color)) }
    }

    private fun setIconValue(icon: String) {
        _state.update { it.copy(icon = it.icon.copy(value = icon)) }
    }

    private fun setAccountTypeChange(type: AccountType) {
        _state.update { it.copy(type = it.type.copy(value = type)) }
    }

    private fun setNameChange(name: String) {
        _state.update { it.copy(name = it.name.copy(value = name, valueError = name.isBlank())) }
    }

    private fun setAmount(amount: String) {

        val totalAmount = (numberFormatRepository.parseToDouble(amount) ?: 0.0) + getCreditAmount()

        _state.update {
            it.copy(
                amount = it.amount.copy(value = amount, valueError = amount.isBlank()),
                totalAmountBackgroundColor = getBalanceBackgroundColor(totalAmount),
                totalAmount = getAmountValue(totalAmount).amountString ?: ""
            )
        }
    }

    private fun getCreditAmount(): Double {
        return if (_state.value.type.value == AccountType.CREDIT) {
            numberFormatRepository.parseToDouble(_state.value.creditLimit.value) ?: 0.0
        } else {
            0.0
        }
    }

    private fun setCreditLimitChange(creditLimit: String) {

        val totalAmount = getTotalAmount(creditLimit, _state.value.amount.value)

        _state.update {
            it.copy(
                creditLimit = it.creditLimit.copy(
                    value = creditLimit,
                    valueError = creditLimit.isBlank()
                ),
                totalAmountBackgroundColor = getBalanceBackgroundColor(totalAmount),
                totalAmount = getAmountValue(totalAmount).amountString ?: ""
            )
        }
    }

    private fun getTotalAmount(creditLimit: String, accountAmount: String): Double {
        return (numberFormatRepository.parseToDouble(creditLimit) ?: 0.0) +
                (numberFormatRepository.parseToDouble(accountAmount) ?: 0.0)
    }

    private fun dismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    private fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    fun processAction(action: AccountCreateAction) {
        when (action) {
            AccountCreateAction.ClosePage -> closePage()
            AccountCreateAction.Delete -> deleteAccount()
            AccountCreateAction.Save -> saveOrUpdateAccount()
            AccountCreateAction.DismissDeleteDialog -> dismissDeleteDialog()
            AccountCreateAction.ShowDeleteDialog -> showDeleteDialog()
        }
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "account_balance"
    }
}
