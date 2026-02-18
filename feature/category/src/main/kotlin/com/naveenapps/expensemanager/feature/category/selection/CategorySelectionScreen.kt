package com.naveenapps.expensemanager.feature.category.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.components.SelectionHeader
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.category.list.getRandomCategoryData

@Composable
fun CategorySelectionScreen(
    modifier: Modifier = Modifier,
    categories: List<Category> = emptyList(),
    selectedCategory: Category? = null,
    createNewCallback: (() -> Unit)? = null,
    onItemSelection: ((Category) -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 48.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            SelectionHeader(
                title = stringResource(id = R.string.select_category),
                createNewCallback = createNewCallback,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
        }

        items(categories, key = { it.id }) { category ->
            val isSelected = selectedCategory?.id == category.id

            CategoryItem(
                name = category.name,
                icon = category.storedIcon.name,
                iconBackgroundColor = category.storedIcon.backgroundColor,
                border = CategoryItemDefaults.border(isSelected),
                modifier = Modifier
                    .fillMaxWidth(),
                trailingContent = { CategoryItemDefaults.SingleCheckedTrailing(isSelected) },
                onClick = {
                    onItemSelection?.invoke(category)
                }
            )
        }
    }
}

@Preview
@Composable
private fun CategorySelectionScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CategorySelectionScreen(
            categories = getRandomCategoryData(),
            selectedCategory = getRandomCategoryData().firstOrNull(),
            createNewCallback = {}
        )
    }
}
