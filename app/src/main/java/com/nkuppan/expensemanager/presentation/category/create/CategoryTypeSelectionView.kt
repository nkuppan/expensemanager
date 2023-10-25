package com.nkuppan.expensemanager.presentation.category.create

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme


@Composable
fun CategoryTypeSelectionView(
    selectedCategoryType: CategoryType,
    onCategoryTypeChange: ((CategoryType) -> Unit),
    modifier: Modifier = Modifier
) {

    Row(modifier = modifier) {
        CustomCategoryFilterChip(
            selectedCategoryType = selectedCategoryType,
            categoryType = CategoryType.INCOME,
            filterName = stringResource(id = R.string.income),
            filterIcon = R.drawable.ic_arrow_downward,
            filterSelectedColor = R.color.green_500,
            onCategoryTypeChange = onCategoryTypeChange,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        CustomCategoryFilterChip(
            selectedCategoryType = selectedCategoryType,
            categoryType = CategoryType.EXPENSE,
            filterName = stringResource(id = R.string.expense),
            filterIcon = R.drawable.ic_arrow_upward,
            filterSelectedColor = R.color.red_500,
            onCategoryTypeChange = onCategoryTypeChange,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RowScope.CustomCategoryFilterChip(
    selectedCategoryType: CategoryType,
    categoryType: CategoryType,
    filterName: String,
    @DrawableRes filterIcon: Int,
    @ColorRes filterSelectedColor: Int,
    onCategoryTypeChange: ((CategoryType) -> Unit),
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier = modifier,
        selected = selectedCategoryType == categoryType,
        onClick = {
            onCategoryTypeChange.invoke(categoryType)
        },
        label = {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = filterName
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = filterIcon),
                contentDescription = ""
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedLabelColor = Color.White,
            selectedContainerColor = colorResource(id = filterSelectedColor),
            selectedLeadingIconColor = Color.White
        )
    )
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
