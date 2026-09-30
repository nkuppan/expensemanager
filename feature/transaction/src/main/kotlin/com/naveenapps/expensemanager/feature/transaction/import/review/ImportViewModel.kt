package com.naveenapps.expensemanager.feature.transaction.import.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.datastore.StatementPasswordStore
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.ParsedTransaction
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.feature.transaction.import.parser.BhimStatementParser
import com.naveenapps.expensemanager.feature.transaction.import.parser.StatementParserResolver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.UUID

class ImportViewModel(
    getCurrencyUseCase: GetCurrencyUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getAllCategoryUseCase: GetAllCategoryUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val numberFormatRepository: NumberFormatRepository,
    private val appComposeNavigator: AppComposeNavigator,
    private val statementPasswordStore: StatementPasswordStore,
    private val parserResolver: StatementParserResolver = StatementParserResolver(),
) : ViewModel() {

    private val _state = MutableStateFlow(ImportState(isLoading = true))
    val state = _state.asStateFlow()

    private data class UndoEntry(val index: Int, val draftId: String, val wasSelected: Boolean)

    private val undoStack = ArrayDeque<UndoEntry>()

    private var allCategories: List<Category> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                getCurrencyUseCase.invoke(),
                getAllAccountsUseCase.invoke(),
                getAllCategoryUseCase.invoke(),
            ) { c, accounts, cats ->
                Triple(c, accounts, cats)
            }.firstOrNull()?.let { (c, accounts, cats) ->
                allCategories = cats
                val mapped = mapAccounts(accounts, c)
                _state.update {
                    it.copy(
                        isLoading = false,
                        accounts = mapped,
                        categories = cats,
                    )
                }
            }
        }
    }

    private fun mapAccounts(
        accounts: List<com.naveenapps.expensemanager.core.model.Account>,
        curr: com.naveenapps.expensemanager.core.model.Currency,
    ) =
        accounts.map { account ->
            account.toAccountUiModel(
                getFormattedAmountUseCase.invoke(account.amount, curr),
            )
        }

    fun processAction(action: ImportAction) {
        // Ignore review mutations while a bulk save is running so the
        // saved set cannot change under the loop.
        if (_state.value.isSaving) {
            when (action) {
                ImportAction.ClosePage,
                ImportAction.ClearError,
                is ImportAction.ParseFailed,
                is ImportAction.ParsedTextReceived,
                -> Unit
                else -> return
            }
        }
        when (action) {
            ImportAction.ClosePage -> appComposeNavigator.popBackStack()
            ImportAction.StartParsing -> _state.update {
                // A new document is being read, so any remembered password has
                // to be re-read before the dialog may prefill it.
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    rememberedPassword = null,
                    rememberPassword = false,
                    isRememberedPasswordLoaded = false,
                )
            }
            is ImportAction.ParsedTextReceived -> onParsedText(action.text)
            is ImportAction.ParseFailed -> _state.update {
                it.copy(isLoading = false, errorMessage = action.message)
            }

            ImportAction.LoadRememberedPassword -> viewModelScope.launch {
                val remembered = statementPasswordStore.read()
                _state.update {
                    it.copy(
                        rememberedPassword = remembered,
                        rememberPassword = remembered != null,
                        isRememberedPasswordLoaded = true,
                    )
                }
            }
            is ImportAction.UpdateRememberPassword -> _state.update {
                it.copy(rememberPassword = action.remember)
            }
            is ImportAction.SubmitPassword -> viewModelScope.launch {
                statementPasswordStore.write(
                    if (_state.value.rememberPassword) action.password else null,
                )
            }
            ImportAction.SwitchToCard -> _state.update { it.copy(viewMode = ImportViewMode.CARD) }
            is ImportAction.OpenCard -> {
                val index = _state.value.drafts.indexOfFirst { it.parsed.id == action.draftId }
                if (index >= 0) {
                    _state.update {
                        it.copy(
                            currentIndex = index,
                            viewMode = ImportViewMode.CARD,
                        )
                    }
                }
            }
            ImportAction.SwitchToList -> _state.update { it.copy(viewMode = ImportViewMode.LIST) }
            ImportAction.AcceptCurrent -> acceptCurrent()
            ImportAction.RejectCurrent -> rejectCurrent()
            ImportAction.UndoLast -> undoLast()
            is ImportAction.UpdateAmount -> updateDraft(action.draftId) {
                it.copy(amountText = action.amount)
            }

            is ImportAction.UpdateNotes -> updateDraft(action.draftId) {
                it.copy(notes = action.notes)
            }

            is ImportAction.UpdateDateTime -> updateDraft(action.draftId) {
                it.copy(dateTime = action.dateTime, dateManuallyCorrected = true)
            }

            is ImportAction.UpdateType -> updateDraft(action.draftId) {
                it.copy(
                    transactionType = action.type,
                    selectedCategory = defaultCategoryFor(action.type, it.selectedCategory),
                )
            }

            is ImportAction.SelectAccount -> updateDraft(action.draftId) {
                it.copy(selectedAccount = action.account, showAccountSelection = false)
            }

            is ImportAction.SelectCategory -> updateDraft(action.draftId) {
                it.copy(selectedCategory = action.category, showCategorySelection = false)
            }

            is ImportAction.ToggleSelection -> {
                val draft = _state.value.drafts.find { it.parsed.id == action.draftId }
                if (draft != null && !draft.isSelected && !draft.isImportable) {
                    _state.update {
                        it.copy(errorMessage = "Failed or invalid rows cannot be imported.")
                    }
                } else {
                    updateDraft(action.draftId) { it.copy(isSelected = !it.isSelected) }
                }
            }

            ImportAction.SelectAll -> _state.update { s ->
                s.copy(drafts = s.drafts.map { it.copy(isSelected = it.isImportable) })
            }

            ImportAction.DeselectAll -> _state.update { s ->
                s.copy(drafts = s.drafts.map { it.copy(isSelected = false) })
            }

            ImportAction.ConfirmSelected -> saveSelected()
            ImportAction.ClearLastAction -> {
                undoStack.clear()
                _state.update { it.copy(lastAction = null, canUndo = false) }
            }
            is ImportAction.ShowAccountSelection -> updateDraft(action.draftId) {
                it.copy(showAccountSelection = true)
            }

            is ImportAction.ShowCategorySelection -> updateDraft(action.draftId) {
                it.copy(showCategorySelection = true)
            }

            is ImportAction.ShowDateSelection -> updateDraft(action.draftId) {
                it.copy(showDateSelection = true)
            }

            is ImportAction.ShowTimeSelection -> updateDraft(action.draftId) {
                it.copy(showTimeSelection = true)
            }

            is ImportAction.DismissSelectors -> updateDraft(action.draftId) {
                it.copy(
                    showAccountSelection = false,
                    showCategorySelection = false,
                    showDateSelection = false,
                    showTimeSelection = false,
                )
            }

            ImportAction.ClearError -> _state.update { it.copy(errorMessage = null) }

            ImportAction.ParsingFinished -> _state.update { it.copy(isLoading = false) }
        }
    }

    private fun onParsedText(text: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val parser = parserResolver.select(text)
            if (parser == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not recognise this statement. Supported formats: " +
                            parserResolver.supportedFormats.joinToString(", ") + ".",
                    )
                }
                return@launch
            }
            val parsed = withContext(Dispatchers.Default) { parser.parse(text) }
            if (parsed.isEmpty()) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No transactions found in this PDF. Please check the file.",
                    )
                }
                return@launch
            }
            // A duplicate-check read failure must surface as an error, never as
            // "no existing transactions". Duplicates themselves stay advisory
            // (warning on card, user decides) per issue #30.
            val existing: List<Transaction>
            try {
                existing = transactionRepository.getAllTransaction().firstOrNull() ?: emptyList()
            } catch (_: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Could not verify duplicates — please review carefully.",
                        duplicateCheckAvailable = false,
                    )
                }
                return@launch
            }
            val stateSnapshot = _state.value
            val defaultAccount = defaultAccount(stateSnapshot)
            val drafts = parsed.map { p ->
                val type = p.transactionType
                val category = defaultCategoryFor(type, null)
                ImportDraft(
                    parsed = p,
                    amountText = numberFormatRepository.formatForEditing(p.amount),
                    notes = p.defaultNotes,
                    dateTime = p.dateTime,
                    transactionType = type,
                    selectedAccount = defaultAccount,
                    selectedCategory = category,
                    isDuplicate = isDuplicate(p, existing),
                    // Opt-in: nothing is selected until the user swipes
                    // right / taps Add (or uses Select all).
                    isSelected = false,
                )
            }
            undoStack.clear()
            _state.update {
                it.copy(
                    isLoading = false,
                    drafts = drafts,
                    currentIndex = 0,
                    acceptedCount = 0,
                    rejectedCount = 0,
                    canUndo = false,
                    lastAction = null,
                    topExpenseCategories = topCategoriesFor(
                        TransactionType.EXPENSE,
                        existing,
                        allCategories,
                    ),
                    topIncomeCategories = topCategoriesFor(
                        TransactionType.INCOME,
                        existing,
                        allCategories,
                    ),
                )
            }
        }
    }

    private fun defaultAccount(state: ImportState) =
        state.accounts.firstOrNull()

    private fun defaultCategoryFor(type: TransactionType, current: Category?): Category? {
        if (current != null) {
            val matches = if (type.isIncome()) current.type.isIncome() else current.type.isExpense()
            if (matches) return current
        }
        val filtered = allCategories.filter {
            if (type.isIncome()) it.type.isIncome() else it.type.isExpense()
        }
        return filtered.firstOrNull()
    }

    internal fun isDuplicate(
        parsed: ParsedTransaction,
        existing: List<Transaction>,
    ): Boolean {
        return existing.any { t ->
            t.type == parsed.transactionType &&
                kotlin.math.abs(t.amount.amount - parsed.amount) < DUPLICATE_AMOUNT_TOLERANCE &&
                matchesWhen(t.createdOn, parsed)
        }
    }

    /**
     * Statements that print a time of day (BHIM) can only match an identical
     * timestamp. Date-only statements (SBI YONO) carry no time, so requiring an
     * exact match would never flag a re-imported row: they match anywhere on the
     * same calendar day instead.
     */
    private fun matchesWhen(existing: Date, parsed: ParsedTransaction): Boolean {
        return if (parsed.isDateOnly) {
            isSameDay(existing, parsed.dateTime)
        } else {
            existing == parsed.dateTime
        }
    }

    private fun isSameDay(a: Date, b: Date): Boolean {
        val ca = Calendar.getInstance().apply { time = a }
        val cb = Calendar.getInstance().apply { time = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    internal fun topCategoriesFor(
        type: TransactionType,
        existing: List<Transaction>,
        categories: List<Category>,
    ): List<Category> {
        val usage = existing
            .filter { it.type == type }
            .groupingBy { it.categoryId }
            .eachCount()
        return categories
            .filter { category ->
                if (type.isIncome()) category.type.isIncome() else category.type.isExpense()
            }
            .sortedByDescending { usage[it.id] ?: 0 }
            .take(MAX_QUICK_CATEGORIES)
    }

    private fun acceptCurrent() {
        val s = _state.value
        val draft = s.currentDraft ?: return
        if (!draft.isImportable) {
            _state.update {
                it.copy(errorMessage = "Correct the date or skip failed rows before adding.")
            }
            return
        }
        undoStack.addLast(UndoEntry(s.currentIndex, draft.parsed.id, draft.isSelected))
        updateDraft(draft.parsed.id) { it.copy(isSelected = true) }
        _state.update {
            it.copy(
                currentIndex = (it.currentIndex + 1).coerceAtMost(it.drafts.size),
                acceptedCount = it.acceptedCount + 1,
                lastAction = ImportReviewAction.ADDED,
                canUndo = true,
            )
        }
    }

    private fun rejectCurrent() {
        val s = _state.value
        val draft = s.currentDraft ?: return
        undoStack.addLast(UndoEntry(s.currentIndex, draft.parsed.id, draft.isSelected))
        updateDraft(draft.parsed.id) { it.copy(isSelected = false) }
        _state.update {
            it.copy(
                currentIndex = (it.currentIndex + 1).coerceAtMost(it.drafts.size),
                rejectedCount = it.rejectedCount + 1,
                lastAction = ImportReviewAction.SKIPPED,
                canUndo = true,
            )
        }
    }

    private fun undoLast() {
        val entry = undoStack.removeLastOrNull() ?: return
        updateDraft(entry.draftId) { it.copy(isSelected = entry.wasSelected) }
        _state.update {
            it.copy(
                currentIndex = entry.index,
                acceptedCount = if (it.lastAction == ImportReviewAction.ADDED) (it.acceptedCount - 1).coerceAtLeast(0) else it.acceptedCount,
                rejectedCount = if (it.lastAction == ImportReviewAction.SKIPPED) (it.rejectedCount - 1).coerceAtLeast(0) else it.rejectedCount,
                lastAction = null,
                canUndo = undoStack.isNotEmpty(),
            )
        }
    }

    private fun updateDraft(draftId: String, transform: (ImportDraft) -> ImportDraft) {
        _state.update { s ->
            s.copy(drafts = s.drafts.map { if (it.parsed.id == draftId) transform(it) else it })
        }
    }

    private fun saveSelected() {
        val s = _state.value
        // Guard against double-confirm taps while a save is in flight.
        if (s.isSaving) return
        val selected = s.drafts.filter { it.isSelected && it.isImportable }
        if (selected.isEmpty()) {
            _state.update { it.copy(errorMessage = "Nothing valid selected to import.") }
            return
        }
        _state.update { it.copy(isSaving = true, errorMessage = null, savedCount = 0, saveTotal = selected.size) }
        viewModelScope.launch {
            var saved = 0
            var skipped = 0
            var failed = false
            // Per-row inserts: the repository exposes no batch API, so a
            // mid-import failure or back-navigation can leave partial writes,
            // reported here as imported/skipped counts.
            try {
                for (draft in selected) {
                    val amountValue = numberFormatRepository.parseToDouble(draft.amountText)
                    val accountId = draft.selectedAccount?.id
                    val categoryId = draft.selectedCategory?.id
                    if (amountValue == null || amountValue <= 0.0 || accountId.isNullOrBlank() || categoryId.isNullOrBlank()) {
                        skipped++
                    } else {
                        val transaction = Transaction(
                            id = UUID.randomUUID().toString(),
                            notes = draft.notes,
                            categoryId = categoryId,
                            fromAccountId = accountId,
                            toAccountId = null,
                            type = draft.transactionType,
                            amount = Amount(amountValue),
                            imagePath = "",
                            createdOn = draft.dateTime,
                            updatedOn = Calendar.getInstance().time,
                        )
                        when (addTransactionUseCase.invoke(transaction)) {
                            is Resource.Success -> saved++
                            is Resource.Error -> skipped++
                        }
                    }
                    _state.update { it.copy(savedCount = saved, saveTotal = selected.size) }
                }
            } catch (e: Exception) {
                // Never swallow coroutine cancellation (e.g. back navigation).
                if (e is CancellationException) throw e
                failed = true
            } finally {
                _state.update {
                    it.copy(
                        isSaving = false,
                        isDone = !failed && saved > 0 && skipped == 0,
                        errorMessage = when {
                            failed -> "Import interrupted — $saved of ${selected.size} imported."
                            saved > 0 && skipped > 0 -> "Imported $saved, skipped $skipped (check amount/account/category)."
                            saved == 0 -> "Could not import. Check amount, account and category."
                            else -> null
                        },
                    )
                }
            }
            if (!failed && saved > 0 && skipped == 0) {
                appComposeNavigator.popBackStack()
            }
        }
    }

    companion object {
        const val MAX_QUICK_CATEGORIES = 5
        private const val DUPLICATE_AMOUNT_TOLERANCE = 0.005
    }
}
