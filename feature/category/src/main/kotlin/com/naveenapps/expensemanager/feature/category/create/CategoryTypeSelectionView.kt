package com.naveenapps.expensemanager.feature.category.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmSegmented
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.feature.category.R

@Composable
fun CategoryTypeSelectionView(
    selectedCategoryType: CategoryType,
    onCategoryTypeChange: ((CategoryType) -> Unit),
    modifier: Modifier = Modifier,
) {
    val types = listOf(CategoryType.INCOME, CategoryType.EXPENSE)
    EmSegmented(
        options = listOf(stringResource(id = R.string.income), stringResource(id = R.string.expense)),
        selectedIndex = types.indexOf(selectedCategoryType),
        onSelect = { onCategoryTypeChange(types[it]) },
        modifier = modifier,
        icons = listOf(Icons.AutoMirrored.Rounded.TrendingDown, Icons.AutoMirrored.Rounded.TrendingUp),
        accents = listOf(EmTheme.colors.income, EmTheme.colors.expense),
    )
}

@EmPreviews
@Composable
private fun CategoryTypeSelectionViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        Column {
            CategoryTypeSelectionView(
                modifier = Modifier
                    .padding(EmTheme.spacing.lg)
                    .fillMaxWidth(),
                selectedCategoryType = CategoryType.INCOME,
                onCategoryTypeChange = {},
            )
            CategoryTypeSelectionView(
                modifier = Modifier
                    .padding(EmTheme.spacing.lg)
                    .fillMaxWidth(),
                selectedCategoryType = CategoryType.EXPENSE,
                onCategoryTypeChange = {},
            )
        }
    }
}
