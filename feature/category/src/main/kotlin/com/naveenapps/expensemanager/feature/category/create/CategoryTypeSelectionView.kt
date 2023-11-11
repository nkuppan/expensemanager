package com.naveenapps.expensemanager.feature.category.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppFilterChip
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.feature.category.R


@Composable
fun CategoryTypeSelectionView(
    selectedCategoryType: CategoryType,
    onCategoryTypeChange: ((CategoryType) -> Unit),
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            filterName = stringResource(id = R.string.income),
            isSelected = selectedCategoryType.isIncome(),
            filterIcon = android.R.drawable.arrow_down_float,
            filterSelectedColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
            onClick = {
                onCategoryTypeChange.invoke(CategoryType.INCOME)
            }
        )
        AppFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            filterName = stringResource(id = R.string.spending),
            isSelected = selectedCategoryType.isExpense(),
            filterIcon = android.R.drawable.arrow_up_float,
            filterSelectedColor = com.naveenapps.expensemanager.core.common.R.color.red_500,
            onClick = {
                onCategoryTypeChange.invoke(CategoryType.EXPENSE)
            }
        )
    }
}

@Preview
@Composable
private fun CategoryTypeSelectionViewPreview() {
    ExpenseManagerTheme {
        Column {
            CategoryTypeSelectionView(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                selectedCategoryType = CategoryType.EXPENSE,
                onCategoryTypeChange = {}
            )
            CategoryTypeSelectionView(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                selectedCategoryType = CategoryType.INCOME,
                onCategoryTypeChange = {}
            )
        }
    }
}
