package com.nkuppan.expensemanager.feature.analysis.expense

import android.graphics.Color
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.core.model.Transaction
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.core.ui.utils.getColorValue
import com.nkuppan.expensemanager.core.ui.utils.getCurrency
import com.nkuppan.expensemanager.feature.transaction.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GraphData(
    val type: GraphItemType,
    val amount: Double,
    val amountText: UiText,
    val transactions: List<Transaction>,
    val graphData: PieData? = null,
    val categoryType: CategoryType = CategoryType.EXPENSE,
    var categoryTotalAmount: Double = 0.0
)

enum class GraphItemType {
    GRAPH_ITEM,
    GRAPH_CATEGORY_TITLE,
    GRAPH_CATEGORY_ITEM
}

fun sortDataUsingType(data: Map<Category, List<Transaction>>, categoryType: CategoryType) =
    data
        .filter {
            it.key.type == categoryType
        }
        .toList()
        .sortedByDescending { pair ->
            pair.second.sumOf { it.amount }
        }

fun List<List<Transaction>>.constructGraphData(): PieData {

    val pieChartEntries = arrayListOf<PieEntry>()
    val colors = arrayListOf<Int>()

    this.forEach { transaction ->
        val category = transaction[0].category
        pieChartEntries.add(
            PieEntry(
                transaction.sumOf { it.amount }.toFloat(),
                category.name
            )
        )
        colors.add(getColorValue(category.backgroundColor))
    }

    val pieDataSet = PieDataSet(pieChartEntries, "")

    pieDataSet.colors = colors
    pieDataSet.sliceSpace = 2f
    pieDataSet.valueTextColor = Color.WHITE
    pieDataSet.valueTextSize = 12f

    return PieData(pieDataSet)
}

/**
 * @param data it will have data with pair values
 */
suspend fun constructGraphItems(
    data: Map<Category, List<Transaction>>,
    categoryType: CategoryType,
    currencySymbol: Int
): List<GraphData> = withContext(Dispatchers.IO) {

    val itemSortedByAmount = sortDataUsingType(data, categoryType)

    val finalAdapterItems = mutableListOf<GraphData>()

    if (itemSortedByAmount.isNotEmpty()) {

        val amount = itemSortedByAmount[0].second.sumOf { it.amount }

        finalAdapterItems.add(
            GraphData(
                GraphItemType.GRAPH_ITEM,
                amount,
                getCurrency(currencySymbol, amount),
                emptyList(),
                itemSortedByAmount.map { it.second }.constructGraphData(),
                categoryType = categoryType
            )
        )

        if (itemSortedByAmount.any()) {

            var categoryTotalAmount = 0.0

            val categoryItems = itemSortedByAmount.map {
                val totalAmount = it.second.sumOf { pair -> pair.amount }
                categoryTotalAmount += totalAmount

                GraphData(
                    GraphItemType.GRAPH_CATEGORY_ITEM,
                    totalAmount,
                    getCurrency(currencySymbol, totalAmount),
                    it.second,
                    null,
                    categoryType = categoryType
                )
            }

            val headerItem = GraphData(
                GraphItemType.GRAPH_CATEGORY_TITLE,
                categoryTotalAmount,
                getCurrency(currencySymbol, categoryTotalAmount),
                emptyList(),
                null,
                categoryType = categoryType
            )

            finalAdapterItems.add(headerItem)
            finalAdapterItems.addAll(
                categoryItems.map {
                    it.categoryTotalAmount = categoryTotalAmount
                    it
                }
            )
        }
    }

    return@withContext finalAdapterItems
}
