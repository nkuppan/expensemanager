package com.naveenapps.expensemanager.feature.filter.type

import androidx.compose.ui.util.fastAny
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.account.GetSelectedAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.account.UpdateSelectedAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.category.GetSelectedCategoriesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.category.UpdateSelectedCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.transactiontype.GetSelectedTransactionTypesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.transactiontype.UpdateSelectedTransactionTypesUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterTypeSelectionViewModel @Inject constructor(
    getSelectedTransactionTypesUseCase: GetSelectedTransactionTypesUseCase,
    getSelectedAccountUseCase: GetSelectedAccountUseCase,
    getSelectedCategoriesUseCase: GetSelectedCategoriesUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getAllCategoryUseCase: GetAllCategoryUseCase,
    private val updateSelectedTransactionTypesUseCase: UpdateSelectedTransactionTypesUseCase,
    private val updateSelectedCategoryUseCase: UpdateSelectedCategoryUseCase,
    private val updateSelectedAccountUseCase: UpdateSelectedAccountUseCase,
) : ViewModel() {

    private val _event = Channel<FilterTypeEvent>()
    val event = _event.receiveAsFlow()

    private val _state = MutableStateFlow(
        FilterTypeState(
            selectedAccounts = emptyList(),
            accounts = emptyList(),
            selectedCategories = emptyList(),
            categories = emptyList(),
            selectedTransactionTypes = emptyList(),
            transactionTypes = TransactionType.entries
        )
    )
    val state = _state.asStateFlow()

    init {
        getSelectedTransactionTypesUseCase.invoke().onEach { types ->
            _state.update { it.copy(selectedTransactionTypes = types) }
        }.launchIn(viewModelScope)

        getSelectedAccountUseCase.invoke().onEach { accounts ->
            _state.update {
                it.copy(
                    selectedAccounts = accounts?.map { account ->
                        account.toAccountUiModel(Amount(amount = account.amount))
                    } ?: emptyList()
                )
            }
        }.launchIn(viewModelScope)

        getSelectedCategoriesUseCase.invoke().onEach { categories ->
            _state.update { it.copy(selectedCategories = categories) }
        }.launchIn(viewModelScope)

        getAllCategoryUseCase.invoke().onEach { categories ->
            _state.update { it.copy(categories = categories) }
        }.launchIn(viewModelScope)

        getAllAccountsUseCase.invoke().onEach { accounts ->
            _state.update {
                it.copy(
                    accounts = accounts.map { account ->
                        account.toAccountUiModel(Amount(amount = account.amount))
                    }
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun setTransactionTypes(type: TransactionType) {
        _state.update {
            it.copy(selectedTransactionTypes = it.selectedTransactionTypes.addOrRemove(type))
        }
    }

    private fun setAccount(account: AccountUiModel) {
        _state.update {
            it.copy(selectedAccounts = it.selectedAccounts.addOrRemove(account))
        }
    }

    private fun setCategory(category: Category) {
        _state.update {
            it.copy(selectedCategories = it.selectedCategories.addOrRemove(category))
        }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            val transactionTypes = state.value.selectedTransactionTypes
            val selectedAccount = state.value.selectedAccounts.map { it.id }
            val selectedCategories = state.value.selectedCategories.map { it.id }

            updateSelectedTransactionTypesUseCase(transactionTypes)
            updateSelectedAccountUseCase(selectedAccount)
            updateSelectedCategoryUseCase(selectedCategories)

            _event.send(FilterTypeEvent.Saved)
        }
    }

    fun processAction(action: FilterTypeAction) {
        when (action) {
            is FilterTypeAction.SelectAccount -> setAccount(action.account)
            is FilterTypeAction.SelectCategory -> setCategory(action.category)
            is FilterTypeAction.SelectTransactionType -> setTransactionTypes(action.transactionType)
            FilterTypeAction.SaveChanges -> saveChanges()
        }
    }
}

fun List<TransactionType>.addOrRemove(type: TransactionType): List<TransactionType> {
    val finalList = toMutableList()
    if (finalList.fastAny { it == type }) {
        finalList.remove(type)
    } else {
        finalList.add(type)
    }
    return finalList
}

fun List<AccountUiModel>.addOrRemove(account: AccountUiModel): List<AccountUiModel> {
    val finalList = toMutableList()
    if (finalList.fastAny { it.id == account.id }) {
        finalList.remove(account)
    } else {
        finalList.add(account)
    }
    return finalList
}

fun List<Category>.addOrRemove(category: Category): List<Category> {
    val finalList = toMutableList()
    if (finalList.fastAny { it.id == category.id }) {
        finalList.remove(category)
    } else {
        finalList.add(category)
    }
    return finalList
}
