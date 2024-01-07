package com.naveenapps.expensemanager.feature.budget.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.fromMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toDoubleOrNullWithLocale
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toStringWithLocale
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.domain.usecase.account.FindAccountByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.AddBudgetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.DeleteBudgetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.FindBudgetByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.UpdateBudgetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.feature.budget.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BudgetCreateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val findBudgetByIdUseCase: FindBudgetByIdUseCase,
    private val findAccountByIdUseCase: FindAccountByIdUseCase,
    private val findCategoryByIdUseCase: FindCategoryByIdUseCase,
    private val addBudgetUseCase: AddBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _isDeleteEnabled = MutableStateFlow(false)
    val isDeleteEnabled = _isDeleteEnabled.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    var bottomSheetSelection = MutableStateFlow(BottomSheetSelection.NONE)
        private set

    var nameField = MutableStateFlow(
        TextFieldValue(
            value = "",
            valueError = false,
            onValueChange = this::setNameChange
        )
    )
        private set

    var amountField = MutableStateFlow(
        TextFieldValue(
            value = "",
            valueError = false,
            onValueChange = this::setAmountChange
        )
    )
        private set

    var currencyIcon = MutableStateFlow(
        TextFieldValue(
            value = "",
            valueError = false,
            onValueChange = null
        )
    )
        private set

    var selectedColorField = MutableStateFlow(
        TextFieldValue(
            value = DEFAULT_COLOR,
            valueError = false,
            onValueChange = this::setColorValue
        )
    )
        private set

    var selectedIconField = MutableStateFlow(
        TextFieldValue(
            value = DEFAULT_ICON,
            valueError = false,
            onValueChange = this::setIconValue
        )
    )
        private set

    var selectedDate = MutableStateFlow(
        TextFieldValue(
            value = Date(),
            valueError = false,
            onValueChange = this::setDateChange
        )
    )
        private set

    private val _accountCount = MutableStateFlow<UiText>(UiText.StringResource(R.string.all))
    val accountCount = _accountCount.asStateFlow()

    private val _categoriesCount =
        MutableStateFlow<UiText>(UiText.StringResource(R.string.all))
    val categoriesCount = _categoriesCount.asStateFlow()

    private var selectedAccounts = emptyList<AccountUiModel>()
    private var isAllAccountsSelected = true
    private var selectedCategories = emptyList<Category>()
    private var isAllCategoriesSelected = true

    private var budget: Budget? = null
    private var currency: Currency? = null

    init {
        readBudgetInfo(savedStateHandle.get<String>(ExpenseManagerScreens.BudgetCreate.KEY_BUDGET_ID))

        getCurrencyUseCase.invoke().onEach { updatedCurrency ->
            currency = updatedCurrency
            currencyIcon.update { it.copy(value = updatedCurrency.symbol) }
        }.launchIn(viewModelScope)
    }

    private suspend fun updateBudgetInfo(budget: Budget?) {
        this.budget = budget

        this.budget?.let { budgetItem ->
            nameField.update { it.copy(value = budgetItem.name) }
            amountField.update { it.copy(value = budgetItem.amount.toStringWithLocale()) }
            selectedColorField.update { it.copy(value = budgetItem.storedIcon.backgroundColor) }
            selectedIconField.update { it.copy(value = budgetItem.storedIcon.name) }
            budgetItem.selectedMonth.fromMonthAndYear()?.let { setDateChange(it) }

            val accounts = budgetItem.accounts.map {
                return@map when (val response = findAccountByIdUseCase.invoke(it)) {
                    is Resource.Error -> null
                    is Resource.Success -> {
                        val data = response.data
                        data.toAccountUiModel(
                            Amount(data.amount, currency = currency),
                        )
                    }
                }
            }.filterNotNull()

            if (accounts.isNotEmpty()) {
                setAccounts(accounts, budgetItem.isAllAccountsSelected)
            }

            val categories = budgetItem.categories.map {
                return@map when (val response = findCategoryByIdUseCase.invoke(it)) {
                    is Resource.Error -> null
                    is Resource.Success -> response.data
                }
            }.filterNotNull()

            if (categories.isNotEmpty()) {
                setCategories(categories, budgetItem.isAllAccountsSelected)
            }

            _isDeleteEnabled.value = true
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

    fun deleteBudget() {
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

    fun closeDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun openDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun saveOrUpdateBudget() {
        val name: String = nameField.value.value
        val color: String = selectedColorField.value.value
        val icon: String = selectedIconField.value.value
        val date: Date = selectedDate.value.value
        val amount: Double? = amountField.value.value.toDoubleOrNullWithLocale()

        var isError = false

        if (name.isBlank()) {
            nameField.update { it.copy(valueError = true) }
            isError = true
        }

        if (amount == null || amount == 0.0) {
            amountField.update { it.copy(valueError = true) }
            isError = true
        }

        if (isError) {
            return
        }

        val categories = selectedCategories.map { it.id }

        val accounts = selectedAccounts.map { it.id }

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
            isAllCategoriesSelected = isAllCategoriesSelected,
            isAllAccountsSelected = isAllAccountsSelected,
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
        selectedColorField.update { it.copy(value = colorValue) }
    }

    private fun setIconValue(icon: String) {
        selectedColorField.update { it.copy(value = icon) }
    }

    private fun setNameChange(name: String) {
        nameField.update {
            it.copy(
                value = name,
                valueError = name.isBlank()
            )
        }
    }

    private fun setAmountChange(amount: String) {
        val amountValue = amount.toDoubleOrNullWithLocale()
        amountField.update {
            it.copy(
                value = amount,
                valueError = amountValue == null || amountValue == 0.0
            )
        }
    }

    private fun setDateChange(date: Date) {
        selectedDate.update { it.copy(value = date) }
    }

    fun setAccounts(selectedAccounts: List<AccountUiModel>, isAllSelected: Boolean) {
        this.selectedAccounts = selectedAccounts
        this.isAllAccountsSelected = isAllSelected
        _accountCount.value = if (isAllSelected) {
            UiText.StringResource(R.string.all_time)
        } else {
            UiText.DynamicString(selectedAccounts.size.toString())
        }
    }

    fun setCategories(selectedCategories: List<Category>, isAllSelected: Boolean) {
        this.selectedCategories = selectedCategories
        this.isAllCategoriesSelected = isAllSelected
        _categoriesCount.value = if (isAllSelected) {
            UiText.StringResource(R.string.all_time)
        } else {
            UiText.DynamicString(selectedCategories.size.toString())
        }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun getSelectedAccounts(): List<AccountUiModel> {
        return this.selectedAccounts
    }

    fun getSelectedCategories(): List<Category> {
        return this.selectedCategories
    }

    fun hideBottomSheet() {
        bottomSheetSelection.value = BottomSheetSelection.NONE
    }

    fun openAccountSelection() {
        bottomSheetSelection.value = BottomSheetSelection.ACCOUNT_SELECTION
    }

    fun openCategorySelection() {
        bottomSheetSelection.value = BottomSheetSelection.CATEGORY_SELECTION
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "account_balance"
    }
}
