package com.nkuppan.expensemanager.presentation.category.list

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.getDrawable
import com.nkuppan.expensemanager.core.ui.theme.NavigationButton
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.UiState
import java.util.Date


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryListScreen(
    navController: NavController
) {
    val viewModel: CategoryListViewModel = hiltViewModel()
    val categoryUiState by viewModel.categories.collectAsState()
    CategoryListScreenScaffoldView(navController, categoryUiState)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryListScreenScaffoldView(
    navController: NavController,
    categoryUiState: UiState<List<Category>>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    NavigationButton(navController)
                },
                title = {
                    Text(text = stringResource(R.string.category))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("category/create")
            }) {
                Image(
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemClick?.invoke(category.id)
                                }
                                .padding(16.dp),
                            name = category.name,
                            iconName = category.iconName,
                            categoryColor = category.backgroundColor
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

@SuppressLint("DiscouragedApi")
@Composable
private fun CategoryItem(
    name: String,
    iconName: String,
    categoryColor: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(36.dp)
                .align(Alignment.CenterVertically),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                onDraw = {
                    drawCircle(color = Color(android.graphics.Color.parseColor(categoryColor)))
                }
            )
            Image(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center),
                painter = painterResource(id = context.getDrawable(iconName)),
                colorFilter = ColorFilter.tint(color = Color.White),
                contentDescription = name
            )
        }
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = name
        )
    }
}

val DUMMY_DATA = listOf(
    Category(
        id = "1",
        name = "Category One",
        type = CategoryType.EXPENSE,
        iconName = "ic_calendar",
        backgroundColor = "#000000",
        createdOn = Date(),
        updatedOn = Date()
    ),
    Category(
        id = "2",
        name = "Category Two",
        type = CategoryType.EXPENSE,
        iconName = "ic_calendar",
        backgroundColor = "#000000",
        createdOn = Date(),
        updatedOn = Date()
    ),
)

@Preview
@Composable
private fun CategoryItemPreview() {
    MaterialTheme {
        CategoryItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            name = "Utilities",
            iconName = "ic_calendar",
            categoryColor = "#000000"
        )
    }
}

@Preview
@Composable
private fun CategoryListItemLoadingStatePreview() {
    MaterialTheme {
        CategoryListScreenScaffoldView(
            rememberNavController(),
            categoryUiState = UiState.Loading,
        )
    }
}

@Preview
@Composable
private fun CategoryListItemEmptyStatePreview() {
    MaterialTheme {
        CategoryListScreenScaffoldView(
            rememberNavController(),
            categoryUiState = UiState.Empty,
        )
    }
}

@Preview
@Composable
private fun CategoryListItemSuccessStatePreview() {
    MaterialTheme {
        CategoryListScreenScaffoldView(
            rememberNavController(),
            categoryUiState = UiState.Success(
                DUMMY_DATA
            ),
        )
    }
}