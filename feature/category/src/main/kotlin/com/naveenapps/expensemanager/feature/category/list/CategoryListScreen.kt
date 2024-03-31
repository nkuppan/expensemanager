package com.naveenapps.expensemanager.feature.category.list

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.components.LoadingItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.feature.category.R
import java.util.Date
import java.util.Random

enum class CategoryTabItems(
    @StringRes val labelResourceID: Int,
    val categoryType: CategoryType,
    val index: Int,
) {
    Expense(R.string.expense, CategoryType.EXPENSE, 0),
    Income(R.string.income, CategoryType.INCOME, 1),
}

@Composable
fun CategoryListScreen(viewModel: CategoryListViewModel = hiltViewModel()) {

    val categoryUiState by viewModel.categories.collectAsState()

    CategoryListScreenContentView(
        categoryUiState = categoryUiState,
        setCategoryType = viewModel::setCategoryType,
        openCategoryCreate = viewModel::openCreateScreen,
        closePage = viewModel::closePage,
    )
}

@Composable
private fun CategoryListScreenContentView(
    categoryUiState: UiState<List<Category>>,
    setCategoryType: (CategoryType) -> Unit,
    openCategoryCreate: (String?) -> Unit,
    closePage: () -> Unit,
) {

    var state by remember { mutableStateOf(CategoryTabItems.Expense) }

    Scaffold(
        topBar = {
            TopNavigationBar(
                onClick = closePage,
                title = stringResource(R.string.category),
                disableBackIcon = false,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    openCategoryCreate.invoke(null)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            TabRow(selectedTabIndex = state.index) {
                CategoryTabItems.entries.forEach { items ->
                    Tab(
                        selected = state.categoryType == items.categoryType,
                        onClick = {
                            state = items
                            setCategoryType.invoke(items.categoryType)
                        },
                        text = {
                            Text(
                                text = stringResource(id = items.labelResourceID).uppercase(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }

            CategoryListScreenContent(
                modifier = Modifier.fillMaxSize(),
                categoryUiState = categoryUiState,
                onItemClick = openCategoryCreate,
            )
        }
    }
}

@Composable
private fun CategoryListScreenContent(
    categoryUiState: UiState<List<Category>>,
    onItemClick: ((String) -> Unit),
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (categoryUiState) {
            UiState.Empty -> {
                EmptyItem(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center),
                    emptyItemText = stringResource(id = R.string.no_category_available),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_category,
                )
            }

            UiState.Loading -> {
                LoadingItem()
            }

            is UiState.Success -> {
                val items = categoryUiState.data

                LazyColumn {
                    items(items, key = { it.id }) { category ->
                        CategoryItem(
                            name = category.name,
                            icon = category.storedIcon.name,
                            iconBackgroundColor = category.storedIcon.backgroundColor,
                            modifier = Modifier
                                .clickable {
                                    onItemClick.invoke(category.id)
                                }
                                .then(ItemSpecModifier),
                        )
                    }
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                        )
                    }
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
    endIcon: ImageVector? = null,
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier.align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp, end = 16.dp),
            text = name,
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
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier.align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
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
            onCheckedChange = onCheckedChange,
        )
    }
}

fun getCategoryData(
    index: Int,
    categoryType: CategoryType,
): Category {
    return Category(
        id = "$index",
        name = "Category $index",
        type = categoryType,
        storedIcon = StoredIcon(
            "account_balance",
            "#000000",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    )
}

fun getRandomCategoryData(totalCount: Int = 10): List<Category> {
    return buildList {
        val random = Random()

        repeat(totalCount) {
            val isEven = random.nextInt() % 2 == 0
            add(
                getCategoryData(
                    it,
                    if (isEven) {
                        CategoryType.EXPENSE
                    } else {
                        CategoryType.INCOME
                    },
                ),
            )
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryItemPreview() {
    ExpenseManagerTheme {
        Column {
            CategoryItem(
                name = "Utilities",
                icon = "account_balance_wallet",
                iconBackgroundColor = "#000000",
                endIcon = Icons.Default.KeyboardDoubleArrowRight,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(id = com.naveenapps.expensemanager.core.common.R.color.black_100))
                    .then(ItemSpecModifier),
            )
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryListItemLoadingStatePreview() {
    ExpenseManagerTheme {
        CategoryListScreenContentView(
            categoryUiState = UiState.Loading,
            setCategoryType = {},
            openCategoryCreate = {},
            closePage = {},
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryListItemEmptyStatePreview() {
    ExpenseManagerTheme {
        CategoryListScreenContentView(
            categoryUiState = UiState.Empty,
            setCategoryType = {},
            openCategoryCreate = {},
            closePage = {},
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        CategoryListScreenContentView(
            categoryUiState = UiState.Success(getRandomCategoryData()),
            setCategoryType = {},
            openCategoryCreate = {},
            closePage = {},
        )
    }
}
