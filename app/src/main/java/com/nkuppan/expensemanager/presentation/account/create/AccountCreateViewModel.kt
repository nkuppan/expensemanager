package com.nkuppan.expensemanager.presentation.account.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.data.usecase.account.AddAccountUseCase
import com.nkuppan.expensemanager.data.usecase.account.DeleteAccountUseCase
import com.nkuppan.expensemanager.data.usecase.account.UpdateAccountUseCase
import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.PaymentMode
import com.nkuppan.expensemanager.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class AccountCreateViewModel @Inject constructor(
    private val addAccountUseCase: AddAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel() {

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _accountCreated = Channel<Unit>()
    val accountCreated = _accountCreated.receiveAsFlow()

    private val _colorPicker = Channel<Unit>()
    val colorPicker = _colorPicker.receiveAsFlow()

    val accountName = MutableStateFlow("")
    val accountNameErrorText = MutableStateFlow("")

    private var paymentMode = PaymentMode.CARD

    val colorValue = MutableStateFlow("#43A546")

    private var accountItem: Account? = null

    fun setAccountValue(account: Account?) {

        this.accountItem = account

        if (accountItem == null) {
            return
        }

        accountItem?.let { accountItem ->
            accountName.value = accountItem.name
            colorValue.value = accountItem.backgroundColor
            paymentMode = accountItem.type
        }
    }

    fun delete() {
        viewModelScope.launch {
            accountItem?.let {
                deleteAccountUseCase(it)
            }
        }
    }

    fun onColorContainerClick() {
        viewModelScope.launch {
            _colorPicker.send(Unit)
        }
    }

    fun onSaveClick() {

        val isEdit = accountItem != null

        val accountName: String = accountName.value
        val colorValue: String = colorValue.value
        val createdOn = accountItem?.createdOn ?: Calendar.getInstance().time
        val updatedOn = Calendar.getInstance().time

        val account = Account(
            id = accountItem?.id ?: UUID.randomUUID().toString(),
            name = accountName,
            type = paymentMode,
            backgroundColor = colorValue,
            createdOn = createdOn,
            updatedOn = updatedOn
        )

        viewModelScope.launch {
            val response = if (isEdit) {
                updateAccountUseCase(account)
            } else {
                addAccountUseCase(account)
            }
            when (response) {
                is Resource.Error -> {
                    _errorMessage.send(UiText.StringResource(R.string.account_create_failed))
                }

                is Resource.Success -> {
                    _accountCreated.send(Unit)
                }
            }
        }
    }

    fun setColorValue(color: Int) {
        colorValue.value = String.format("#%06X", 0xFFFFFF and color)
    }

    fun isEditMode(): Boolean {
        return accountItem != null
    }

    fun setPaymentMode(paymentMode: PaymentMode) {
        this.paymentMode = paymentMode
    }

    fun getPaymentModeById(checkedId: Int) =
        when (checkedId) {
            R.id.card -> PaymentMode.CARD
            R.id.wallet -> PaymentMode.WALLET
            R.id.upi -> PaymentMode.UPI
            R.id.cheque -> PaymentMode.CHEQUE
            R.id.internet_banking -> PaymentMode.INTERNET_BANKING
            R.id.bank_account -> PaymentMode.BANK_ACCOUNT
            else -> PaymentMode.NONE
        }

    fun getViewIdByPaymentMode(paymentMode: PaymentMode) =
        when (paymentMode) {
            PaymentMode.NONE -> R.id.card
            PaymentMode.CARD -> R.id.card
            PaymentMode.WALLET -> R.id.wallet
            PaymentMode.UPI -> R.id.upi
            PaymentMode.CHEQUE -> R.id.cheque
            PaymentMode.INTERNET_BANKING -> R.id.internet_banking
            PaymentMode.BANK_ACCOUNT -> R.id.bank_account
        }
}