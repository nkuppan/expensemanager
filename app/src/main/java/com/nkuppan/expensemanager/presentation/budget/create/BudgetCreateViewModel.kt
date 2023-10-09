package com.nkuppan.expensemanager.presentation.budget.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.common.ui.utils.UiText
import com.nkuppan.expensemanager.data.utils.toTransactionMonth
import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.getCurrencyIcon
import com.nkuppan.expensemanager.domain.usecase.budget.AddBudgetUseCase
import com.nkuppan.expensemanager.domain.usecase.budget.DeleteBudgetUseCase
import com.nkuppan.expensemanager.domain.usecase.budget.FindBudgetByIdUseCase
import com.nkuppan.expensemanager.domain.usecase.budget.UpdateBudgetUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
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
    private val addBudgetUseCase: AddBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<UiText>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _budgetUpdated = MutableSharedFlow<Boolean>()
    val budgetUpdated = _budgetUpdated.asSharedFlow()

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

    private val _date: MutableStateFlow<Date> = MutableStateFlow(Date())
    val date = _date.asStateFlow()

    private var budget: Budget? = null

    init {
        readBudgetInfo(savedStateHandle.get<String>(BUDGET_ID))

        getCurrencyUseCase.invoke().onEach {
            currencyIcon.value = it.getCurrencyIcon()
        }.launchIn(viewModelScope)
    }

    private fun updateBudgetInfo(budget: Budget?) {

        this.budget = budget

        this.budget?.let { budgetItem ->
            name.value = budgetItem.name
            amount.value = budgetItem.amount.toString()
            colorValue.value = budgetItem.iconBackgroundColor
            icon.value = budgetItem.iconName
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
                        _budgetUpdated.emit(true)
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

        val categories = emptyList<String>()
        val accounts = emptyList<String>()

        val budget = Budget(
            id = budget?.id ?: UUID.randomUUID().toString(),
            name = name,
            iconBackgroundColor = color,
            iconName = icon.value,
            amount = amount,
            selectedMonth = date.toTransactionMonth(),
            categories = categories,
            accounts = accounts,
            isAllCategoriesSelected = true,
            isAllAccountsSelected = true,
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
                    _budgetUpdated.emit(true)
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

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "account_balance"
        private const val BUDGET_ID = "budgetId"
    }
}