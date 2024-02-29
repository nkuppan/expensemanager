package com.naveenapps.expensemanager.feature.transaction.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.toDoubleOrNullWithLocale
import com.naveenapps.expensemanager.core.common.utils.toStringWithLocale
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.FindTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.UpdateTransactionUseCase
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TransactionCreateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCurrencyUseCase: GetCurrencyUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getAllCategoryUseCase: GetAllCategoryUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val findTransactionByIdUseCase: FindTransactionByIdUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val settingsRepository: SettingsRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _isDeleteEnabled = MutableStateFlow(false)
    val isDeleteEnabled = _isDeleteEnabled.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    private val _amount = MutableStateFlow(
        TextFieldValue(
            value = 0.0.toStringWithLocale(),
            valueError = false,
            onValueChange = this::setAmountOnChange
        )
    )
    val amount = _amount.asStateFlow()

    private val _date = MutableStateFlow(Date())
    val date = _date.asStateFlow()

    private val _currencyIcon = MutableStateFlow<String?>(null)
    val currencyIcon = _currencyIcon.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes = _notes.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountUiModel>>(emptyList())
    val accounts = _accounts.asStateFlow()

    private val _selectedCategory = MutableStateFlow(defaultCategory)
    var selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedFromAccount = MutableStateFlow(defaultAccount)
    var selectedFromAccount = _selectedFromAccount.asStateFlow()

    private val _selectedToAccount = MutableStateFlow(defaultAccount)
    var selectedToAccount = _selectedToAccount.asStateFlow()

    private val _selectedTransactionType = MutableStateFlow(TransactionType.EXPENSE)
    var selectedTransactionType = _selectedTransactionType.asStateFlow()

    private var transaction: Transaction? = null

    private var selectedCurrency: Currency? = null

    init {

        setDate(Date())

        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke(),
        ) { currency, accounts ->
            selectedCurrency = currency
            _currencyIcon.value = currency.symbol
            val mappedAccounts = if (accounts.isEmpty()) {
                emptyList()
            } else {
                accounts.map {
                    it.toAccountUiModel(
                        getFormattedAmountUseCase.invoke(
                            it.amount,
                            currency,
                        ),
                        if (it.type == AccountType.CREDIT) {
                            getFormattedAmountUseCase.invoke(
                                it.getAvailableCreditLimit(),
                                currency
                            )
                        } else {
                            null
                        }
                    )
                }
            }

            val accountId = settingsRepository.getDefaultAccount().firstOrNull()
            val account = mappedAccounts.find { it.id == accountId }

            _accounts.value = mappedAccounts
            _selectedFromAccount.value = account ?: mappedAccounts.firstOrNull() ?: defaultAccount
            _selectedToAccount.value = account ?: mappedAccounts.firstOrNull() ?: defaultAccount
        }.launchIn(viewModelScope)

        combine(
            selectedTransactionType,
            getAllCategoryUseCase.invoke(),
        ) { transactionType, categories ->
            val filteredCategories = categories.filter { category ->
                if (transactionType.isIncome()) {
                    category.type.isIncome()
                } else {
                    category.type.isExpense()
                }
            }

            val categoryId = when (transactionType) {
                TransactionType.INCOME -> {
                    settingsRepository.getDefaultIncomeCategory().firstOrNull()
                }

                TransactionType.EXPENSE -> {
                    settingsRepository.getDefaultExpenseCategory().firstOrNull()
                }

                else -> {
                    null
                }
            }

            val expenseCategory = filteredCategories.find { it.id == categoryId }

            _categories.value = filteredCategories
            if (transaction == null) {
                setCategorySelection(
                    expenseCategory ?: filteredCategories.firstOrNull() ?: defaultCategory
                )
            }
        }.launchIn(viewModelScope)

        readInfo(
            savedStateHandle.get<String>(
                ExpenseManagerScreens.TransactionCreate.KEY_TRANSACTION_ID,
            ),
        )
    }

    private fun readInfo(transactionId: String?) {
        transactionId ?: return
        viewModelScope.launch {
            when (val response = findTransactionByIdUseCase.invoke(transactionId)) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    val transaction = response.data
                    this@TransactionCreateViewModel.transaction = transaction
                    val currency = selectedCurrency
                    currency ?: return@launch
                    setAmountOnChange(transaction.amount.amount.toStringWithLocale())
                    setDate(transaction.createdOn)
                    setNotes(transaction.notes)
                    setCategorySelection(transaction.category)
                    setAccountSelection(
                        2,
                        transaction.fromAccount.toAccountUiModel(
                            getFormattedAmountUseCase.invoke(
                                transaction.fromAccount.amount,
                                currency,
                            ),
                        ),
                    )
                    val toAccount = transaction.toAccount
                    if (toAccount != null) {
                        setAccountSelection(
                            3,
                            toAccount.toAccountUiModel(
                                getFormattedAmountUseCase.invoke(
                                    toAccount.amount,
                                    currency,
                                ),
                            ),
                        )
                    }
                    setTransactionType(transaction.type)
                    _isDeleteEnabled.value = true
                }
            }
        }
    }

    fun doSave() {
        val amount: String = this._amount.value.value

        var isError = false

        if (amount.isBlank()) {
            this._amount.update { it.copy(valueError = true) }
            isError = true
        }

        val amountValue = amount.toDoubleOrNullWithLocale()

        if (amountValue == null || amountValue <= 0.0) {
            this._amount.update { it.copy(valueError = true) }
            isError = true
        }

        if (selectedTransactionType.value == TransactionType.TRANSFER &&
            selectedFromAccount.value.id == selectedToAccount.value.id
        ) {
            //_message.tryEmit(UiText.StringResource(R.string.select_different_account))
            isError = true
        }

        if (isError) {
            return
        }

        val transaction = Transaction(
            id = this.transaction?.id ?: UUID.randomUUID().toString(),
            notes = notes.value,
            categoryId = selectedCategory.value.id,
            fromAccountId = selectedFromAccount.value.id,
            toAccountId =
            if (selectedTransactionType.value == TransactionType.TRANSFER) {
                selectedToAccount.value.id
            } else {
                null
            },
            type = selectedTransactionType.value,
            amount = Amount(amountValue!!),
            imagePath = "",
            createdOn = date.value,
            updatedOn = Calendar.getInstance().time,
        )

        viewModelScope.launch {
            val response = if (this@TransactionCreateViewModel.transaction != null) {
                updateTransactionUseCase.invoke(transaction)
            } else {
                addTransactionUseCase.invoke(transaction)
            }
            when (response) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    closePage()
                }
            }
        }
    }

    fun setAmountOnChange(amount: String) {
        val amountValue = amount.toDoubleOrNullWithLocale()
        this._amount.update {
            it.copy(
                value = amount,
                valueError = amount.isBlank() || amountValue == null || amountValue <= 0.0
            )
        }
    }

    fun setDate(date: Date) {
        _date.value = date
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun setTransactionType(transactionType: TransactionType) {
        _selectedTransactionType.value = transactionType
    }

    fun setAccountSelection(sheetSelection: Int, account: AccountUiModel) {
        if (sheetSelection == 2) {
            this._selectedFromAccount.value = account
        } else {
            this._selectedToAccount.value = account
        }
    }

    fun setCategorySelection(category: Category) {
        this._selectedCategory.value = category
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            transaction?.let {
                when (deleteTransactionUseCase.invoke(it)) {
                    is Resource.Error -> Unit
                    is Resource.Success -> {
                        closePage()
                    }
                }
            }
        }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun openCategoryCreate() {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.CategoryCreate.createRoute(""),
        )
    }

    fun openAccountCreate() {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.AccountCreate.createRoute(""),
        )
    }

    fun closeDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun openDeleteDialog() {
        _showDeleteDialog.value = true
    }

    companion object {
        private val defaultCategory = Category(
            id = "1",
            name = "Shopping",
            type = CategoryType.EXPENSE,
            storedIcon = StoredIcon(
                name = "ic_calendar",
                backgroundColor = "#000000",
            ),
            createdOn = Date(),
            updatedOn = Date(),
        )

        private val defaultAccount = AccountUiModel(
            id = "1",
            name = "Shopping",
            storedIcon = StoredIcon(
                name = "ic_calendar",
                backgroundColor = "#000000",
            ),
            amount = Amount(0.0, "$ 0.00"),
            amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
        )
    }
}
