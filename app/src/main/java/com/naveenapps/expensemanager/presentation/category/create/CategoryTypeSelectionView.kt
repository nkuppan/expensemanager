package com.naveenapps.expensemanager.presentation.category.create

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
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.domain.model.CategoryType
import com.naveenapps.expensemanager.domain.model.isExpense
import com.naveenapps.expensemanager.domain.model.isIncome
import com.naveenapps.expensemanager.ui.components.AppFilterChip
import com.naveenapps.expensemanager.ui.theme.ExpenseManagerTheme


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
            filterIcon = R.drawable.ic_arrow_downward,
            filterSelectedColor = R.color.green_500,
            onClick = {
                onCategoryTypeChange.invoke(CategoryType.INCOME)
            }
        )
        AppFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            filterName = stringResource(id = R.string.spending),
            isSelected = selectedCategoryType.isExpense(),
            filterIcon = R.drawable.ic_arrow_upward,
            filterSelectedColor = R.color.red_500,
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
