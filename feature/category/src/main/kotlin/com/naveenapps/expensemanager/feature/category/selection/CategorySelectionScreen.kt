package com.naveenapps.expensemanager.feature.category.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.SelectionTitle
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.category.list.CategoryItem
import com.naveenapps.expensemanager.feature.category.list.getRandomCategoryData

@Composable
fun CategorySelectionScreen(
    modifier: Modifier = Modifier,
    categories: List<Category> = emptyList(),
    selectedCategory: Category? = null,
    onItemSelection: ((Category) -> Unit)? = null
) {
    LazyColumn(modifier = modifier) {
        item {
            SelectionTitle(stringResource(id = R.string.select_category))
        }
        items(categories) { category ->
            val isSelected = selectedCategory?.id == category.id
            Box(
                modifier = Modifier
                    .clickable {
                        onItemSelection?.invoke(category)
                    }
                    .then(
                        if (isSelected) {
                            Modifier
                                .padding(4.dp)
                                .background(
                                    color = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.green_500).copy(
                                        alpha = .1f
                                    ),
                                    shape = RoundedCornerShape(size = 12.dp)
                                )
                        } else {
                            Modifier.padding(4.dp)
                        }
                    )
                    .padding(12.dp)
            ) {
                CategoryItem(
                    modifier = Modifier,
                    name = category.name,
                    icon = category.iconName,
                    iconBackgroundColor = category.iconBackgroundColor,
                    endIcon = if (isSelected) {
                        //TODO
                        android.R.drawable.stat_sys_upload_done
                    } else {
                        null
                    }
                )
            }
        }
    }
}


@Preview
@Composable
private fun CategorySelectionScreenPreview() {
    ExpenseManagerTheme {
        CategorySelectionScreen(
            categories = getRandomCategoryData(),
            selectedCategory = getRandomCategoryData().firstOrNull()
        )
    }
}