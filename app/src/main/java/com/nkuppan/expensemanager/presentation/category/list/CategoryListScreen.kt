package com.nkuppan.expensemanager.presentation.category.list

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.ui.components.IconAndBackgroundView
import com.nkuppan.expensemanager.ui.components.TopNavigationBar
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.utils.ItemSpecModifier
import com.nkuppan.expensemanager.utils.AppPreviewsLightAndDarkMode
import java.util.Date


@Composable
fun CategoryListScreen(
    navController: NavController
) {
    val viewModel: CategoryListViewModel = hiltViewModel()
    val categoryUiState by viewModel.categories.collectAsState()
    CategoryListScreenScaffoldView(navController, categoryUiState)
}

@Composable
private fun CategoryListScreenScaffoldView(
    navController: NavController,
    categoryUiState: UiState<List<Category>>
) {
    Scaffold(
        topBar = {
            TopNavigationBar(
                navController = navController,
                title = stringResource(R.string.category),
                disableBackIcon = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("category/create")
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->
        CategoryListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            categoryUiState = categoryUiState
        ) { categoryId ->
            navController.navigate("category/create?categoryId=${categoryId}")
        }
    }
}

@Composable
private fun CategoryListScreenContent(
    categoryUiState: UiState<List<Category>>,
    modifier: Modifier = Modifier,
    onItemClick: ((String) -> Unit)? = null
) {

    val scrollState = rememberLazyListState()

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

                LazyColumn(state = scrollState) {
                    items(categoryUiState.data) { category ->
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
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp)
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
    @DrawableRes endIcon: Int? = null
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
                painter = painterResource(id = endIcon),
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

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryItemPreview() {
    ExpenseManagerTheme {
        Column {

            CategoryItem(
                name = "Utilities",
                icon = "ic_calendar",
                iconBackgroundColor = "#000000",
                endIcon = R.drawable.ic_arrow_right,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(id = R.color.black_100))
                    .then(ItemSpecModifier)
            )
        }
    }
}

@Preview
@Composable
private fun CategoryListItemLoadingStatePreview() {
    ExpenseManagerTheme {
        CategoryListScreenScaffoldView(
            rememberNavController(),
            categoryUiState = UiState.Loading,
        )
    }
}

@Preview
@Composable
private fun CategoryListItemEmptyStatePreview() {
    ExpenseManagerTheme {
        CategoryListScreenScaffoldView(
            rememberNavController(),
            categoryUiState = UiState.Empty,
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun CategoryListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        CategoryListScreenScaffoldView(
            rememberNavController(),
            categoryUiState = UiState.Success(getRandomCategoryData()),
        )
    }
}