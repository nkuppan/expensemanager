package com.nkuppan.expensemanager.presentation.transaction.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.data.usecase.account.GetAccountsByNameUseCase
import com.nkuppan.expensemanager.data.usecase.category.GetCategoryByNameUseCase
import com.nkuppan.expensemanager.data.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.AddTransactionUseCase
import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TransactionCreateViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getAccountsByNameUseCase: GetAccountsByNameUseCase,
    private val getCategoryByNameUseCase: GetCategoryByNameUseCase,
    private val getCurrentSymbolUseCase: GetCurrencyUseCase,
) : ViewModel() {

    private val _snackbarMessage = Channel<UiText>()
    val snackbarMessage = _snackbarMessage.receiveAsFlow()

    private val _transactionCreated = Channel<Unit>()
    val transactionCreated = _transactionCreated.receiveAsFlow()

    private val _amountClick = Channel<Unit>()
    val amountClick = _amountClick.receiveAsFlow()

    private val _dateClick = Channel<Unit>()
    val dateClick = _dateClick.receiveAsFlow()

    private val _categoryClick = Channel<Unit>()
    val categoryClick = _categoryClick.receiveAsFlow()

    private val _accountClick = Channel<Unit>()
    val accountClick = _accountClick.receiveAsFlow()

    private val _amount: MutableStateFlow<String> = MutableStateFlow("")
    val amount = _amount.asStateFlow()

    private val _date: MutableStateFlow<String> = MutableStateFlow("")
    val date = _date.asStateFlow()

    private val _currencyType = MutableStateFlow(R.string.default_currency_type)
    val currencyType = _currencyType.asStateFlow()

    val notes: MutableStateFlow<String> = MutableStateFlow("")
    val notesErrorText: MutableStateFlow<String> = MutableStateFlow("")

    private val _categories = Channel<List<String>>()
    val categories = _categories.receiveAsFlow()

    private var categoriesList = mutableListOf<Category>()

    var selectedCategoryId: Int = 0

    private val _accounts = Channel<List<String>>()
    val accounts = _accounts.receiveAsFlow()

    private var accountsList = mutableListOf<Account>()

    var selectedAccountId: Int = 0

    private var transaction: Transaction? = null

    private var dateValue: Date = Date()

    private var amountValue: Double = 0.0

    init {
        setDate(Date())
        setAmount(0.0)

        viewModelScope.launch {
            getCurrentSymbolUseCase.invoke().collectLatest { currencySymbol ->
                _currencyType.value = currencySymbol.type
            }
        }
    }

    fun loadCategories(searchValue: String? = "") {

        viewModelScope.launch {
            val categories = when (val response = getCategoryByNameUseCase.invoke(searchValue)) {
                is Resource.Error -> {
                    emptyList()
                }

                is Resource.Success -> {
                    response.data
                }
            }

            var selectedItemId = 0

            categoriesList = categories.toMutableList()

            val modified = categories.mapIndexed { index, category ->

                if (category.id == transaction?.categoryId) {
                    selectedItemId = index
                }

                category.name
            }

            selectedCategoryId = selectedItemId

            _categories.send(modified)
        }
    }

    fun loadAccounts(searchValue: String? = "") {

        viewModelScope.launch {

            val accounts = when (val response = getAccountsByNameUseCase.invoke(searchValue)) {
                is Resource.Error -> {
                    emptyList()
                }

                is Resource.Success -> {
                    response.data
                }
            }

            var selectedItemId = 0

            accountsList = accounts.toMutableList()

            val modified = accounts.mapIndexed { index, account ->

                if (account.id == transaction?.accountId) {
                    selectedItemId = index
                }

                account.name
            }

            selectedAccountId = selectedItemId

            _accounts.send(modified)
        }
    }

    fun onDateClick() {
        viewModelScope.launch {
            _dateClick.send(Unit)
        }
    }

    fun onAmountClick() {
        viewModelScope.launch {
            _amountClick.send(Unit)
        }
    }

    fun onCategoryAddClick() {
        viewModelScope.launch {
            _categoryClick.send(Unit)
        }
    }

    fun onAccountAddClick() {
        viewModelScope.launch {
            _accountClick.send(Unit)
        }
    }

    fun onSaveClick() {

        val transaction = Transaction(
            id = this.transaction?.id ?: UUID.randomUUID().toString(),
            notes = notes.value,
            categoryId = getCategoryId(),
            accountId = getAccountId(),
            amount = amount.value.toDouble(),
            imagePath = "",
            createdOn = dateValue,
            updatedOn = Calendar.getInstance().time
        ).apply {
            getCategory()?.let {
                category = it
            }
        }

        viewModelScope.launch {
            when (addTransactionUseCase.invoke(transaction)) {
                is Resource.Error -> {
                    _snackbarMessage.send(UiText.StringResource(R.string.unable_to_add_transaction))
                }

                is Resource.Success -> {
                    _transactionCreated.send(Unit)
                }
            }
        }
    }

    fun setTransaction(transaction: Transaction? = null) {

        transaction ?: return

        this.transaction = transaction

        notes.value = transaction.notes

        setDate(transaction.createdOn)

        setAmount(transaction.amount)
    }

    private fun getCategoryId(): String {
        return if (categoriesList.isNotEmpty() && selectedCategoryId < categoriesList.size)
            categoriesList[selectedCategoryId].id
        else
            ""
    }

    private fun getCategory(): Category? {
        return if (categoriesList.isNotEmpty() && selectedCategoryId < categoriesList.size)
            categoriesList[selectedCategoryId]
        else
            null
    }

    private fun getAccountId(): String {
        return if (accountsList.isNotEmpty() && selectedAccountId < accountsList.size)
            accountsList[selectedAccountId].id
        else
            ""
    }

    fun getAmountValue(): Double {
        return amountValue
    }

    fun setAmount(aAmount: Double) {
        amountValue = aAmount
        _amount.value = aAmount.toString()
    }

    fun setDate(aNewDate: Date) {

        _date.value = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            .format(aNewDate)

        dateValue = aNewDate
    }
}