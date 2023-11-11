package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.ui.components.PieChartUiData
import com.naveenapps.expensemanager.core.designsystem.ui.components.PieChartView
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.CategoryTransactionUiModel
import com.naveenapps.expensemanager.feature.category.transaction.getRandomCategoryTransactionData


@Composable
fun CategoryAmountView(
    modifier: Modifier = Modifier,
    categoryTransactionUiModel: CategoryTransactionUiModel
) {
    Column(modifier = modifier) {
        DashboardWidgetTitle(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(id = R.string.categories)
        )
        Row(modifier = Modifier.padding(top = 16.dp)) {
            PieChartView(
                totalAmountText = categoryTransactionUiModel.totalAmount.amountString ?: "",
                chartData = categoryTransactionUiModel.pieChartData.map {
                    PieChartUiData(
                        it.name,
                        it.value,
                        it.color
                    )
                },
                hideValues = true,
                chartHeight = 300,
                chartWidth = 300
            )
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(categoryTransactionUiModel.categoryTransactions.size) {
                    if (it < 4) {
                        val item = categoryTransactionUiModel.categoryTransactions[it]
                        com.naveenapps.expensemanager.feature.category.transaction.CategoryTransactionSmallItem(
                            name = item.category.name,
                            icon = item.category.iconName,
                            iconBackgroundColor = item.category.iconBackgroundColor,
                            amount = item.amount.amountString ?: ""
                        )
                    }
                }
            }
        }
    }
}

@com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
@Composable
fun CategoryAmountViewPreview() {
    ExpenseManagerTheme {
        Surface {
            CategoryAmountView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                categoryTransactionUiModel = getRandomCategoryTransactionData()
            )
        }
    }
}