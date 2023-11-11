package com.naveenapps.expensemanager.presentation.category.selection

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.presentation.category.list.CategoryCheckedItem
import com.naveenapps.expensemanager.presentation.category.list.getRandomCategoryData
import com.naveenapps.expensemanager.presentation.selection.SelectionTitle


@Composable
fun MultipleCategoriesSelectionScreen(
    onItemSelection: ((List<Category>, Boolean) -> Unit)? = null
) {

    val context = LocalContext.current

    val viewModel: CategorySelectionViewModel = hiltViewModel()
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
                    selectedCategories.size == categories.size
                )
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.category_selection_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        onClearChanges = viewModel::clearChanges,
        onItemSelection = viewModel::selectThisAccount
    )
}

@Composable
fun MultipleCategoriesSelectionScreen(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    selectedCategories: List<Category>,
    onApplyChanges: (() -> Unit),
    onClearChanges: (() -> Unit),
    onItemSelection: ((Category, Boolean) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SelectionTitle(stringResource(id = R.string.select_account))
        }
        items(categories) { account ->
            val isSelected = selectedCategories.fastAny { it.id == account.id }
            CategoryCheckedItem(
                modifier = Modifier
                    .clickable {
                        onItemSelection?.invoke(account, isSelected.not())
                    }
                    .padding(start = 16.dp, end = 16.dp),
                name = account.name,
                icon = account.iconName,
                iconBackgroundColor = account.iconBackgroundColor,
                isSelected = isSelected,
                onCheckedChange = {
                    onItemSelection?.invoke(account, it)
                }
            )
        }
        item {
            Column {
                Divider(modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                ) {
                    TextButton(onClick = onClearChanges) {
                        Text(text = stringResource(id = R.string.clear_all).uppercase())
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onApplyChanges) {
                        Text(text = stringResource(id = R.string.select).uppercase())
                    }
                }
                Spacer(modifier = Modifier.padding(16.dp))
            }
        }
    }
}


@Preview
@Composable
private fun MultipleCategoriesSelectionScreenPreview() {
    ExpenseManagerTheme {
        MultipleCategoriesSelectionScreen(
            categories = getRandomCategoryData(),
            selectedCategories = getRandomCategoryData(),
            onApplyChanges = {},
            onClearChanges = {},
            onItemSelection = null
        )
    }
}