package com.naveenapps.expensemanager.feature.budget.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.fromMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.domain.usecase.account.FindAccountByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.AddBudgetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.DeleteBudgetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.FindBudgetByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.UpdateBudgetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.toAccountUiModel
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
import java.util.Date
import java.util.UUID


class BudgetCreateViewModel(
    savedStateHandle: SavedStateHandle,
    getCurrencyUseCase: GetCurrencyUseCase,
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    private val findBudgetByIdUseCase: FindBudgetByIdUseCase,
    private val findAccountByIdUseCase: FindAccountByIdUseCase,
    private val findCategoryByIdUseCase: FindCategoryByIdUseCase,
    private val addBudgetUseCase: AddBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val appComposeNavigator: AppComposeNavigator,
    private val numberFormatRepository: NumberFormatRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        BudgetCreateState(
            isLoading = true,
            name = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setNameChange
            ),
            amount = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setAmountChange
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
            month = TextFieldValue(
                value = Date(),
                valueError = false,
                onValueChange = this::setDateChange
            ),
            currency = getDefaultCurrencyUseCase.invoke(),
            isAllAccountSelected = true,
            selectedAccounts = emptyList(),
            isAllCategorySelected = true,
            selectedCategories = emptyList(),
            showDeleteButton = false,
            showDeleteDialog = false,
            showAccountSelectionDialog = false,
            showCategorySelectionDialog = false,
            showMonthSelection = false
        )
    )
    val state = _state.asStateFlow()

    private var budget: Budget? = null

    init {
        getCurrencyUseCase.invoke().onEach { updatedCurrency ->
            _state.update {
                it.copy(currency = updatedCurrency)
            }
        }.launchIn(viewModelScope)

        readBudgetInfo(savedStateHandle.get<String>(ExpenseManagerArgsNames.ID))
    }

    private suspend fun updateBudgetInfo(budget: Budget) {
        this.budget = budget

        val accounts = budget.accounts.map {
            return@map when (val response = findAccountByIdUseCase.invoke(it)) {
                is Resource.Error -> null
                is Resource.Success -> {
                    val data = response.data
                    data.toAccountUiModel(
                        Amount(data.amount, currency = _state.value.currency),
                    )
                }
            }
        }.filterNotNull()

        if (accounts.isNotEmpty()) {
            setAccounts(accounts, budget.isAllAccountsSelected)
        }

        val categories = budget.categories.map {
            return@map when (val response = findCategoryByIdUseCase.invoke(it)) {
                is Resource.Error -> null
                is Resource.Success -> response.data
            }
        }.filterNotNull()

        if (categories.isNotEmpty()) {
            setCategories(categories, budget.isAllAccountsSelected)
        }

        _state.update { state ->
            state.copy(
                isLoading = false,
                name = state.name.copy(value = budget.name),
                amount = state.amount.copy(value = numberFormatRepository.formatForEditing(budget.amount)),
                icon = state.icon.copy(value = budget.storedIcon.name),
                color = state.color.copy(value = budget.storedIcon.backgroundColor),
                month = state.month.copy(value = budget.selectedMonth.fromMonthAndYear() ?: Date()),
                isAllAccountSelected = budget.isAllAccountsSelected,
                selectedAccounts = emptyList(),
                isAllCategorySelected = budget.isAllCategoriesSelected,
                selectedCategories = emptyList(),
                showDeleteButton = true,
            )
        }
    }

    private fun readBudgetInfo(budgetId: String?) {
        budgetId ?: return
        viewModelScope.launch {
            when (val response = findBudgetByIdUseCase.invoke(budgetId)) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    updateBudgetInfo(response.data)
                }
            }
        }
    }

    private fun deleteBudget() {
        viewModelScope.launch {
            budget?.let { budget ->
                when (deleteBudgetUseCase.invoke(budget)) {
                    is Resource.Error -> Unit
                    is Resource.Success -> {
                        closePage()
                    }
                }
            }
        }
    }

    private fun saveOrUpdateBudget() {
        val name: String = _state.value.name.value
        val color: String = _state.value.color.value
        val icon: String = _state.value.icon.value
        val date: Date = _state.value.month.value
        val amount: Double? = numberFormatRepository.parseToDouble(_state.value.amount.value)

        var isError = false

        if (name.isBlank()) {
            _state.update { it.copy(name = it.name.copy(valueError = true)) }
            isError = true
        }

        if (amount == null || amount == 0.0) {
            _state.update { it.copy(amount = it.amount.copy(valueError = true)) }
            isError = true
        }

        if (isError) {
            return
        }

        val categories = _state.value.selectedCategories.map { it.id }

        val accounts = _state.value.selectedAccounts.map { it.id }

        val budget = Budget(
            id = budget?.id ?: UUID.randomUUID().toString(),
            name = name,
            storedIcon = StoredIcon(
                name = icon,
                backgroundColor = color,
            ),
            amount = amount ?: 0.0,
            selectedMonth = date.toMonthAndYear(),
            categories = categories,
            accounts = accounts,
            isAllCategoriesSelected = _state.value.isAllCategorySelected,
            isAllAccountsSelected = _state.value.isAllAccountSelected,
            createdOn = Calendar.getInstance().time,
            updatedOn = Calendar.getInstance().time,
        )

        viewModelScope.launch {
            val response = if (this@BudgetCreateViewModel.budget != null) {
                updateBudgetUseCase(budget)
            } else {
                addBudgetUseCase(budget)
            }
            when (response) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    closePage()
                }
            }
        }
    }

    private fun setColorValue(colorValue: String) {
        _state.update { it.copy(color = it.color.copy(value = colorValue)) }
    }

    private fun setIconValue(icon: String) {
        _state.update { it.copy(icon = it.icon.copy(value = icon)) }
    }

    private fun setNameChange(name: String) {
        _state.update {
            it.copy(
                name = it.name.copy(
                    value = name,
                    valueError = name.isBlank()
                )
            )
        }
    }

    private fun setAmountChange(amount: String) {
        val amountValue = numberFormatRepository.parseToDouble(amount)
        _state.update {
            it.copy(
                amount = it.amount.copy(
                    value = amount,
                    valueError = amountValue == null || amountValue == 0.0
                )
            )
        }
    }

    private fun setDateChange(date: Date) {
        _state.update {
            it.copy(
                month = it.month.copy(value = date),
                showMonthSelection = false
            )
        }
    }

    private fun setAccounts(selectedAccounts: List<AccountUiModel>, isAllSelected: Boolean) {
        _state.update {
            it.copy(
                isAllAccountSelected = isAllSelected,
                selectedAccounts = selectedAccounts,
                showAccountSelectionDialog = false
            )
        }
    }

    private fun setCategories(selectedCategories: List<Category>, isAllSelected: Boolean) {
        _state.update {
            it.copy(
                isAllCategorySelected = isAllSelected,
                selectedCategories = selectedCategories,
                showCategorySelectionDialog = false
            )
        }
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun closeAccountSelection() {
        _state.update {
            it.copy(
                showAccountSelectionDialog = false
            )
        }
    }

    private fun openAccountSelection() {
        _state.update {
            it.copy(
                showAccountSelectionDialog = true
            )
        }
    }

    private fun closeCategorySelection() {
        _state.update {
            it.copy(
                showCategorySelectionDialog = false,
            )
        }
    }

    private fun openCategorySelection() {
        _state.update {
            it.copy(
                showCategorySelectionDialog = true,
            )
        }
    }

    private fun closeDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    private fun openDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    private fun closeMonthSelection() {
        _state.update { it.copy(showMonthSelection = false) }
    }

    private fun openMonthSelection() {
        _state.update { it.copy(showMonthSelection = true) }
    }

    fun processAction(action: BudgetCreateAction) {
        when (action) {
            BudgetCreateAction.ClosePage -> closePage()
            BudgetCreateAction.OpenAccountSelectionDialog -> openAccountSelection()
            BudgetCreateAction.CloseAccountSelectionDialog -> closeAccountSelection()
            BudgetCreateAction.OpenCategorySelectionDialog -> openCategorySelection()
            BudgetCreateAction.CloseCategorySelectionDialog -> closeCategorySelection()

            BudgetCreateAction.CloseDeleteDialog -> closeDeleteDialog()
            BudgetCreateAction.ShowDeleteDialog -> openDeleteDialog()
            BudgetCreateAction.Save -> saveOrUpdateBudget()
            BudgetCreateAction.Delete -> deleteBudget()
            is BudgetCreateAction.SelectAccounts -> setAccounts(
                action.accounts,
                action.isAllSelected
            )

            is BudgetCreateAction.SelectCategories -> setCategories(
                action.categories,
                action.isAllSelected
            )

            BudgetCreateAction.CloseMonthSelection -> closeMonthSelection()
            BudgetCreateAction.ShowMonthSelection -> openMonthSelection()
        }
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "account_balance"
    }
}
