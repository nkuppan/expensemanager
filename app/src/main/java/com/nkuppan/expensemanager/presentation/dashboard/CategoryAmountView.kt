package com.nkuppan.expensemanager.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.presentation.category.transaction.CategoryTransactionSmallItem
import com.nkuppan.expensemanager.presentation.category.transaction.CategoryTransactionUiModel
import com.nkuppan.expensemanager.presentation.category.transaction.PieChartView
import com.nkuppan.expensemanager.presentation.category.transaction.getRandomCategoryTransactionData
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.theme.widget.AppCardView
import com.nkuppan.expensemanager.ui.utils.UiText
import com.nkuppan.expensemanager.utils.AppPreviewsLightAndDarkMode


@Composable
fun CategoryAmountView(
    transactionPeriod: UiText,
    modifier: Modifier = Modifier,
    categoryTransactionUiModel: CategoryTransactionUiModel
) {

    val context = LocalContext.current

    AppCardView(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row {
                Column {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.categories),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = transactionPeriod.asString(context),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row {
                PieChartView(
                    totalAmountText = categoryTransactionUiModel.totalAmount.asString(context),
                    chartData = categoryTransactionUiModel.pieChartData,
                    hideValues = true,
                    chartHeight = 300,
                    chartWidth = 300
                )
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(categoryTransactionUiModel.categoryTransactions.size) {
                        if (it < 4) {
                            val item = categoryTransactionUiModel.categoryTransactions[it]
                            CategoryTransactionSmallItem(
                                name = item.category.name,
                                icon = item.category.iconName,
                                iconBackgroundColor = item.category.iconBackgroundColor,
                                amount = item.amount.asString(context)
                            )
                        }
                    }
                }
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun CategoryAmountViewPreview() {
    ExpenseManagerTheme {
        CategoryAmountView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            transactionPeriod = UiText.DynamicString("This Month(Oct 2023)"),
            categoryTransactionUiModel = getRandomCategoryTransactionData()
        )
    }
}