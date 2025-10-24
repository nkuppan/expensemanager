package com.naveenapps.expensemanager.feature.transaction.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.toDoubleOrNullWithLocale
import com.naveenapps.expensemanager.core.common.utils.toStringWithLocale
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
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
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID


class TransactionCreateViewModel(
    savedStateHandle: SavedStateHandle,
    getCurrencyUseCase: GetCurrencyUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getAllCategoryUseCase: GetAllCategoryUseCase,
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val findTransactionByIdUseCase: FindTransactionByIdUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val settingsRepository: SettingsRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val transactionType = MutableStateFlow(TransactionType.EXPENSE)

    private val isInitialSyncCompleted = MutableStateFlow(
        TransactionCreateInitSetupState(
            isCategorySyncCompleted = false,
            isAccountSyncCompleted = false
        )
    )

    private val _state = MutableStateFlow(
        TransactionCreateState(
            amount = TextFieldValue(
                value = 0.0.toStringWithLocale(),
                valueError = false,
                onValueChange = this::setAmountOnChange
            ),
            notes = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setNotes
            ),
            dateTime = Date(),
            transactionType = TransactionType.EXPENSE,
            currency = getDefaultCurrencyUseCase.invoke(),
            selectedCategory = defaultCategory,
            selectedFromAccount = defaultAccount,
            selectedToAccount = defaultAccount,
            accounts = emptyList(),
            categories = emptyList(),
            showDeleteButton = false,
            showDeleteDialog = false,
            showCategorySelection = false,
            showAccountSelection = false,
            showNumberPad = false,
            showTimeSelection = false,
            showDateSelection = false,
            accountSelection = AccountSelection.FROM_ACCOUNT
        )
    )
    var state = _state.asStateFlow()

    private var transaction: Transaction? = null

    init {

        setDate(Date())

        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke(),
        ) { currency, accounts ->
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

            _state.update {
                it.copy(
                    currency = currency,
                    accounts = mappedAccounts,
                    selectedFromAccount = account ?: mappedAccounts.firstOrNull() ?: defaultAccount,
                    selectedToAccount = account ?: mappedAccounts.firstOrNull() ?: defaultAccount
                )
            }
            isInitialSyncCompleted.update {
                it.copy(isAccountSyncCompleted = true)
            }
        }.launchIn(viewModelScope)

        combine(
            transactionType,
            getAllCategoryUseCase.invoke(),
            settingsRepository.getDefaultIncomeCategory(),
            settingsRepository.getDefaultExpenseCategory()
        ) { transactionType, categories, defaultIncomeCategory, defaultExpenseCategory ->
            val filteredCategories = categories.filter { category ->
                if (transactionType.isIncome()) {
                    category.type.isIncome()
                } else {
                    category.type.isExpense()
                }
            }

            val categoryId = when (transactionType) {
                TransactionType.INCOME -> {
                    defaultIncomeCategory
                }

                TransactionType.EXPENSE -> {
                    defaultExpenseCategory
                }

                else -> {
                    null
                }
            }

            val expenseCategory = filteredCategories.find { it.id == categoryId }

            _state.update {
                it.copy(
                    transactionType = transactionType,
                    selectedCategory = if (transaction != null) {
                        expenseCategory ?: filteredCategories.firstOrNull() ?: defaultCategory
                    } else {
                        filteredCategories.firstOrNull() ?: defaultCategory
                    },
                    categories = filteredCategories
                )
            }

            isInitialSyncCompleted.update {
                it.copy(isCategorySyncCompleted = true)
            }
        }.launchIn(viewModelScope)

        isInitialSyncCompleted.onEach {
            if (it.isAccountSyncCompleted && it.isCategorySyncCompleted) {
                readInfo(savedStateHandle.get<String>(ExpenseManagerArgsNames.ID))
            }
        }.distinctUntilChanged().launchIn(viewModelScope)
    }

    private fun readInfo(transactionId: String?) {
        transactionId ?: return
        viewModelScope.launch {
            when (val response = findTransactionByIdUseCase.invoke(transactionId)) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    val transaction = response.data
                    this@TransactionCreateViewModel.transaction = transaction
                    this@TransactionCreateViewModel.transactionType.value = transaction.type
                    _state.update {
                        it.copy(
                            amount = it.amount.copy(value = transaction.amount.amount.toStringWithLocale()),
                            transactionType = transaction.type,
                            dateTime = transaction.createdOn,
                            notes = it.notes.copy(value = transaction.notes),
                            selectedFromAccount = transaction.fromAccount.toAccountUiModel(
                                getFormattedAmountUseCase.invoke(
                                    transaction.fromAccount.amount,
                                    it.currency,
                                ),
                            ),
                            selectedToAccount = transaction.toAccount?.let { account ->
                                account.toAccountUiModel(
                                    getFormattedAmountUseCase.invoke(
                                        account.amount,
                                        it.currency,
                                    ),
                                )
                            } ?: defaultAccount,
                            selectedCategory = transaction.category,
                            showDeleteButton = true,
                        )
                    }
                }
            }
        }
    }

    private fun save() {
        val amount: String = this.state.value.amount.value

        var isError = false

        if (amount.isBlank()) {
            _state.update { it.copy(amount = it.amount.copy(valueError = true)) }
            isError = true
        }

        val amountValue = amount.toDoubleOrNullWithLocale()

        if (amountValue == null || amountValue <= 0.0) {
            _state.update { it.copy(amount = it.amount.copy(valueError = true)) }
            isError = true
        }

        if (_state.value.transactionType == TransactionType.TRANSFER &&
            _state.value.selectedFromAccount.id == _state.value.selectedToAccount.id
        ) {
            //_message.tryEmit(UiText.StringResource(R.string.select_different_account))
            isError = true
        }

        if (isError) {
            return
        }

        val transaction = Transaction(
            id = this.transaction?.id ?: UUID.randomUUID().toString(),
            notes = _state.value.notes.value,
            categoryId = _state.value.selectedCategory.id,
            fromAccountId = _state.value.selectedFromAccount.id,
            toAccountId =
            if (_state.value.transactionType == TransactionType.TRANSFER) {
                _state.value.selectedToAccount.id
            } else {
                null
            },
            type = _state.value.transactionType,
            amount = Amount(amountValue!!),
            imagePath = "",
            createdOn = _state.value.dateTime,
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

    private fun setAmountOnChange(amount: String) {
        val amountValue = amount.toDoubleOrNullWithLocale()
        _state.update {
            it.copy(
                amount = it.amount.copy(
                    value = amount,
                    valueError = amount.isBlank() || amountValue == null || amountValue <= 0.0
                ),
                showNumberPad = false
            )
        }
    }

    private fun setDate(date: Date) {
        _state.update { it.copy(dateTime = date) }
    }

    private fun setNotes(notes: String) {
        _state.update { it.copy(notes = it.notes.copy(value = notes)) }
    }

    private fun deleteTransaction() {
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

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun openCategoryCreate() {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.CategoryCreate(null),
        )
    }

    private fun openAccountCreate() {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.AccountCreate(null),
        )
    }

    private fun dismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    private fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    fun processAction(action: TransactionCreateAction) {
        when (action) {
            TransactionCreateAction.ClosePage -> closePage()
            TransactionCreateAction.DismissDeleteDialog -> dismissDeleteDialog()
            TransactionCreateAction.ShowDeleteDialog -> showDeleteDialog()
            is TransactionCreateAction.OpenAccountCreate -> {
                openAccountCreate()
            }

            is TransactionCreateAction.OpenCategoryCreate -> {
                openCategoryCreate()
            }

            TransactionCreateAction.Delete -> deleteTransaction()
            is TransactionCreateAction.SetNumberPadValue -> {
                action.amount?.let {
                    setAmountOnChange(it)
                }
            }

            TransactionCreateAction.Save -> save()
            TransactionCreateAction.DismissCategorySelection -> {
                _state.update { it.copy(showCategorySelection = false) }
            }

            TransactionCreateAction.ShowCategorySelection -> {
                _state.update { it.copy(showCategorySelection = true) }
            }

            is TransactionCreateAction.SelectCategory -> {
                _state.update {
                    it.copy(
                        selectedCategory = action.category,
                        showCategorySelection = false
                    )
                }
            }

            TransactionCreateAction.DismissAccountSelection -> {
                _state.update { it.copy(showAccountSelection = false) }
            }

            is TransactionCreateAction.SelectAccount -> {
                _state.update {
                    when (it.accountSelection) {
                        AccountSelection.FROM_ACCOUNT -> {
                            it.copy(
                                selectedFromAccount = action.account,
                                showAccountSelection = false
                            )
                        }

                        AccountSelection.TO_ACCOUNT -> {
                            it.copy(
                                selectedToAccount = action.account,
                                showAccountSelection = false
                            )
                        }
                    }
                }
            }

            is TransactionCreateAction.ShowAccountSelection -> {
                _state.update {
                    it.copy(
                        showAccountSelection = true,
                        accountSelection = action.type
                    )
                }
            }

            is TransactionCreateAction.ChangeTransactionType -> {
                transactionType.update { action.type }
                _state.update { it.copy(transactionType = action.type) }
            }

            TransactionCreateAction.DismissNumberPad -> {
                _state.update { it.copy(showNumberPad = false) }
            }

            TransactionCreateAction.ShowNumberPad -> {
                _state.update { it.copy(showNumberPad = true) }
            }

            TransactionCreateAction.DismissDateSelection -> {
                _state.update {
                    it.copy(
                        showDateSelection = false,
                        showTimeSelection = false
                    )
                }
            }

            is TransactionCreateAction.SelectDate -> {
                _state.update {
                    it.copy(
                        dateTime = action.date,
                        showDateSelection = false,
                        showTimeSelection = false
                    )
                }
            }

            TransactionCreateAction.ShowDateSelection -> {
                _state.update { it.copy(showDateSelection = true) }
            }

            TransactionCreateAction.ShowTimeSelection -> {
                _state.update { it.copy(showTimeSelection = true) }
            }
        }
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
