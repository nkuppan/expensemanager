package com.naveenapps.expensemanager.feature.category.selection

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.category.list.getRandomCategoryData
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MultipleCategoriesSelectionScreen(
    viewModel: CategorySelectionViewModel = koinViewModel(),
    selectedCategories: List<Category> = emptyList(),
    onItemSelection: ((List<Category>, Boolean) -> Unit)? = null,
) {
    viewModel.selectAllThisCategory(selectedCategories)

    CategorySelectionView(viewModel, onItemSelection)
}

@Composable
private fun CategorySelectionView(
    viewModel: CategorySelectionViewModel,
    onItemSelection: ((List<Category>, Boolean) -> Unit)?,
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()

    MultipleCategoriesSelectionScreen(
        modifier = Modifier,
        categories = categories,
        selectedCategories = selectedCategories,
        onApplyChanges = {
            if (selectedCategories.isNotEmpty()) {
                onItemSelection?.invoke(
                    selectedCategories,
                    selectedCategories.size == categories.size,
                )
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.category_selection_message),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        },
        onClearChanges = viewModel::clearChanges,
        onItemSelection = viewModel::selectThisCategory,
    )
}

@Composable
fun MultipleCategoriesSelectionScreen(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    selectedCategories: List<Category>,
    onApplyChanges: (() -> Unit),
    onClearChanges: (() -> Unit),
    onItemSelection: ((Category, Boolean) -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.select_category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (selectedCategories.isNotEmpty()) {
                Text(
                    text = pluralStringResource(
                        id = R.plurals.items_selected,
                        count = selectedCategories.size,
                        selectedCategories.size,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // Category list
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories, key = { it.id }) { category ->
                val isSelected = selectedCategories.fastAny { it.id == category.id }
                CategoryItem(
                    name = category.name,
                    icon = category.storedIcon.name,
                    iconBackgroundColor = category.storedIcon.backgroundColor,
                    border = CategoryItemDefaults.border(isSelected),
                    onClick = {
                        onItemSelection?.invoke(category, isSelected.not())
                    },
                    trailingContent = {
                        CategoryItemDefaults.MultiCheckedTrailing(isSelected)
                    },
                )
            }
        }

        // Bottom actions
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onClearChanges,
                enabled = selectedCategories.isNotEmpty(),
            ) {
                Text(
                    text = stringResource(id = R.string.clear_all),
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onApplyChanges) {
                Text(
                    text = stringResource(id = com.naveenapps.expensemanager.feature.filter.R.string.apply),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MultipleCategoriesSelectionScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        MultipleCategoriesSelectionScreen(
            categories = getRandomCategoryData(),
            selectedCategories = getRandomCategoryData(),
            onApplyChanges = {},
            onClearChanges = {},
            onItemSelection = null,
        )
    }
}
