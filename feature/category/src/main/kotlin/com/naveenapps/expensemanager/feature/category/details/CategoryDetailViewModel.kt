package com.naveenapps.expensemanager.feature.category.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdFlowUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens.CategoryDetails.KEY_CATEGORY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val findCategoryByIdFlowUseCase: FindCategoryByIdFlowUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val appComposeNavigator: AppComposeNavigator
) : ViewModel() {

    private val _category = MutableStateFlow<CategoryTransaction?>(null)
    val category = _category.asStateFlow()

    private val _categoryTransactions = MutableStateFlow<List<TransactionUiItem>>(emptyList())
    val categoryTransactions = _categoryTransactions.asStateFlow()

    init {
        savedStateHandle.get<String>(KEY_CATEGORY_ID)?.let {
            combine(
                getCurrencyUseCase.invoke(),
                findCategoryByIdFlowUseCase.invoke(it),
                getTransactionWithFilterUseCase.invoke()
            ) { currency, category, transactions ->
                if (category != null && transactions?.isNotEmpty() == true) {
                    val filterTransaction = transactions.filter { transaction ->
                        transaction.type.ordinal == category.type.ordinal
                    }

                    val totalSpent = filterTransaction.sumOf { it.amount.amount }

                    val categoryTransaction = filterTransaction
                        .filter { it.categoryId == category.id }
                        .map { transaction ->
                            transaction.toTransactionUIModel(
                                getFormattedAmountUseCase.invoke(
                                    transaction.amount.amount, currency
                                )
                            )
                        }

                    val categoryAmount = categoryTransaction.sumOf { it.amount.amount }

                    _category.value = CategoryTransaction(
                        category = category,
                        percent = (categoryAmount / totalSpent).toFloat() * 100,
                        amount = getFormattedAmountUseCase.invoke(
                            amount = categoryAmount,
                            currency = currency
                        ),
                        transaction = filterTransaction
                    )
                    _categoryTransactions.value = categoryTransaction
                }
            }.launchIn(viewModelScope)
        }
    }

    fun openCategoryEditScreen() {
        val categoryId = savedStateHandle.get<String>(KEY_CATEGORY_ID)
        categoryId ?: return

        appComposeNavigator.navigate(
            ExpenseManagerScreens.CategoryCreate.createRoute(categoryId)
        )
    }

    fun openTransactionCreateScreen(transactionId: String?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate.createRoute(transactionId ?: "")
        )
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}