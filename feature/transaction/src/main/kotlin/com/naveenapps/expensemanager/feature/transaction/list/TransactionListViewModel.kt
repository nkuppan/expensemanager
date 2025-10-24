package com.naveenapps.expensemanager.feature.transaction.list

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.getAmountTextColor
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionGroup
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update


class TransactionListViewModel(
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    appCoroutineDispatchers: AppCoroutineDispatchers,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _transactions = MutableStateFlow(TransactionListState(emptyList()))
    val state = _transactions.asStateFlow()

    init {
        combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
        ) { currency, transactions ->

            val groupedItem = transactions?.groupBy {
                it.createdOn.toCompleteDateWithDate()
            }?.map {
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
                    transactionListItem = groupedItem?.convertGroupToTransactionListItems()
                        ?: emptyList()
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

    fun processAction(action: TransactionListAction) {
        when (action) {
            TransactionListAction.ClosePage -> closePage()
            TransactionListAction.OpenCreateTransaction -> openCreateScreen()
            is TransactionListAction.OpenEdiTransaction -> openCreateScreen(action.transactionId)
        }
    }
}

fun List<Transaction>.toTransactionSum() =
    this.sumOf {
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

fun List<TransactionGroup>.convertGroupToTransactionListItems(): List<TransactionListItem> {
    return buildList {
        this@convertGroupToTransactionListItems.forEach {
            add(
                TransactionListItem.HeaderItem(
                    date = it.date,
                    amountTextColor = it.amountTextColor,
                    totalAmount = it.totalAmount.amountString ?: ""
                )
            )

            it.transactions.forEach {
                add(TransactionListItem.TransactionItem(date = it))
            }

            add(TransactionListItem.Divider)
        }
    }
}

sealed class TransactionListItem {

    data class HeaderItem(
        val date: String,
        @DrawableRes val amountTextColor: Int,
        val totalAmount: String,
    ) : TransactionListItem()

    data class TransactionItem(
        val date: TransactionUiItem,
    ) : TransactionListItem()

    data object Divider : TransactionListItem()
}
