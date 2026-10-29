package com.naveenapps.expensemanager.feature.category.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.PieChartUiData
import com.naveenapps.expensemanager.core.designsystem.ui.components.PieChartView
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState

/**
 * The pie chart of spending (or income) per category in a card, with [centerLabel] and the
 * total in the middle. Shared by the Categories tab and a budget's details.
 */
@Composable
fun CategorySpendingChartCard(
    state: CategoryTransactionState,
    centerLabel: String,
    modifier: Modifier = Modifier,
    onChartClick: (() -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    val indicationSource = remember { MutableInteractionSource() }
    AppCardView(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PieChartView(
                totalAmountText = centerLabel + "\n" + state.totalAmount.amountString,
                chartData = state.pieChartData.map {
                    PieChartUiData(
                        it.titleResId?.let { resId -> stringResource(resId) } ?: it.name,
                        it.value,
                        it.color.toColorInt(),
                    )
                },
                chartHeight = 600,
                hideValues = state.hideValues,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .then(
                        if (onChartClick != null) {
                            Modifier.clickable(indicationSource, null, onClick = onChartClick)
                        } else {
                            Modifier
                        },
                    ),
            )
            footer?.invoke()
        }
    }
}

/**
 * One grouped card per category with its amount and share, largest first. A [selectedCategoryId]
 * gets an outline (used to show which category the list below is filtered to).
 */
fun LazyListScope.categorySpendingItems(
    categoryTransactions: List<CategoryTransaction>,
    onItemClick: (CategoryTransaction) -> Unit,
    selectedCategoryId: String? = null,
) {
    itemsIndexed(
        items = categoryTransactions,
        key = { _, item -> "category_${item.category.id}" },
    ) { index, categoryTransaction ->
        val isSelected = categoryTransaction.category.id == selectedCategoryId
        AppCardView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 2.dp),
            shape = AppCardViewDefaults.cardShape(index, categoryTransactions),
            border = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                CardDefaults.outlinedCardBorder()
            },
        ) {
            CategoryTransactionItem(
                modifier = Modifier.clickable { onItemClick(categoryTransaction) },
                name = categoryTransaction.category.titleResId?.let { stringResource(it) }
                    ?: categoryTransaction.category.name,
                icon = categoryTransaction.category.storedIcon.name,
                iconBackgroundColor = categoryTransaction.category.storedIcon.backgroundColor,
                customImagePath = categoryTransaction.category.storedIcon.customImagePath,
                amount = categoryTransaction.amount.amountString ?: "",
                percentage = categoryTransaction.percent,
            )
        }
    }
}
