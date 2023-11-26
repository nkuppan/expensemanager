package com.naveenapps.expensemanager.feature.transaction.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.FindTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.UpdateTransactionUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.feature.transaction.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _message = MutableSharedFlow<UiText>()
    val message = _message.asSharedFlow()

    private val _showDelete = MutableStateFlow(false)
    val showDelete = _showDelete.asStateFlow()

    private val _amount: MutableStateFlow<String> = MutableStateFlow("0.0")
    val amount = _amount.asStateFlow()

    private val _amountErrorMessage: MutableStateFlow<UiText?> = MutableStateFlow(null)
    val amountErrorMessage = _amountErrorMessage.asStateFlow()

    private val _date: MutableStateFlow<Date> = MutableStateFlow(Date())
    val date = _date.asStateFlow()

    private val _currencyIcon = MutableStateFlow<Int?>(null)
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

        getCurrencyUseCase.invoke().onEach {
            selectedCurrency = it
        }.launchIn(viewModelScope)

        getCurrencyUseCase.invoke().combine(getAllAccountsUseCase.invoke()) { currency, accounts ->
            currency to accounts
        }.map { currencyAndAccountPair ->

            val (currency, accounts) = currencyAndAccountPair

            val mappedAccounts = if (accounts.isEmpty()) {
                emptyList()
            } else {
                accounts.map {
                    it.toAccountUiModel(
                        getFormattedAmountUseCase.invoke(
                            it.amount,
                            currency
                        )
                    )
                }
            }
            _accounts.value = mappedAccounts
            _selectedFromAccount.value = mappedAccounts.firstOrNull() ?: defaultAccount
            _selectedToAccount.value = mappedAccounts.firstOrNull() ?: defaultAccount
        }.launchIn(viewModelScope)

        selectedTransactionType.combine(getAllCategoryUseCase.invoke()) { transactionType, categories ->
            transactionType to categories
        }.onEach { pair ->

            val (transactionType, categories) = pair

            val filteredCategories = categories.filter { category ->
                if (transactionType.isIncome()) {
                    category.type.isIncome()
                } else {
                    category.type.isExpense()
                }
            }
            _categories.value = filteredCategories
            _selectedCategory.value = filteredCategories.firstOrNull() ?: defaultCategory

        }.launchIn(viewModelScope)

        readInfo(
            savedStateHandle.get<String>(
                ExpenseManagerScreens.TransactionCreate.KEY_TRANSACTION_ID
            )
        )
    }

    private fun readInfo(transactionId: String?) {
        transactionId ?: return
        viewModelScope.launch {
            when (val response = findTransactionByIdUseCase.invoke(transactionId)) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    val transaction = response.data
                    val currency = selectedCurrency
                    currency ?: return@launch
                    setAmount(transaction.amount.amount.toString())
                    setDate(transaction.createdOn)
                    setNotes(transaction.notes)
                    setCategorySelection(transaction.category)
                    setAccountSelection(
                        2,
                        transaction.fromAccount.toAccountUiModel(
                            getFormattedAmountUseCase.invoke(
                                transaction.fromAccount.amount,
                                currency
                            )
                        )
                    )
                    val toAccount = transaction.toAccount
                    if (toAccount != null) {
                        setAccountSelection(
                            3,
                            toAccount.toAccountUiModel(
                                getFormattedAmountUseCase.invoke(
                                    toAccount.amount,
                                    currency
                                )
                            )
                        )
                    }
                    setTransactionType(transaction.type)
                    this@TransactionCreateViewModel.transaction = transaction
                    _showDelete.value = true
                }
            }
        }
    }

    fun doSave() {

        val amount = this.amount.value

        if (amount.isBlank()) {
            _amountErrorMessage.value = UiText.StringResource(R.string.amount_error_message)
            return
        }

        if (amount.toDouble() <= 0.0) {
            _amountErrorMessage.value =
                UiText.StringResource(R.string.amount_should_greater_than_zero)
            return
        }

        if (selectedTransactionType.value == TransactionType.TRANSFER &&
            selectedFromAccount.value.id == selectedToAccount.value.id
        ) {
            _message.tryEmit(UiText.StringResource(R.string.select_different_account))
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
            amount = Amount(amount.toDouble()),
            imagePath = "",
            createdOn = date.value,
            updatedOn = Calendar.getInstance().time
        )

        viewModelScope.launch {

            val response = if (this@TransactionCreateViewModel.transaction != null) {
                updateTransactionUseCase.invoke(transaction)
            } else {
                addTransactionUseCase.invoke(transaction)
            }
            when (response) {
                is Resource.Error -> {
                    _message.emit(UiText.StringResource(R.string.unable_to_add_transaction))
                }

                is Resource.Success -> {
                    closePage()
                }
            }
        }
    }

    fun setAmount(amount: String) {

        _amount.value = amount

        if (amount.isBlank()) {
            _amountErrorMessage.value = UiText.StringResource(R.string.amount_error_message)
            return
        }

        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0.0) {
            _amountErrorMessage.value =
                UiText.StringResource(R.string.amount_should_greater_than_zero)
            return
        }

        _amountErrorMessage.value = null
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
                    is Resource.Error -> {
                        _message.emit(
                            UiText.StringResource(R.string.transaction_delete_error_message)
                        )
                    }

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

    companion object {
        private val defaultCategory = Category(
            id = "1",
            name = "Shopping",
            type = CategoryType.EXPENSE,
            storedIcon = StoredIcon(
                name = "ic_calendar",
                backgroundColor = "#000000"
            ),
            createdOn = Date(),
            updatedOn = Date()
        )

        private val defaultAccount = AccountUiModel(
            id = "1",
            name = "Shopping",
            storedIcon = StoredIcon(
                name = "ic_calendar",
                backgroundColor = "#000000"
            ),
            amount = Amount(0.0, "$ 0.00"),
            amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500
        )
    }
}