package com.naveenapps.expensemanager.feature.transaction.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.FindTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.UpdateTransactionUseCase
import com.naveenapps.expensemanager.core.model.Account
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
import com.naveenapps.expensemanager.core.model.isTransfer
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
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
    private val numberFormatRepository: NumberFormatRepository,
) : ViewModel() {

    private val transactionType = MutableStateFlow(TransactionType.EXPENSE)

    // Tracks whether both the accounts and categories flows have emitted their first value,
    // so we only populate an editing transaction once both are ready.
    private val syncState = MutableStateFlow(
        TransactionCreateInitSetupState(
            isCategorySyncCompleted = false,
            isAccountSyncCompleted = false,
        )
    )

    private val _state = MutableStateFlow(
        TransactionCreateState(
            amount = TextFieldValue(
                value = numberFormatRepository.formatForEditing(0.0),
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

    // Non-null only when editing an existing transaction.
    private var editingTransaction: Transaction? = null

    init {
        observeAccountsAndCurrency(getCurrencyUseCase, getAllAccountsUseCase)
        observeCategories(getAllCategoryUseCase)
        loadTransactionWhenReady(savedStateHandle)
    }

    // region Observers

    private fun observeAccountsAndCurrency(
        getCurrencyUseCase: GetCurrencyUseCase,
        getAllAccountsUseCase: GetAllAccountsUseCase,
    ) {
        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke(),
        ) { currency, accounts ->
            val mappedAccounts = mapAccountsToUiModels(accounts, currency)
            val defaultAccountId = settingsRepository.getDefaultAccount().firstOrNull()
            val selectedAccount = mappedAccounts.find { it.id == defaultAccountId }
                ?: mappedAccounts.firstOrNull()
                ?: defaultAccount
            _state.update {
                it.copy(
                    currency = currency,
                    accounts = mappedAccounts,
                    selectedFromAccount = selectedAccount,
                    selectedToAccount = selectedAccount,
                )
            }
            syncState.update { it.copy(isAccountSyncCompleted = true) }
        }.launchIn(viewModelScope)
    }

    private fun observeCategories(getAllCategoryUseCase: GetAllCategoryUseCase) {
        combine(
            transactionType,
            getAllCategoryUseCase.invoke(),
            settingsRepository.getDefaultIncomeCategory(),
            settingsRepository.getDefaultExpenseCategory()
        ) { type, categories, defaultIncomeCategory, defaultExpenseCategory ->
            val filteredCategories = categories.filter { category ->
                if (type.isIncome()) category.type.isIncome() else category.type.isExpense()
            }
            val preferredCategoryId = when (type) {
                TransactionType.INCOME -> defaultIncomeCategory
                TransactionType.EXPENSE -> defaultExpenseCategory
                TransactionType.TRANSFER -> null
            }
            val matchedCategory = filteredCategories.find { it.id == preferredCategoryId }

            _state.update {
                it.copy(
                    transactionType = type,
                    categories = filteredCategories,
                    selectedCategory = matchedCategory ?: filteredCategories.firstOrNull()
                    ?: defaultCategory,
                )
            }
            syncState.update { it.copy(isCategorySyncCompleted = true) }
        }.launchIn(viewModelScope)
    }

    private fun loadTransactionWhenReady(savedStateHandle: SavedStateHandle) {
        val transactionId = savedStateHandle.get<String>(ExpenseManagerArgsNames.ID) ?: return
        viewModelScope.launch {
            syncState.first { it.isAccountSyncCompleted && it.isCategorySyncCompleted }
            loadEditingTransaction(transactionId)
        }
    }

    // endregion

    // region Transaction load (edit mode)

    private suspend fun loadEditingTransaction(transactionId: String) {
        when (val response = findTransactionByIdUseCase.invoke(transactionId)) {
            is Resource.Error -> Unit
            is Resource.Success -> {
                val transaction = response.data
                editingTransaction = transaction
                transactionType.value = transaction.type
                _state.update { current ->
                    current.copy(
                        amount = current.amount.copy(
                            value = numberFormatRepository.formatForEditing(transaction.amount.amount)
                        ),
                        transactionType = transaction.type,
                        dateTime = transaction.createdOn,
                        notes = current.notes.copy(value = transaction.notes),
                        selectedCategory = transaction.category,
                        selectedFromAccount = transaction.fromAccount.toAccountUiModel(
                            getFormattedAmountUseCase.invoke(
                                transaction.fromAccount.amount,
                                current.currency
                            ),
                        ),
                        selectedToAccount = transaction.toAccount?.let { toAccount ->
                            toAccount.toAccountUiModel(
                                getFormattedAmountUseCase.invoke(
                                    toAccount.amount,
                                    current.currency
                                ),
                            )
                        } ?: defaultAccount,
                        showDeleteButton = true,
                    )
                }
            }
        }
    }

    // endregion

    // region Save / Delete

    private fun save() {
        val currentState = _state.value
        val amountText = currentState.amount.value
        val amountValue = numberFormatRepository.parseToDouble(amountText)

        if (amountText.isBlank() || amountValue == null || amountValue <= 0.0) {
            _state.update { it.copy(amount = it.amount.copy(valueError = true)) }
            return
        }

        if (currentState.transactionType.isTransfer() &&
            currentState.selectedFromAccount.id == currentState.selectedToAccount.id
        ) return

        // amountValue is smart-cast to Double after the null check above
        persistTransaction(buildTransactionFromState(currentState, amountValue))
    }

    internal fun buildTransactionFromState(
        state: TransactionCreateState,
        amountValue: Double
    ): Transaction {
        return Transaction(
            id = editingTransaction?.id ?: UUID.randomUUID().toString(),
            notes = state.notes.value,
            categoryId = state.selectedCategory.id,
            fromAccountId = state.selectedFromAccount.id,
            toAccountId = if (state.transactionType.isTransfer()) state.selectedToAccount.id else null,
            type = state.transactionType,
            amount = Amount(amountValue),
            imagePath = "",
            createdOn = state.dateTime,
            updatedOn = Calendar.getInstance().time,
        )
    }

    private fun persistTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val response = if (editingTransaction != null) {
                updateTransactionUseCase.invoke(transaction)
            } else {
                addTransactionUseCase.invoke(transaction)
            }
            if (response is Resource.Success) {
                closePage()
            }
        }
    }

    private fun deleteTransaction() {
        val transaction = editingTransaction ?: return
        viewModelScope.launch {
            if (deleteTransactionUseCase.invoke(transaction) is Resource.Success) {
                closePage()
            }
        }
    }

    // endregion

    // region State helpers

    private fun mapAccountsToUiModels(
        accounts: List<Account>,
        currency: Currency
    ): List<AccountUiModel> {
        return accounts.map { account ->
            account.toAccountUiModel(
                getFormattedAmountUseCase.invoke(account.amount, currency),
                if (account.type == AccountType.CREDIT) {
                    getFormattedAmountUseCase.invoke(account.getAvailableCreditLimit(), currency)
                } else {
                    null
                }
            )
        }
    }

    private fun setAmountOnChange(amount: String) {
        val amountValue = numberFormatRepository.parseToDouble(amount)
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

    private fun setNotes(notes: String) {
        _state.update { it.copy(notes = it.notes.copy(value = notes)) }
    }

    private fun changeTransactionType(type: TransactionType) {
        transactionType.update { type }
        _state.update { it.copy(transactionType = type) }
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun openCategoryCreate() {
        appComposeNavigator.navigate(ExpenseManagerScreens.CategoryCreate(null))
    }

    private fun openAccountCreate() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountCreate(null))
    }

    private fun dismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    private fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    // endregion

    fun processAction(action: TransactionCreateAction) {
        when (action) {
            TransactionCreateAction.ClosePage -> closePage()
            TransactionCreateAction.ShowDeleteDialog -> showDeleteDialog()
            TransactionCreateAction.DismissDeleteDialog -> dismissDeleteDialog()
            TransactionCreateAction.Delete -> deleteTransaction()
            TransactionCreateAction.Save -> save()

            is TransactionCreateAction.OpenAccountCreate -> openAccountCreate()
            is TransactionCreateAction.OpenCategoryCreate -> openCategoryCreate()

            is TransactionCreateAction.ChangeTransactionType -> changeTransactionType(action.type)

            is TransactionCreateAction.SetNumberPadValue -> action.amount?.let {
                setAmountOnChange(
                    it
                )
            }

            TransactionCreateAction.ShowCategorySelection -> _state.update {
                it.copy(
                    showCategorySelection = true
                )
            }

            TransactionCreateAction.DismissCategorySelection -> _state.update {
                it.copy(
                    showCategorySelection = false
                )
            }

            is TransactionCreateAction.SelectCategory -> _state.update {
                it.copy(selectedCategory = action.category, showCategorySelection = false)
            }

            is TransactionCreateAction.ShowAccountSelection -> _state.update {
                it.copy(showAccountSelection = true, accountSelection = action.type)
            }

            TransactionCreateAction.DismissAccountSelection -> _state.update {
                it.copy(
                    showAccountSelection = false
                )
            }

            is TransactionCreateAction.SelectAccount -> _state.update {
                when (it.accountSelection) {
                    AccountSelection.FROM_ACCOUNT -> it.copy(
                        selectedFromAccount = action.account,
                        showAccountSelection = false
                    )

                    AccountSelection.TO_ACCOUNT -> it.copy(
                        selectedToAccount = action.account,
                        showAccountSelection = false
                    )
                }
            }

            TransactionCreateAction.ShowNumberPad -> _state.update { it.copy(showNumberPad = true) }
            TransactionCreateAction.DismissNumberPad -> _state.update { it.copy(showNumberPad = false) }

            TransactionCreateAction.ShowDateSelection -> _state.update { it.copy(showDateSelection = true) }
            TransactionCreateAction.ShowTimeSelection -> _state.update { it.copy(showTimeSelection = true) }
            TransactionCreateAction.DismissDateSelection -> _state.update {
                it.copy(showDateSelection = false, showTimeSelection = false)
            }

            is TransactionCreateAction.SelectDate -> _state.update {
                it.copy(
                    dateTime = action.date,
                    showDateSelection = false,
                    showTimeSelection = false
                )
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
