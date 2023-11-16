package com.naveenapps.expensemanager.feature.category.list

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.feature.category.R
import java.util.Date


enum class CategoryTabItems(
    @StringRes val labelResourceID: Int,
    val categoryType: CategoryType,
    val index: Int
) {
    Expense(R.string.spending, CategoryType.EXPENSE, 0),
    Income(R.string.income, CategoryType.INCOME, 1),
}

@Composable
fun CategoryListScreen(viewModel: CategoryListViewModel = hiltViewModel()) {

    var state by remember { mutableStateOf(CategoryTabItems.Expense) }

    Scaffold(
        topBar = {
            TopNavigationBar(
                onClick = {
                    viewModel.closePage()
                },
                title = stringResource(R.string.category),
                disableBackIcon = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.openCreateScreen(null)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            PrimaryTabRow(selectedTabIndex = state.index) {
                CategoryTabItems.values().forEach { items ->
                    Tab(
                        selected = state.categoryType == items.categoryType,
                        onClick = {
                            state = items
                            viewModel.setCategoryType(items.categoryType)
                        },
                        text = {
                            Text(
                                text = stringResource(id = items.labelResourceID),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            CategoryListContent(viewModel)
        }
    }
}

@Composable
private fun CategoryListContent(viewModel: CategoryListViewModel) {

    val categoryUiState by viewModel.categories.collectAsState()

    CategoryListScreenContent(
        modifier = Modifier.fillMaxSize(),
        categoryUiState = categoryUiState
    ) { categoryId ->
        viewModel.openCreateScreen(categoryId)
    }
}

@Composable
private fun CategoryListScreenContent(
    categoryUiState: UiState<List<Category>>,
    modifier: Modifier = Modifier,
    onItemClick: ((String) -> Unit)? = null
) {
    Box(modifier = modifier) {

        when (categoryUiState) {
            UiState.Empty -> {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center),
                    text = stringResource(id = R.string.no_category_available),
                    textAlign = TextAlign.Center
                )
            }

            UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.Center)
                )
            }

            is UiState.Success -> {
                val items = categoryUiState.data

                Column {
                    items.forEach { category ->
                        CategoryItem(
                            name = category.name,
                            icon = category.iconName,
                            iconBackgroundColor = category.iconBackgroundColor,
                            modifier = Modifier
                                .clickable {
                                    onItemClick?.invoke(category.id)
                                }
                                .then(ItemSpecModifier),
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    endIcon: ImageVector? = null
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier.align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp, end = 16.dp),
            text = name
        )
        if (endIcon != null) {
            Icon(
                modifier = Modifier.align(Alignment.CenterVertically),
                imageVector = endIcon,
                contentDescription = null,
            )
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun CategoryCheckedItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier.align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp)
                .align(Alignment.CenterVertically),
            text = name,
            style = MaterialTheme.typography.bodyLarge,
        )
        Checkbox(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 8.dp),
            checked = isSelected,
            onCheckedChange = onCheckedChange
        )
    }
}

fun getCategoryData(index: Int): Category {
    return Category(
        id = "$index",
        name = "Category $index",
        type = CategoryType.EXPENSE,
        iconName = "ic_calendar",
        iconBackgroundColor = "#000000",
        createdOn = Date(),
        updatedOn = Date()
    )
}

fun getRandomCategoryData(): List<Category> {
    return buildList {
        repeat(15) {
            add(getCategoryData(it))
        }
    }
}

@com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
@Composable
private fun CategoryItemPreview() {
    ExpenseManagerTheme {
        Column {
            CategoryItem(
                name = "Utilities",
                icon = "ic_calendar",
                iconBackgroundColor = "#000000",
                endIcon = Icons.Default.KeyboardDoubleArrowRight,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.black_100))
                    .then(ItemSpecModifier)
            )
        }
    }
}

@Preview
@Composable
private fun CategoryListItemLoadingStatePreview() {
    ExpenseManagerTheme {
        CategoryListScreenContent(categoryUiState = UiState.Loading)
    }
}

@Preview
@Composable
private fun CategoryListItemEmptyStatePreview() {
    ExpenseManagerTheme {
        CategoryListScreenContent(categoryUiState = UiState.Empty)
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CategoryListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        CategoryListScreenContent(
            categoryUiState = UiState.Success(getRandomCategoryData()),
        )
    }
}