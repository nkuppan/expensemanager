package com.naveenapps.expensemanager.feature.datefilter

import androidx.compose.ui.util.fastAny
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.account.UpdateSelectedAccountUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterTypeSelectionViewModel @Inject constructor(
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getAllCategoryUseCase: GetAllCategoryUseCase,
    private val updateSelectedAccountUseCase: UpdateSelectedAccountUseCase
) : ViewModel() {

    private val _transactionTypes = MutableStateFlow(TransactionType.values().toList())
    val transactionTypes = _transactionTypes.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedTransactionTypes = MutableStateFlow<List<TransactionType>>(emptyList())
    val selectedTransactionTypes = _selectedTransactionTypes.asStateFlow()

    private val _selectedAccounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val selectedAccounts = _selectedAccounts.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories = _selectedCategories.asStateFlow()

    init {
        getAllCategoryUseCase.invoke().onEach {
            _categories.value = it
        }.launchIn(viewModelScope)

        getAllAccountsUseCase.invoke().onEach { accountList ->
            _accounts.value = accountList.map { account ->
                account.toAccountUiModel(Amount(amount = account.amount))
            }
        }.launchIn(viewModelScope)
    }

    fun setTransactionTypes(type: TransactionType) {
        _selectedTransactionTypes.value = _selectedTransactionTypes.value.addOrRemove(type)
    }

    fun setAccount(account: AccountUiModel) {
        _selectedAccounts.value = _selectedAccounts.value.addOrRemove(account)
    }

    fun setCategory(category: Category) {
        _selectedCategories.value = _selectedCategories.value.addOrRemove(category)
    }

    fun saveChanges() {
        viewModelScope.launch {
            val selectedAccounts = _selectedAccounts.value
            if (selectedAccounts.isNotEmpty()) {
                updateSelectedAccountUseCase.invoke(selectedAccounts.map { it.id })
            }
            val selectedCategories = _selectedCategories.value
            if (selectedCategories.isNotEmpty()) {
                updateSelectedAccountUseCase.invoke(selectedCategories.map { it.id })
            }
        }
    }


    private fun List<TransactionType>.addOrRemove(type: TransactionType): List<TransactionType> {
        val finalList = toMutableList()
        if (finalList.fastAny { it == type }) {
            finalList.remove(type)
        } else {
            finalList.add(type)
        }
        return finalList
    }

    private fun List<AccountUiModel>.addOrRemove(account: AccountUiModel): List<AccountUiModel> {
        val finalList = toMutableList()
        if (finalList.fastAny { it.id == account.id }) {
            finalList.remove(account)
        } else {
            finalList.add(account)
        }
        return finalList
    }

    private fun List<Category>.addOrRemove(category: Category): List<Category> {
        val finalList = toMutableList()
        if (finalList.fastAny { it.id == category.id }) {
            finalList.remove(category)
        } else {
            finalList.add(category)
        }
        return finalList
    }
}