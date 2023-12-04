package com.naveenapps.expensemanager.feature.category.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.SelectionTitle
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getSelectedBGColor
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.category.list.CategoryItem
import com.naveenapps.expensemanager.feature.category.list.getRandomCategoryData

@Composable
fun CategorySelectionScreen(
    modifier: Modifier = Modifier,
    categories: List<Category> = emptyList(),
    selectedCategory: Category? = null,
    createNewCallback: (() -> Unit)? = null,
    onItemSelection: ((Category) -> Unit)? = null,
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
            ) {
                SelectionTitle(
                    title = stringResource(id = R.string.select_category),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clickable {
                            createNewCallback?.invoke()
                        },
                    text = stringResource(id = R.string.add_new).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
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
                                    color = getSelectedBGColor(),
                                    shape = RoundedCornerShape(size = 12.dp),
                                )
                        } else {
                            Modifier.padding(4.dp)
                        },
                    )
                    .padding(12.dp),
            ) {
                CategoryItem(
                    modifier = Modifier,
                    name = category.name,
                    icon = category.storedIcon.name,
                    iconBackgroundColor = category.storedIcon.backgroundColor,
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview
@Composable
private fun CategorySelectionScreenPreview() {
    ExpenseManagerTheme {
        CategorySelectionScreen(
            categories = getRandomCategoryData(),
            selectedCategory = getRandomCategoryData().firstOrNull(),
        )
    }
}
