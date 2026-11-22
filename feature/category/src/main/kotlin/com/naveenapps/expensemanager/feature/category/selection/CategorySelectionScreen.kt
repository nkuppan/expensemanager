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
import com.naveenapps.expensemanager.core.common.utils.localizedCategoryName
import com.naveenapps.expensemanager.core.designsystem.em.EmNoSearchResults
import com.naveenapps.expensemanager.core.designsystem.em.EmSearchField
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.displayName
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
                val name = context.localizedCategoryName(category.defaultKey, category.name)
                name.contains(search, ignoreCase = true)
            }
        }
    }

    Column(modifier = modifier) {
        EmSectionHeader(
            title = stringResource(id = R.string.select_category),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
            onAddClick = createNewCallback,
        )

        EmSearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = stringResource(id = R.string.search_categories),
            modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
        )

        if (visibleCategories.isEmpty() && query.isNotBlank()) {
            EmNoSearchResults(modifier = Modifier.padding(horizontal = EmTheme.spacing.xl, vertical = EmTheme.spacing.xl))
        }

        // The header and search stay put while the list scrolls under them.
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(
                start = EmTheme.spacing.lg,
                end = EmTheme.spacing.lg,
                top = EmTheme.spacing.md,
                bottom = EmTheme.spacing.xxxl,
            ),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        ) {
            items(visibleCategories, key = { it.id }) { category ->
                val isSelected = selectedCategory?.id == category.id

                CategoryItem(
                    name = category.displayName(),
                    icon = category.storedIcon.name,
                    iconBackgroundColor = category.storedIcon.backgroundColor,
                    customImagePath = category.storedIcon.customImagePath,
                    selected = isSelected,
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
