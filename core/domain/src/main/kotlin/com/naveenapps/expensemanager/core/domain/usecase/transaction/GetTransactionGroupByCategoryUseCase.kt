package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.PieChartData
import com.naveenapps.expensemanager.core.model.getDummyPieChartData
import com.naveenapps.expensemanager.core.model.isTransfer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn

class GetTransactionGroupByCategoryUseCase(
    private val getAllCategoryUseCase: GetAllCategoryUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val appCoroutineDispatchers: AppCoroutineDispatchers
) {
    fun invoke(categoryType: CategoryType): Flow<CategoryTransactionState> {
        return combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
        ) { currency, transactions ->

            val maps = if (transactions?.isNotEmpty() == true) {
                transactions.filterNot { it.type.isTransfer() }.groupBy { it.categoryId }
                    .map { it.value[0].category to it.value }.toMap()
            } else {
                emptyMap()
            }

            val filteredTransaction = maps.filter { it.key.type == categoryType }

            val totalAmount =
                filteredTransaction.map { it.value.sumOf { transactions -> transactions.amount.amount } }
                    .sumOf { it }

            val categoryTransactions = buildList {
                filteredTransaction.map {
                    val totalSpentAmount = it.value.sumOf {
                        it.amount.amount
                    }
                    add(
                        CategoryTransaction(
                            it.key,
                            percent = (totalSpentAmount / totalAmount).toFloat() * 100,
                            getFormattedAmountUseCase.invoke(
                                amount = totalSpentAmount,
                                currency = currency,
                            ),
                            it.value,
                        ),
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
                                    getFormattedAmountUseCase.invoke(
                                        amount = 0.0,
                                        currency = currency,
                                    ),
                                    emptyList(),
                                ),
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

            CategoryTransactionState(
                pieChartData = pieChartData,
                totalAmount = getFormattedAmountUseCase.invoke(
                    amount = totalAmount,
                    currency = currency,
                ),
                categoryTransactions = newCategoryTransaction,
                hideValues = categoryTransactions.isEmpty(),
                categoryType = categoryType
            )
        }.flowOn(appCoroutineDispatchers.computation)
    }
}

fun CategoryTransaction.toChartModel(): PieChartData {
    return PieChartData(
        name = this.category.name,
        value = this.percent,
        color = this.category.storedIcon.backgroundColor,
    )
}
