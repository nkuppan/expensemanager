package com.naveenapps.expensemanager.feature.category.selection

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.MultiSelectionSheet
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.category.list.getRandomCategoryData
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MultipleCategoriesSelectionScreen(
    selectedCategories: List<Category>,
    onItemSelection: ((List<Category>, Boolean) -> Unit),
    viewModel: CategorySelectionViewModel = koinViewModel(),
    /** Closes the sheet without applying; shown as an × in the header when set. */
    onClose: (() -> Unit)? = null,
) {
    viewModel.selectAllThisCategory(selectedCategories)

    CategorySelectionView(viewModel, onItemSelection, onClose)
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun CategorySelectionView(
    viewModel: CategorySelectionViewModel,
    onItemSelection: ((List<Category>, Boolean) -> Unit)?,
    onClose: (() -> Unit)?,
) {
    val activity = LocalActivity.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()

    MultipleCategorySelectionViewContent(
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
                if (activity != null && !activity.isFinishing) {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.category_selection_message),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        },
        onSetSelection = viewModel::setSelection,
        onItemSelection = viewModel::selectThisCategory,
        onClose = onClose,
    )
}

@Composable
private fun MultipleCategorySelectionViewContent(
    categories: List<Category>,
    selectedCategories: List<Category>,
    onApplyChanges: (() -> Unit),
    onSetSelection: ((List<Category>, Boolean) -> Unit),
    modifier: Modifier = Modifier,
    onItemSelection: ((Category, Boolean) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    // Search only narrows what's listed; "Select all" acts on what's listed.
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
    val selectedIds = remember(selectedCategories) { selectedCategories.map { it.id }.toSet() }
    val selectedVisible = visibleCategories.count { it.id in selectedIds }
    val selectAllState = when {
        visibleCategories.isEmpty() || selectedVisible == 0 -> ToggleableState.Off
        selectedVisible == visibleCategories.size -> ToggleableState.On
        else -> ToggleableState.Indeterminate
    }

    MultiSelectionSheet(
        title = stringResource(id = R.string.select_category),
        searchPlaceholder = stringResource(id = R.string.search_categories),
        query = query,
        onQueryChange = { query = it },
        selectedText = pluralStringResource(
            id = R.plurals.items_selected,
            count = selectedCategories.size,
            selectedCategories.size,
        ),
        selectAllState = selectAllState,
        onSelectAllClick = { onSetSelection(visibleCategories, selectAllState != ToggleableState.On) },
        showNoResults = visibleCategories.isEmpty() && query.isNotBlank(),
        onDone = onApplyChanges,
        onClose = onClose,
        modifier = modifier,
    ) {
        items(visibleCategories, key = { it.id }) { category ->
            val isSelected = category.id in selectedIds
            CategoryItem(
                name = category.titleResId?.let { stringResource(it) } ?: category.name,
                icon = category.storedIcon.name,
                iconBackgroundColor = category.storedIcon.backgroundColor,
                customImagePath = category.storedIcon.customImagePath,
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
}

@Preview
@Composable
private fun MultipleCategoriesSelectionScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        MultipleCategorySelectionViewContent(
            categories = getRandomCategoryData(),
            selectedCategories = getRandomCategoryData(),
            onApplyChanges = {},
            onSetSelection = { _, _ -> },
            onItemSelection = null,
        )
    }
}
