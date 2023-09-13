package com.nkuppan.expensemanager.presentation.account.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.AccountType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.getCurrencyIcon
import com.nkuppan.expensemanager.domain.usecase.account.AddAccountUseCase
import com.nkuppan.expensemanager.domain.usecase.account.DeleteAccountUseCase
import com.nkuppan.expensemanager.domain.usecase.account.FindAccountByIdUseCase
import com.nkuppan.expensemanager.domain.usecase.account.UpdateAccountUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
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
    private val findAccountByIdUseCase: FindAccountByIdUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<UiText>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _accountUpdated = MutableSharedFlow<Boolean>()
    val accountUpdated = _accountUpdated.asSharedFlow()

    var accountType = MutableStateFlow(AccountType.BANK_ACCOUNT)
        private set

    var name = MutableStateFlow("")
        private set

    var nameErrorMessage = MutableStateFlow<UiText?>(null)
        private set

    var currentBalance = MutableStateFlow("")
        private set

    var currentBalanceErrorMessage = MutableStateFlow<UiText?>(null)
        private set

    var currencyIcon = MutableStateFlow<Int?>(null)
        private set

    var colorValue = MutableStateFlow(DEFAULT_COLOR)
        private set

    var icon = MutableStateFlow(DEFAULT_ICON)
        private set

    private var account: Account? = null

    init {
        readAccountInfo(savedStateHandle.get<String>(CATEGORY_ID))

        getCurrencyUseCase.invoke().onEach {
            currencyIcon.value = it.getCurrencyIcon()
        }.launchIn(viewModelScope)
    }

    private fun updateAccountInfo(account: Account?) {

        this.account = account

        this.account?.let { accountItem ->
            name.value = accountItem.name
            currentBalance.value = accountItem.amount.toString()
            accountType.value = accountItem.type
            colorValue.value = accountItem.iconBackgroundColor
            icon.value = accountItem.iconName
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

        val account = Account(
            id = account?.id ?: UUID.randomUUID().toString(),
            name = name,
            type = accountType.value,
            iconBackgroundColor = color,
            iconName = icon.value,
            amount = currentBalance.toDoubleOrNull() ?: 0.0,
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
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "ic_calendar"
        private const val CATEGORY_ID = "accountId"
    }
}