package com.naveenapps.expensemanager.feature.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.getAmountTextColor
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionGroup
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionListViewModel(
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    appCoroutineDispatchers: AppCoroutineDispatchers,
    private val appComposeNavigator: AppComposeNavigator,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val imageStorageRepository: ImageStorageRepository,
) : ViewModel() {

    private val _transactions = MutableStateFlow(TransactionListState(emptyList(), isLoading = true))
    val state = _transactions.asStateFlow()

    private val _event = Channel<TransactionListEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val searchQuery = MutableStateFlow("")

    // The transactions currently listed, by id, so a swipe-delete can act on the full model.
    private var visibleTransactions: Map<String, Transaction> = emptyMap()

    // Deleted but still undoable. Its attachment files are only removed once the undo window
    // has passed; otherwise "Undo" would bring back a transaction pointing at missing photos.
    private var pendingUndo: Transaction? = null

    init {
        combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
            searchQuery,
        ) { currency, transactions, query ->

            val matching = transactions.orEmpty().filter { it.matches(query) }
            visibleTransactions = matching.associateBy { it.id }

            val groupedItem = matching.groupBy {
                it.createdOn.toCompleteDateWithDate()
            }.map {
                val totalAmount = it.value.toTransactionSum()
                TransactionGroup(
                    date = it.key,
                    amountTextColor = totalAmount.getAmountTextColor(),
                    totalAmount = getFormattedAmountUseCase.invoke(totalAmount, currency),
                    transactions = it.value.map { transaction ->
                        transaction.toTransactionUIModel(
                            getFormattedAmountUseCase.invoke(
                                transaction.amount.amount,
                                currency,
                            ),
                        )
                    },
                )
            }

            _transactions.update {
                it.copy(
                    transactionListItem = groupedItem.convertGroupToTransactionListItems(),
                    isLoading = false,
                )
            }
        }.flowOn(appCoroutineDispatchers.computation).launchIn(viewModelScope)
    }

    private fun openCreateScreen(transactionId: String? = null) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate(transactionId),
        )
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun search(query: String) {
        searchQuery.value = query
        _transactions.update { it.copy(searchQuery = query) }
    }

    private fun deleteTransaction(transactionId: String) {
        val transaction = visibleTransactions[transactionId] ?: return
        viewModelScope.launch {
            // A second delete inside the undo window makes the first one final.
            finalizePendingDelete()
            if (deleteTransactionUseCase(transaction) is Resource.Success) {
                pendingUndo = transaction
                _event.send(TransactionListEvent.ShowUndoDelete)
            } else {
                _event.send(TransactionListEvent.ShowMessage(DesignSystemR.string.error_delete_failed))
            }
        }
    }

    private fun undoDelete() {
        val transaction = pendingUndo ?: return
        pendingUndo = null
        viewModelScope.launch {
            // Re-adding goes through the normal path, so account balances are re-applied.
            if (addTransactionUseCase(transaction) !is Resource.Success) {
                transaction.attachments.forEach { imageStorageRepository.deleteTransactionAttachment(it) }
                _event.send(TransactionListEvent.ShowMessage(DesignSystemR.string.error_save_failed))
            }
        }
    }

    private fun finalizePendingDelete() {
        val transaction = pendingUndo ?: return
        pendingUndo = null
        transaction.attachments.forEach { imageStorageRepository.deleteTransactionAttachment(it) }
    }

    override fun onCleared() {
        finalizePendingDelete()
        super.onCleared()
    }

    fun processAction(action: TransactionListAction) {
        when (action) {
            TransactionListAction.ClosePage -> closePage()
            TransactionListAction.OpenCreateTransaction -> openCreateScreen()
            is TransactionListAction.OpenEdiTransaction -> openCreateScreen(action.transactionId)
            is TransactionListAction.Search -> search(action.query)
            is TransactionListAction.DeleteTransaction -> deleteTransaction(action.transactionId)
            TransactionListAction.UndoDelete -> undoDelete()
            TransactionListAction.UndoExpired -> finalizePendingDelete()
        }
    }
}

/**
 * Case-insensitive match on the free-text parts of a transaction: notes, category name and
 * account names. Built-in categories store an English name, so a translated category label
 * only matches by its English name.
 */
internal fun Transaction.matches(query: String): Boolean {
    val needle = query.trim()
    if (needle.isEmpty()) return true
    return listOfNotNull(notes, category.name, fromAccount.name, toAccount?.name)
        .any { it.contains(needle, ignoreCase = true) }
}

fun List<Transaction>.toTransactionSum() = this.sumOf {
    when (it.type) {
        TransactionType.INCOME -> {
            it.amount.amount
        }

        TransactionType.EXPENSE -> {
            it.amount.amount * -1
        }

        TransactionType.TRANSFER -> {
            0.0
        }
    }
}

fun List<TransactionGroup>.convertGroupToTransactionListItems(): List<TransactionListItem> = buildList {
    this@convertGroupToTransactionListItems.forEach {
        add(
            TransactionListItem.HeaderItem(
                date = it.date,
                amountTextColor = it.amountTextColor,
                totalAmount = it.totalAmount.amountString ?: "",
            ),
        )

        it.transactions.forEach {
            add(TransactionListItem.TransactionItem(date = it))
        }

        add(TransactionListItem.Divider)
    }
}

sealed class TransactionListItem {

    data class HeaderItem(
        val date: String,
        val amountTextColor: Int,
        val totalAmount: String,
    ) : TransactionListItem()

    data class TransactionItem(
        val date: TransactionUiItem,
    ) : TransactionListItem()

    data object Divider : TransactionListItem()
}
