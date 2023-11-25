package com.naveenapps.expensemanager.feature.datefilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.account.GetSelectedAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.account.UpdateSelectedAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.category.GetSelectedCategoriesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.category.UpdateSelectedCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.MoveDateRangeBackwardUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.MoveDateRangeForwardUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.transactiontype.GetSelectedTransactionTypesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.transactiontype.UpdateSelectedTransactionTypesUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.DateRangeType
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
class FilterViewModel @Inject constructor(
    getSelectedTransactionTypesUseCase: GetSelectedTransactionTypesUseCase,
    getSelectedAccountUseCase: GetSelectedAccountUseCase,
    getSelectedCategoriesUseCase: GetSelectedCategoriesUseCase,
    getDateRangeUseCase: GetDateRangeUseCase,
    private val moveDateRangeBackwardUseCase: MoveDateRangeBackwardUseCase,
    private val moveDateRangeForwardUseCase: MoveDateRangeForwardUseCase,
    private val updateSelectedTransactionTypesUseCase: UpdateSelectedTransactionTypesUseCase,
    private val updateSelectedCategoryUseCase: UpdateSelectedCategoryUseCase,
    private val updateSelectedAccountUseCase: UpdateSelectedAccountUseCase
) : ViewModel() {

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    private val _showForward = MutableStateFlow(true)
    val showForward = _showForward.asStateFlow()

    private val _showBackward = MutableStateFlow(true)
    val showBackward = _showBackward.asStateFlow()

    private var dateRangeType = DateRangeType.THIS_MONTH

    private val _selectedTransactionTypes = MutableStateFlow<List<TransactionType>>(emptyList())
    val selectedTransactionTypes = _selectedTransactionTypes.asStateFlow()

    private val _selectedAccounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val selectedAccounts = _selectedAccounts.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories = _selectedCategories.asStateFlow()

    init {
        getSelectedTransactionTypesUseCase.invoke().onEach {
            _selectedTransactionTypes.value = it
        }.launchIn(viewModelScope)

        getSelectedAccountUseCase.invoke().onEach {
            _selectedAccounts.value = it?.map { account ->
                account.toAccountUiModel(Amount(amount = account.amount))
            } ?: emptyList()
        }.launchIn(viewModelScope)

        getSelectedCategoriesUseCase.invoke().onEach {
            _selectedCategories.value = it
        }.launchIn(viewModelScope)

        getDateRangeUseCase.invoke().onEach {
            dateRangeType = it.type

            _date.value = if (it.type == DateRangeType.ALL) {
                it.name
            } else {
                it.description
            }

            when (it.type) {
                DateRangeType.TODAY,
                DateRangeType.THIS_WEEK,
                DateRangeType.THIS_MONTH,
                DateRangeType.THIS_YEAR -> {
                    _showForward.value = true
                    _showBackward.value = true
                }

                DateRangeType.ALL,
                DateRangeType.CUSTOM -> {
                    _showForward.value = false
                    _showBackward.value = false
                }
            }
        }.launchIn(viewModelScope)
    }

    fun moveDateRangeForward() {
        viewModelScope.launch {
            moveDateRangeForwardUseCase.invoke(dateRangeType)
        }
    }

    fun moveDateRangeBackward() {
        viewModelScope.launch {
            moveDateRangeBackwardUseCase.invoke(dateRangeType)
        }
    }

    fun removeTransaction(transactionType: TransactionType) {
        viewModelScope.launch {
            updateSelectedTransactionTypesUseCase.invoke(
                _selectedTransactionTypes.value.addOrRemove(transactionType)
            )
        }
    }

    fun removeAccount(accountUiModel: AccountUiModel) {
        viewModelScope.launch {
            updateSelectedAccountUseCase.invoke(
                _selectedAccounts.value.addOrRemove(accountUiModel).map { it.id }
            )
        }
    }

    fun removeCategory(category: Category) {
        viewModelScope.launch {
            updateSelectedCategoryUseCase.invoke(
                _selectedCategories.value.addOrRemove(category).map { it.id }
            )
        }
    }
}