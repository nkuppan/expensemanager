package com.naveenapps.expensemanager.feature.category.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.components.SelectionHeader
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.NoSearchResults
import com.naveenapps.expensemanager.core.designsystem.ui.components.SheetSearchField
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
    // Same search as the budget screen's category sheet: matches the name shown, so built-in
    // categories are found by their translated name. Cleared each time the sheet opens.
    var query by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val visibleCategories = remember(categories, query) {
        val search = query.trim()
        if (search.isEmpty()) {
            categories
        } else {
            categories.filter { category ->
                val name = category.titleResId?.let { context.getString(it) } ?: category.name
                name.contains(search, ignoreCase = true)
            }
        }
    }

    Column(modifier = modifier) {
        SelectionHeader(
            title = stringResource(id = R.string.select_category),
            createNewCallback = createNewCallback,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        SheetSearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = stringResource(id = R.string.search_categories),
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (visibleCategories.isEmpty() && query.isNotBlank()) {
            NoSearchResults(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp))
        }

        // The header and search stay put while the list scrolls under them.
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(visibleCategories, key = { it.id }) { category ->
                val isSelected = selectedCategory?.id == category.id

                CategoryItem(
                    name = category.titleResId?.let { stringResource(it) } ?: category.name,
                    icon = category.storedIcon.name,
                    iconBackgroundColor = category.storedIcon.backgroundColor,
                    customImagePath = category.storedIcon.customImagePath,
                    border = CategoryItemDefaults.border(isSelected),
                    modifier = Modifier
                        .fillMaxWidth(),
                    trailingContent = { CategoryItemDefaults.SingleCheckedTrailing(isSelected) },
                    onClick = {
                        onItemSelection?.invoke(category)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun CategorySelectionScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CategorySelectionScreen(
            categories = getRandomCategoryData(),
            selectedCategory = getRandomCategoryData().firstOrNull(),
            createNewCallback = {},
        )
    }
}
