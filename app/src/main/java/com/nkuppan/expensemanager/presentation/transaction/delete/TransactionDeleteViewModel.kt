package com.nkuppan.expensemanager.presentation.transaction.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.data.usecase.transaction.DeleteTransactionUseCase
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDeleteViewModel @Inject constructor(
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _deleted = Channel<Boolean>()
    val deleted = _deleted.receiveAsFlow()

    fun deleteTransaction(transaction: Transaction) {

        viewModelScope.launch {

            when (val result = deleteTransactionUseCase.invoke(transaction = transaction)) {
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