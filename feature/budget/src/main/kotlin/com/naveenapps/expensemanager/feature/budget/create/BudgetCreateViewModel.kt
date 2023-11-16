package com.naveenapps.expensemanager.feature.budget.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.fromMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.getCurrencyIcon
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
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
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.feature.account.list.toAccountUiModel
import com.naveenapps.expensemanager.feature.budget.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private val _errorMessage = MutableSharedFlow<UiText>()
    val message = _errorMessage.asSharedFlow()

    private val _showDelete = MutableStateFlow(false)
    val showDelete = _showDelete.asStateFlow()

    var name = MutableStateFlow("")
        private set

    var nameErrorMessage = MutableStateFlow<UiText?>(null)
        private set

    var amount = MutableStateFlow("")
        private set

    var amountErrorMessage = MutableStateFlow<UiText?>(null)
        private set

    var currencyIcon = MutableStateFlow<Int?>(null)
        private set

    var colorValue = MutableStateFlow(DEFAULT_COLOR)
        private set

    var icon = MutableStateFlow(DEFAULT_ICON)
        private set

    private val _date = MutableStateFlow(Date())
    val date = _date.asStateFlow()

    private val _accountCount = MutableStateFlow<UiText>(UiText.StringResource(R.string.all_time))
    val accountCount = _accountCount.asStateFlow()

    private val _categoriesCount =
        MutableStateFlow<UiText>(UiText.StringResource(R.string.all_time))
    val categoriesCount = _categoriesCount.asStateFlow()

    private var selectedAccounts = emptyList<AccountUiModel>()
    private var isAllAccountsSelected = true
    private var selectedCategories = emptyList<Category>()
    private var isAllCategoriesSelected = true

    private var budget: Budget? = null
    private var currency: Currency? = null

    init {
        readBudgetInfo(savedStateHandle.get<String>(ExpenseManagerScreens.BudgetCreate.KEY_BUDGET_ID))

        getCurrencyUseCase.invoke().onEach {
            currency = it
            currencyIcon.value = it.type.getCurrencyIcon()
        }.launchIn(viewModelScope)
    }

    private suspend fun updateBudgetInfo(budget: Budget?) {

        this.budget = budget

        this.budget?.let { budgetItem ->
            name.value = budgetItem.name
            amount.value = budgetItem.amount.toString()
            colorValue.value = budgetItem.iconBackgroundColor
            icon.value = budgetItem.iconName
            budgetItem.selectedMonth.fromMonthAndYear()?.let { setDate(it) }

            val accounts = budgetItem.accounts.map {
                return@map when (val response = findAccountByIdUseCase.invoke(it)) {
                    is Resource.Error -> null
                    is Resource.Success -> {
                        val data = response.data
                        data.toAccountUiModel(
                            Amount(data.amount, currency = currency)
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

            _showDelete.value = true
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
                    is Resource.Error -> {
                        _errorMessage.emit(
                            UiText.StringResource(R.string.budget_delete_error_message)
                        )
                    }

                    is Resource.Success -> {
                        closePage()
                    }
                }
            }
        }
    }


    fun saveOrUpdateBudget() {

        val name: String = name.value
        val color: String = colorValue.value
        val date: Date = _date.value
        val amount: Double? = amount.value.toDoubleOrNull()

        if (name.isBlank()) {
            nameErrorMessage.value = UiText.StringResource(R.string.budget_name_error)
            return
        }

        if (amount == null || amount == 0.0) {
            amountErrorMessage.value = UiText.StringResource(R.string.budget_amount_error)
            return
        }

        val categories = selectedCategories.map {
            it.id
        }

        val accounts = selectedAccounts.map {
            it.id
        }

        val budget = Budget(
            id = budget?.id ?: UUID.randomUUID().toString(),
            name = name,
            iconBackgroundColor = color,
            iconName = icon.value,
            amount = amount,
            selectedMonth = date.toMonthAndYear(),
            categories = categories,
            accounts = accounts,
            isAllCategoriesSelected = isAllCategoriesSelected,
            isAllAccountsSelected = isAllAccountsSelected,
            createdOn = Calendar.getInstance().time,
            updatedOn = Calendar.getInstance().time
        )

        viewModelScope.launch {
            val response = if (this@BudgetCreateViewModel.budget != null) {
                updateBudgetUseCase(budget)
            } else {
                addBudgetUseCase(budget)
            }
            when (response) {
                is Resource.Error -> {
                    _errorMessage.emit(UiText.StringResource(R.string.budget_create_error))
                }

                is Resource.Success -> {
                    closePage()
                }
            }
        }
    }

    fun setColorValue(colorValue: Int) {
        this.colorValue.value = String.format("#%06X", 0xFFFFFF and colorValue)
    }

    fun setIcon(icon: String) {
        this.icon.value = icon
    }

    fun setNameChange(name: String) {
        this.name.value = name
        if (name.isBlank()) {
            nameErrorMessage.value = UiText.StringResource(R.string.budget_name_error)
        } else {
            nameErrorMessage.value = null
        }
    }

    fun setAmountChange(amount: String) {
        this.amount.value = amount
        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue == 0.0) {
            amountErrorMessage.value = UiText.StringResource(R.string.budget_amount_error)
        } else {
            amountErrorMessage.value = null
        }
    }

    fun setDate(date: Date) {
        _date.value = date
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

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "account_balance"
    }
}