package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.usecase.category.GetAllCategoryUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.presentation.category.transaction.CategoryTransaction
import com.nkuppan.expensemanager.presentation.category.transaction.CategoryTransactionUiModel
import com.nkuppan.expensemanager.presentation.category.transaction.getDummyPieChartData
import com.nkuppan.expensemanager.presentation.category.transaction.toChartModel
import com.nkuppan.expensemanager.ui.utils.getCurrency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetTransactionGroupByCategoryUseCase @Inject constructor(
    private val getAllCategoryUseCase: GetAllCategoryUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
) {
    fun invoke(categoryType: CategoryType): Flow<CategoryTransactionUiModel> {
        return combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke()
        ) { currency, transactions ->

            val maps = if (transactions?.isNotEmpty() == true) {
                transactions.groupBy { it.categoryId }
                    .map { it.value[0].category to it.value }.toMap()
            } else {
                emptyMap()
            }

            val filteredTransaction = maps.filter { it.key.type == categoryType }

            val totalAmount =
                filteredTransaction.map { it.value.sumOf { transactions -> transactions.amount } }
                    .sumOf { it }

            val categoryTransactions = buildList {
                filteredTransaction.map {
                    val totalSpentAmount = it.value.sumOf {
                        it.amount
                    }
                    add(
                        CategoryTransaction(
                            it.key,
                            percent = (totalSpentAmount / totalAmount).toFloat() * 100,
                            getCurrency(
                                currency = currency,
                                amount = totalSpentAmount
                            ),
                            it.value
                        )
                    )
                }
            }.sortedByDescending { it.percent }

            var newCategoryTransaction = categoryTransactions

            val pieChartData = if (categoryTransactions.isEmpty()) {
                val categories = getAllCategoryUseCase.invoke().firstOrNull()?.filter {
                    it.type == categoryType
                }

                newCategoryTransaction = buildList {
                    categories?.size?.let {
                        repeat(it) {
                            add(
                                CategoryTransaction(
                                    categories[it],
                                    percent = 0.0f,
                                    getCurrency(
                                        currency = currency,
                                        amount = 0.0
                                    ),
                                    emptyList()
                                )
                            )
                        }
                    }
                }
                buildList {
                    categories?.size?.let { size ->
                        repeat(size) {
                            add(getDummyPieChartData(categories[it].name, (100 / size).toFloat()))
                        }
                    }
                }
            } else {
                categoryTransactions.map {
                    it.toChartModel()
                }
            }

            CategoryTransactionUiModel(
                pieChartData = pieChartData,
                totalAmount = getCurrency(
                    currency = currency,
                    amount = totalAmount
                ),
                categoryTransactions = newCategoryTransaction,
                hideValues = categoryTransactions.isEmpty()
            )
        }
    }
}