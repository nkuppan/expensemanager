package com.naveenapps.expensemanager.feature.transaction.create

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAccountUiModelUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.CreateRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.FindRecurringTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.ProcessRecurringTransactionsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.GetCurrentExchangeRateUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.SuggestedRate
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.FindTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.UpdateTransactionUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.ExchangeRateSnapshot
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.isTransfer
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.BudgetAlertTrigger
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.feature.transaction.R
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    private val imageStorageRepository: ImageStorageRepository,
    private val appComposeNavigator: AppComposeNavigator,
    private val numberFormatRepository: NumberFormatRepository,
    private val feedbackRepository: FeedbackRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val budgetAlertTrigger: BudgetAlertTrigger,
    private val createRecurringTransactionUseCase: CreateRecurringTransactionUseCase,
    private val getAccountUiModelUseCase: GetAccountUiModelUseCase,
    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase,
    private val getCurrentExchangeRateUseCase: GetCurrentExchangeRateUseCase,
    private val findRecurringTransactionByIdUseCase: FindRecurringTransactionByIdUseCase,
) : ViewModel() {

    private val sessionCreatedAttachmentPaths = mutableListOf<String>()

    // Repeat pre-selected by the caller (only for a new transaction). It's the starting point,
    // so leaving it as-is doesn't count as an unsaved change.
    private val initialRepeat: RecurringFrequency? =
        if (savedStateHandle.get<String>(ExpenseManagerArgsNames.ID).isNullOrBlank()) {
            savedStateHandle.get<String>(ExpenseManagerArgsNames.REPEAT)
                ?.let { name -> RecurringFrequency.entries.find { it.name == name } }
        } else {
            null
        }

    private val _event = Channel<TransactionCreateEvent>()
    val event = _event.receiveAsFlow()

    private val transactionType = MutableStateFlow(TransactionType.EXPENSE)

    // Tracks whether both the accounts and categories flows have emitted their first value,
    // so we only populate an editing transaction once both are ready.
    private val syncState = MutableStateFlow(
        TransactionCreateInitSetupState(
            isCategorySyncCompleted = false,
            isAccountSyncCompleted = false,
        ),
    )

    private val _state = MutableStateFlow(
        TransactionCreateState(
            amount = TextFieldValue(
                value = numberFormatRepository.formatForEditing(0.0),
                valueError = false,
                onValueChange = this::setAmountOnChange,
            ),
            notes = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setNotes,
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
            // Opened in init once the "auto-open calculator" preference is known (see
            // openCalculatorIfEnabled), so users who turned it off never see it flash up.
            showNumberPad = false,
            showTimeSelection = false,
            showDateSelection = false,
            accountSelection = AccountSelection.FROM_ACCOUNT,
            repeat = initialRepeat,
        ),
    )
    var state = _state.asStateFlow()

    // Non-null only when editing an existing transaction.
    private var editingTransaction: Transaction? = null

    // Ids seen in the previous accounts/categories emission; null until the first one.
    private var knownAccountIds: Set<String>? = null
    private var knownCategoryIds: Set<String>? = null

    init {
        observeAccountsAndCurrency(getCurrencyUseCase, getAllAccountsUseCase)
        observeCategories(getAllCategoryUseCase)
        loadTransactionWhenReady(savedStateHandle)
        openCalculatorIfEnabled(savedStateHandle)
    }

    // A new transaction opens straight onto the keypad by default: the amount is the one field
    // every entry needs, so this saves a tap on the app's most frequent action. Users can turn
    // it off in Advanced settings. Editing (an id was passed) always opens on the form.
    private fun openCalculatorIfEnabled(savedStateHandle: SavedStateHandle) {
        if (!savedStateHandle.get<String>(ExpenseManagerArgsNames.ID).isNullOrBlank()) return
        viewModelScope.launch {
            if (settingsRepository.isAutoOpenCalculator().first()) {
                _state.update { it.copy(showNumberPad = true) }
            }
        }
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
            val mappedAccounts = accounts.map { getAccountUiModelUseCase(it) }
            val defaultAccountId = settingsRepository.getDefaultAccount().firstOrNull()
            val preferredAccount = mappedAccounts.find { it.id == defaultAccountId }
                ?: mappedAccounts.firstOrNull()
                ?: defaultAccount

            // This flow re-emits whenever any account changes (including one created from the
            // picker on this screen). Only apply defaults on the first emission; afterwards
            // keep the user's choice, refreshed from the new list, and auto-select a newly
            // created account for whichever side the picker was opened for.
            val previousIds = knownAccountIds
            knownAccountIds = mappedAccounts.map { it.id }.toSet()
            val newAccount = previousIds?.takeIf { it.isNotEmpty() }?.let { ids ->
                mappedAccounts.firstOrNull { it.id !in ids }
            }

            _state.update { current ->
                val from: AccountUiModel
                val to: AccountUiModel
                if (previousIds.isNullOrEmpty()) {
                    from = preferredAccount
                    to = firstOtherAccount(mappedAccounts, from.id) ?: from
                } else {
                    val refreshedFrom = mappedAccounts.find { it.id == current.selectedFromAccount.id }
                        ?: preferredAccount
                    val refreshedTo = mappedAccounts.find { it.id == current.selectedToAccount.id }
                        ?: firstOtherAccount(mappedAccounts, refreshedFrom.id)
                        ?: refreshedFrom
                    from = if (newAccount != null && current.accountSelection == AccountSelection.FROM_ACCOUNT) {
                        newAccount
                    } else {
                        refreshedFrom
                    }
                    to = if (newAccount != null && current.accountSelection == AccountSelection.TO_ACCOUNT) {
                        newAccount
                    } else {
                        refreshedTo
                    }
                }
                current.copy(
                    currency = currency,
                    accounts = mappedAccounts,
                    selectedFromAccount = from,
                    selectedToAccount = to,
                )
            }
            syncState.update { it.copy(isAccountSyncCompleted = true) }
            refreshExchangeRate()
        }.launchIn(viewModelScope)
    }

    private fun observeCategories(getAllCategoryUseCase: GetAllCategoryUseCase) {
        combine(
            transactionType,
            getAllCategoryUseCase.invoke(),
            settingsRepository.getDefaultIncomeCategory(),
            settingsRepository.getDefaultExpenseCategory(),
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

            // Re-emits on every category change and on type switches. A category the user
            // just created is selected; otherwise the current choice is kept while it is still
            // valid for this type, and the default is used only when it isn't.
            val previousIds = knownCategoryIds
            knownCategoryIds = categories.map { it.id }.toSet()
            val isFirstLoad = previousIds.isNullOrEmpty()
            val newCategory = if (isFirstLoad) {
                null
            } else {
                filteredCategories.firstOrNull { it.id !in previousIds.orEmpty() }
            }

            _state.update { current ->
                val keptCategory = if (isFirstLoad) {
                    null
                } else {
                    filteredCategories.find { it.id == current.selectedCategory.id }
                }
                current.copy(
                    transactionType = type,
                    categories = filteredCategories,
                    selectedCategory = newCategory
                        ?: keptCategory
                        ?: matchedCategory
                        ?: filteredCategories.firstOrNull()
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

    fun createImageCaptureUri(): Uri = imageStorageRepository.createImageCaptureUri()

    // region Transaction load (edit mode)

    private suspend fun loadEditingTransaction(transactionId: String) {
        when (val response = findTransactionByIdUseCase.invoke(transactionId)) {
            is Resource.Error -> Unit

            is Resource.Success -> {
                val transaction = response.data
                editingTransaction = transaction
                // Only link to the series if its rule still exists (stopped or re-scheduled
                // rules are gone; this transaction then stands on its own).
                val recurringRuleId = ProcessRecurringTransactionsUseCase.ruleIdFromOccurrenceId(transaction.id)
                    ?.takeIf { findRecurringTransactionByIdUseCase.invoke(it) != null }
                transactionType.value = transaction.type
                _state.update { current ->
                    current.copy(
                        amount = current.amount.copy(
                            value = numberFormatRepository.formatForEditing(transaction.amount.amount),
                        ),
                        transactionType = transaction.type,
                        dateTime = transaction.createdOn,
                        notes = current.notes.copy(value = transaction.notes),
                        selectedCategory = transaction.category,
                        selectedFromAccount = getAccountUiModelUseCase(transaction.fromAccount),
                        selectedToAccount = transaction.toAccount
                            ?.let { getAccountUiModelUseCase(it) }
                            ?: defaultAccount,
                        showDeleteButton = true,
                        canRepeat = false,
                        recurringRuleId = recurringRuleId,
                        attachments = transaction.attachments,
                    )
                }
                refreshExchangeRate()
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
        ) {
            // Previously returned silently, so tapping Save appeared to do nothing.
            viewModelScope.launch {
                _event.send(TransactionCreateEvent.ShowMessage(R.string.select_different_account))
            }
            return
        }

        val rateInput = currentState.exchangeRate
        if (rateInput != null && rateInput.isLoading) return
        if (rateInput != null && parseRate(rateInput.rateText) == null) {
            _state.update { it.copy(exchangeRate = rateInput.copy(rateError = true)) }
            return
        }

        // amountValue is smart-cast to Double after the null check above
        persistTransaction(buildTransactionFromState(currentState, amountValue))
    }

    internal fun buildTransactionFromState(
        state: TransactionCreateState,
        amountValue: Double,
    ): Transaction = Transaction(
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
        attachments = state.attachments,
        fromCurrencyCode = state.selectedFromAccount.currencyCode,
        toCurrencyCode = if (state.transactionType.isTransfer()) {
            state.selectedToAccount.currencyCode
        } else {
            state.selectedFromAccount.currencyCode
        },
        rateSnapshot = state.exchangeRate?.toSnapshot(),
    )

    /**
     * The snapshot to store: the transaction's existing one while untouched, a clone of the
     * suggested rate (fetched, or their fixed rate) while the user kept it, otherwise the rate
     * they typed. Same shape each time.
     */
    private fun ExchangeRateInputState.toSnapshot(): ExchangeRateSnapshot? {
        val rate = parseRate(rateText) ?: return null
        existingSnapshot?.let { if (it.rate == rate) return it }
        val keptSuggested = source != ExchangeRateSource.USER && source == suggestedSource &&
            fetchedRate == rate
        return ExchangeRateSnapshot(
            id = UUID.randomUUID().toString(),
            source = if (keptSuggested) source else ExchangeRateSource.USER,
            provider = if (keptSuggested) provider else null,
            baseCode = baseCode,
            quoteCode = quoteCode,
            rate = rate,
            capturedAt = if (keptSuggested) {
                fetchedAt ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            },
        )
    }

    private fun parseRate(text: String): Double? = numberFormatRepository.parseToDouble(text)?.takeIf { it.isFinite() && it > 0.0 }

    private fun persistTransaction(transaction: Transaction) {
        val isNewTransaction = editingTransaction == null
        val previouslyPersistedAttachments = editingTransaction?.attachments ?: emptyList()
        viewModelScope.launch {
            val response = if (editingTransaction != null) {
                updateTransactionUseCase.invoke(transaction)
            } else {
                addTransactionUseCase.invoke(transaction)
            }
            if (response is Resource.Error) {
                _state.update {
                    it.copy(saveError = response.exception.message ?: "Couldn't save the transaction")
                }
            }
            if (response is Resource.Success) {
                (previouslyPersistedAttachments - transaction.attachments.toSet()).forEach {
                    imageStorageRepository.deleteTransactionAttachment(it)
                }
                sessionCreatedAttachmentPaths.removeAll(transaction.attachments)
                // New or edited spending may have pushed a budget past 80% / 100%.
                budgetAlertTrigger.checkBudgetsSoon()
                discardSessionAttachments()
                if (isNewTransaction && transaction.rateSnapshot == null) {
                    _state.value.repeat?.let { frequency ->
                        createRecurringTransactionUseCase.invoke(transaction, frequency)
                        analyticsRepository.logEvent(
                            AnalyticsEvents.RECURRING_CREATED,
                            mapOf(
                                AnalyticsParams.FREQUENCY to frequency.name.lowercase(),
                                AnalyticsParams.TYPE to transaction.type.name.lowercase(),
                            ),
                        )
                    }
                    onNewTransactionCreated(transaction)
                } else {
                    analyticsRepository.logEvent(
                        AnalyticsEvents.TRANSACTION_UPDATED,
                        mapOf(AnalyticsParams.TYPE to transaction.type.name.lowercase()),
                    )
                }
                closePage()
            } else {
                _event.send(TransactionCreateEvent.ShowMessage(DesignSystemR.string.error_save_failed))
            }
        }
    }

    // Only ever called after a brand-new transaction (not an edit) is saved successfully.
    // Bumps the running "transactions created" count, and — once someone has logged enough
    // transactions and enough days have passed since install — requests Google Play's in-app
    // review popup exactly once for the life of the install.
    private suspend fun onNewTransactionCreated(transaction: Transaction) {
        feedbackRepository.setTransactionCreated(true)
        logTransactionCreated(transaction)
        if (feedbackRepository.shouldShowFeedbackDialog().first()) {
            // Marked immediately, before we know whether Play actually showed anything —
            // Play never reports that back, so "requested" is the only signal we get.
            feedbackRepository.setFeedbackDialogShown(true)
            analyticsRepository.logEvent(AnalyticsEvents.REVIEW_REQUESTED, emptyMap())
            _event.send(TransactionCreateEvent.RequestReview)
        }
    }

    // Activation metric: `is_first` marks the install's first-ever transaction, so the
    // install -> first transaction funnel can be built in Firebase without BigQuery.
    private suspend fun logTransactionCreated(transaction: Transaction) {
        val count = feedbackRepository.getTransactionCreatedCount().first()
        if (count == 1) {
            // Very first transaction of this install: Home celebrates it once.
            feedbackRepository.setFirstSaveCelebrationPending(true)
        }
        analyticsRepository.logEvent(
            AnalyticsEvents.TRANSACTION_CREATED,
            mapOf(
                AnalyticsParams.TYPE to transaction.type.name.lowercase(),
                AnalyticsParams.IS_FIRST to (count <= 1).toString(),
                AnalyticsParams.TRANSACTION_COUNT to count.toString(),
                AnalyticsParams.HAS_NOTES to transaction.notes.isNotBlank().toString(),
                AnalyticsParams.HAS_ATTACHMENT to transaction.attachments.isNotEmpty().toString(),
            ),
        )
    }

    private fun deleteTransaction() {
        val transaction = editingTransaction ?: return
        viewModelScope.launch {
            if (deleteTransactionUseCase.invoke(transaction) is Resource.Success) {
                analyticsRepository.logEvent(
                    AnalyticsEvents.TRANSACTION_DELETED,
                    mapOf(AnalyticsParams.TYPE to transaction.type.name.lowercase()),
                )
                transaction.attachments.forEach {
                    imageStorageRepository.deleteTransactionAttachment(it)
                }
                discardSessionAttachments()
                closePage()
            } else {
                _state.update { it.copy(showDeleteDialog = false) }
                _event.send(TransactionCreateEvent.ShowMessage(DesignSystemR.string.error_delete_failed))
            }
        }
    }

    // endregion

    // region Attachments

    private fun onAttachmentPicked(uri: Uri) {
        viewModelScope.launch {
            val path = imageStorageRepository.saveTransactionAttachment(uri) ?: return@launch
            sessionCreatedAttachmentPaths.add(path)
            _state.update { it.copy(attachments = it.attachments + path) }
        }
    }

    private fun removeAttachment(path: String) {
        _state.update { it.copy(attachments = it.attachments - path) }
    }

    private fun cancelEditing() {
        discardSessionAttachments()
        closePage()
    }

    /** Back / close: ask before throwing away edits, otherwise leave straight away. */
    private fun requestClose() {
        if (hasUnsavedChanges()) {
            _state.update { it.copy(showDiscardDialog = true) }
        } else {
            cancelEditing()
        }
    }

    /**
     * New transaction: dirty once the user has entered anything worth keeping (defaults such as
     * the pre-selected account/category/date don't count). Editing: dirty when any saved field
     * differs from the loaded transaction.
     */
    internal fun hasUnsavedChanges(state: TransactionCreateState = _state.value): Boolean {
        val amount = numberFormatRepository.parseToDouble(state.amount.value) ?: 0.0
        val original = editingTransaction
            ?: return amount > 0.0 ||
                state.notes.value.isNotBlank() ||
                state.attachments.isNotEmpty() ||
                state.repeat != initialRepeat

        // Round-trip the stored amount through the edit formatter so precision beyond the
        // currency's decimals doesn't register as a change.
        val originalAmount = numberFormatRepository.parseToDouble(
            numberFormatRepository.formatForEditing(original.amount.amount),
        ) ?: original.amount.amount

        val toAccountChanged = state.transactionType == TransactionType.TRANSFER &&
            state.selectedToAccount.id != original.toAccount?.id

        return amount != originalAmount ||
            state.notes.value != original.notes ||
            state.transactionType != original.type ||
            state.dateTime.time != original.createdOn.time ||
            state.selectedCategory.id != original.category.id ||
            state.selectedFromAccount.id != original.fromAccount.id ||
            toAccountChanged ||
            state.attachments != original.attachments
    }

    private fun discardSessionAttachments() {
        sessionCreatedAttachmentPaths.forEach { imageStorageRepository.deleteTransactionAttachment(it) }
        sessionCreatedAttachmentPaths.clear()
    }

    // endregion

    // region Exchange rate (cross-currency transfers only)

    private var rateJob: Job? = null

    /**
     * Decides what the rate field shows after accounts, type or date change:
     * - not a cross-currency transfer: no rate at all;
     * - editing, same pair, same day: the transaction's stored snapshot, untouched;
     * - the user already typed a rate for this pair and the date didn't change: keep it;
     * - otherwise whatever the exchange method suggests (see [GetCurrentExchangeRateUseCase]):
     *   today's fetched rate, the pair's fixed rate, or nothing — then the user enters it.
     */
    private fun refreshExchangeRate(dateChanged: Boolean = false) {
        val current = _state.value
        val fromCode = current.selectedFromAccount.currencyCode
        val toCode = current.selectedToAccount.currencyCode

        if (fromCode.isNotBlank()) {
            viewModelScope.launch {
                val currency = formatAmountInCurrencyUseCase.currencyFor(fromCode)
                _state.update { it.copy(currency = currency) }
            }
        }

        if (!current.transactionType.isTransfer() || fromCode.isBlank() || toCode.isBlank() ||
            fromCode == toCode
        ) {
            rateJob?.cancel()
            _state.update { it.copy(exchangeRate = null) }
            return
        }

        val stored = editingTransaction?.rateSnapshot
        if (stored != null && stored.baseCode == fromCode && stored.quoteCode == toCode &&
            isSameDay(editingTransaction!!.createdOn, current.dateTime)
        ) {
            rateJob?.cancel()
            setRateInput(
                ExchangeRateInputState(
                    baseCode = fromCode,
                    quoteCode = toCode,
                    rateText = numberFormatRepository.formatForEditing(stored.rate),
                    source = stored.source,
                    provider = stored.provider,
                    fetchedAt = stored.capturedAt,
                    fetchedRate = stored.rate.takeIf { stored.source != ExchangeRateSource.USER },
                    suggestedSource = stored.source.takeIf { it != ExchangeRateSource.USER },
                    existingSnapshot = stored,
                ),
            )
            return
        }

        val typed = current.exchangeRate
        if (!dateChanged && typed != null && typed.baseCode == fromCode &&
            typed.quoteCode == toCode && typed.existingSnapshot == null
        ) {
            return
        }

        rateJob?.cancel()
        setRateInput(
            ExchangeRateInputState(
                baseCode = fromCode,
                quoteCode = toCode,
                rateText = "",
                source = ExchangeRateSource.USER,
                isLoading = true,
            ),
        )
        rateJob = viewModelScope.launch {
            val suggestion = getCurrentExchangeRateUseCase(
                fromCode = fromCode,
                toCode = toCode,
                isDatedToday = isSameDay(current.dateTime, Date()),
            )
            setRateInput(
                when (suggestion) {
                    is SuggestedRate.Suggested -> ExchangeRateInputState(
                        baseCode = fromCode,
                        quoteCode = toCode,
                        rateText = numberFormatRepository.formatForEditing(suggestion.rate.rate),
                        source = suggestion.rate.source,
                        provider = suggestion.rate.provider,
                        fetchedAt = suggestion.rate.capturedAt,
                        fetchedRate = suggestion.rate.rate,
                        suggestedSource = suggestion.rate.source,
                    )

                    is SuggestedRate.None -> ExchangeRateInputState(
                        baseCode = fromCode,
                        quoteCode = toCode,
                        rateText = "",
                        source = ExchangeRateSource.USER,
                        hint = when (suggestion.reason) {
                            SuggestedRate.Reason.BACKDATED -> BACKDATED_HINT
                            SuggestedRate.Reason.UNAVAILABLE -> UNAVAILABLE_HINT
                            SuggestedRate.Reason.NO_FIXED_RATE -> NO_FIXED_RATE_HINT
                            SuggestedRate.Reason.DISABLED -> DISABLED_HINT
                        },
                    )
                },
            )
        }
    }

    private fun changeExchangeRate(text: String) {
        val input = _state.value.exchangeRate ?: return
        val rate = parseRate(text)
        setRateInput(
            input.copy(
                rateText = text,
                // Any edit makes it the user's rate; typing the suggested value back restores it.
                source = input.suggestedSource?.takeIf {
                    rate != null && rate == input.fetchedRate && input.existingSnapshot == null
                } ?: ExchangeRateSource.USER,
                existingSnapshot = input.existingSnapshot?.takeIf { it.rate == rate },
                rateError = false,
            ),
        )
    }

    private fun useFetchedExchangeRate() {
        val input = _state.value.exchangeRate ?: return
        val fetched = input.fetchedRate ?: return
        changeExchangeRate(numberFormatRepository.formatForEditing(fetched))
    }

    private fun setRateInput(input: ExchangeRateInputState) {
        _state.update { it.copy(exchangeRate = input, saveError = null) }
        updateReceivedAmount()
    }

    private fun updateReceivedAmount() {
        val current = _state.value
        val input = current.exchangeRate ?: return
        val rate = parseRate(input.rateText)
        val amount = numberFormatRepository.parseToDouble(current.amount.value)
        viewModelScope.launch {
            val received = if (rate != null && amount != null && amount > 0.0) {
                formatAmountInCurrencyUseCase(amount * rate, input.quoteCode).amountString
            } else {
                null
            }
            _state.update { state ->
                state.copy(exchangeRate = state.exchangeRate?.copy(receivedAmount = received))
            }
        }
    }

    private fun isSameDay(first: Date, second: Date): Boolean {
        val a = Calendar.getInstance().apply { time = first }
        val b = Calendar.getInstance().apply { time = second }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    // endregion

    // region State helpers

    private fun setAmountOnChange(amount: String) {
        val amountValue = numberFormatRepository.parseToDouble(amount)
        _state.update {
            it.copy(
                amount = it.amount.copy(
                    value = amount,
                    valueError = amount.isBlank() || amountValue == null || amountValue <= 0.0,
                ),
                showNumberPad = false,
            )
        }
        updateReceivedAmount()
    }

    private fun setNotes(notes: String) {
        _state.update { it.copy(notes = it.notes.copy(value = notes)) }
    }

    private fun changeTransactionType(type: TransactionType) {
        transactionType.update { type }
        _state.update { current ->
            // Switching to Transfer with both sides on the same account would only fail on
            // save; pick a different destination up front when one exists.
            val toAccount = if (type.isTransfer() &&
                current.selectedToAccount.id == current.selectedFromAccount.id
            ) {
                firstOtherAccount(current.accounts, current.selectedFromAccount.id)
                    ?: current.selectedToAccount
            } else {
                current.selectedToAccount
            }
            current.copy(transactionType = type, selectedToAccount = toAccount, saveError = null)
        }
        refreshExchangeRate()
    }

    private fun firstOtherAccount(
        accounts: List<AccountUiModel>,
        accountId: String,
    ): AccountUiModel? = accounts.firstOrNull { it.id != accountId }

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
            TransactionCreateAction.ClosePage -> requestClose()

            TransactionCreateAction.ConfirmDiscard -> {
                _state.update { it.copy(showDiscardDialog = false) }
                cancelEditing()
            }

            TransactionCreateAction.DismissDiscardDialog -> _state.update { it.copy(showDiscardDialog = false) }

            TransactionCreateAction.ShowDeleteDialog -> showDeleteDialog()

            TransactionCreateAction.DismissDeleteDialog -> dismissDeleteDialog()

            TransactionCreateAction.Delete -> deleteTransaction()

            TransactionCreateAction.Save -> save()

            is TransactionCreateAction.SelectRepeat -> _state.update { it.copy(repeat = action.frequency) }

            TransactionCreateAction.OpenRecurringSeries -> _state.value.recurringRuleId?.let {
                appComposeNavigator.navigate(ExpenseManagerScreens.RecurringTransactionEdit(it))
            }

            is TransactionCreateAction.OpenAccountCreate -> openAccountCreate()

            is TransactionCreateAction.OpenCategoryCreate -> openCategoryCreate()

            is TransactionCreateAction.ChangeTransactionType -> changeTransactionType(action.type)

            // The keypad reports null when it's dismissed (back / tap outside). Close it then,
            // otherwise the user is stuck on the keypad until they enter an amount.
            is TransactionCreateAction.SetNumberPadValue -> action.amount?.let {
                setAmountOnChange(it)
            } ?: _state.update { it.copy(showNumberPad = false) }

            TransactionCreateAction.ShowCategorySelection -> _state.update {
                it.copy(
                    showCategorySelection = true,
                )
            }

            TransactionCreateAction.DismissCategorySelection -> _state.update {
                it.copy(
                    showCategorySelection = false,
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
                    showAccountSelection = false,
                )
            }

            is TransactionCreateAction.SelectAccount -> {
                _state.update {
                    when (it.accountSelection) {
                        AccountSelection.FROM_ACCOUNT -> it.copy(
                            selectedFromAccount = action.account,
                            showAccountSelection = false,
                            saveError = null,
                        )

                        AccountSelection.TO_ACCOUNT -> it.copy(
                            selectedToAccount = action.account,
                            showAccountSelection = false,
                            saveError = null,
                        )
                    }
                }
                refreshExchangeRate()
            }

            TransactionCreateAction.ShowNumberPad -> _state.update {
                it.copy(
                    showNumberPad = true,
                    numberPadInitialAmount = numberFormatRepository.parseToDouble(it.amount.value),
                )
            }

            TransactionCreateAction.DismissNumberPad -> _state.update { it.copy(showNumberPad = false) }

            TransactionCreateAction.ShowDateSelection -> _state.update { it.copy(showDateSelection = true) }

            TransactionCreateAction.ShowTimeSelection -> _state.update { it.copy(showTimeSelection = true) }

            TransactionCreateAction.DismissDateSelection -> _state.update {
                it.copy(showDateSelection = false, showTimeSelection = false)
            }

            is TransactionCreateAction.SelectDate -> {
                val dayChanged = !isSameDay(_state.value.dateTime, action.date)
                _state.update {
                    it.copy(
                        dateTime = action.date,
                        showDateSelection = false,
                        showTimeSelection = false,
                        saveError = null,
                    )
                }
                // A rate belongs to a day: a new day always needs its rate entered again.
                if (dayChanged) refreshExchangeRate(dateChanged = true)
            }

            TransactionCreateAction.ShowAttachmentPicker -> _state.update {
                it.copy(showAttachmentPicker = true)
            }

            TransactionCreateAction.DismissAttachmentPicker -> _state.update {
                it.copy(showAttachmentPicker = false)
            }

            is TransactionCreateAction.AttachmentPicked -> {
                _state.update { it.copy(showAttachmentPicker = false) }
                onAttachmentPicked(action.uri)
            }

            is TransactionCreateAction.RemoveAttachment -> removeAttachment(action.path)

            is TransactionCreateAction.ChangeExchangeRate -> changeExchangeRate(action.rateText)

            TransactionCreateAction.UseFetchedExchangeRate -> useFetchedExchangeRate()
        }
    }

    override fun onCleared() {
        super.onCleared()
        discardSessionAttachments()
    }

    companion object {
        private const val BACKDATED_HINT =
            "This transfer isn't dated today, so no rate is fetched. Enter the rate for that day."
        private const val UNAVAILABLE_HINT =
            "No rate is available right now (offline, or not offered by your provider). Enter it."
        private const val NO_FIXED_RATE_HINT =
            "You have no fixed rate for this pair. Enter the rate, or add the pair in " +
                "Settings > Exchange method."
        private const val DISABLED_HINT =
            "Currency conversion is off. Enter the rate for this transfer; it is saved with " +
                "this transfer only."

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
