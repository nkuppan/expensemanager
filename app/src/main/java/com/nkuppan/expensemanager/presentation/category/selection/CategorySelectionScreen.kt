package com.nkuppan.expensemanager.presentation.category.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.presentation.category.list.CategoryItem
import com.nkuppan.expensemanager.presentation.category.list.DUMMY_DATA
import com.nkuppan.expensemanager.presentation.selection.SelectionTitle

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
            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onItemSelection?.invoke(category)
                }) {
                CategoryItem(
                    modifier = Modifier
                        .clickable {
                            onItemSelection?.invoke(category)
                        }
                        .padding(16.dp),
                    name = category.name,
                    iconName = category.iconName,
                    categoryColor = category.iconBackgroundColor,
                    endIcon = if (selectedCategory?.id == category.id) {
                        R.drawable.ic_done
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
    MaterialTheme {
        CategorySelectionScreen(
            categories = DUMMY_DATA,
            selectedCategory = DUMMY_DATA.firstOrNull()
        )
    }
}