package com.nkuppan.expensemanager.presentation.account.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.data.usecase.account.DeleteAccountUseCase
import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountDeleteViewModel @Inject constructor(
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _deleted = Channel<Boolean>()
    val deleted = _deleted.receiveAsFlow()

    fun deleteAccount(account: Account) {

        viewModelScope.launch {

            when (val result = deleteAccountUseCase.invoke(account = account)) {
                is Resource.Error -> {
                    _deleted.send(false)
                }

                is Resource.Success -> {
                    _deleted.send(result.data)
                }
            }
        }
    }
}