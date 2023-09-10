package com.nkuppan.expensemanager.presentation.category.list

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.presentation.category.create.NavigationButton
import java.util.Date


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryListScreen(
    navController: NavController
) {
    val viewModel: CategoryListViewModel = hiltViewModel()
    val categoryUiState by viewModel.categories.collectAsState()
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
    ) { it ->
        CategoryListScreen(
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            categoryUiState = categoryUiState
        ) { categoryId ->
            navController.navigate("category/create?categoryId=${categoryId}")
        }
    }
}

@Composable
fun CategoryListScreen(
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
                                .padding(16.dp)
                                .clickable {
                                    onItemClick?.invoke(category.id)
                                },
                            name = category.name,
                            iconName = category.iconName,
                            categoryColor = category.backgroundColor
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun CategoryItem(
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

@SuppressLint("DiscouragedApi")
fun Context.getDrawable(iconName: String): Int {
    return runCatching {
        val resources = this.resources.getIdentifier(
            iconName, "drawable", this.packageName
        )

        if (resources > 0) resources else null
    }.getOrNull() ?: R.drawable.ic_calendar
}

@Preview
@Composable
fun CategoryItemPreview() {
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
fun CategoryListItemLoadingStatePreview() {
    MaterialTheme {
        CategoryListScreen(
            categoryUiState = UiState.Loading,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
fun CategoryListItemEmptyStatePreview() {
    MaterialTheme {
        CategoryListScreen(
            categoryUiState = UiState.Empty,
            modifier = Modifier.fillMaxSize()
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
fun CategoryListItemSuccessStatePreview() {
    MaterialTheme {
        CategoryListScreen(
            categoryUiState = UiState.Success(
                DUMMY_DATA
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}