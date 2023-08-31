package com.nkuppan.expensemanager.feature.category.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.core.model.UiState
import com.nkuppan.expensemanager.feature.category.R
import java.util.Date

@Composable
fun CategoryListScreen(
    categoryUiState: UiState<List<Category>>,
    modifier: Modifier = Modifier
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
                    items(categoryUiState.data) {
                        CategoryItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp),
                            name = it.name,
                            isFavorite = it.isFavorite,
                            categoryColor = it.backgroundColor
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
    isFavorite: Boolean,
    categoryColor: String,
    modifier: Modifier = Modifier
) {
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
                painter = painterResource(
                    id = if (isFavorite) {
                        R.drawable.ic_favorite
                    } else {
                        R.drawable.ic_favorite_border
                    }
                ),
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
        Image(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(
                id = if (isFavorite) {
                    R.drawable.ic_favorite
                } else {
                    R.drawable.ic_favorite_border
                }
            ),
            contentDescription = name
        )
    }
}

@Preview
@Composable
fun CategoryItemPreview() {
    MaterialTheme {
        CategoryItem(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            name = "Utilities",
            isFavorite = false,
            categoryColor = "#FFFFFF"
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
        isFavorite = false,
        backgroundColor = "#000000",
        createdOn = Date(),
        updatedOn = Date()
    ),
    Category(
        id = "2",
        name = "Category Two",
        type = CategoryType.EXPENSE,
        isFavorite = false,
        backgroundColor = "#FFFFFF",
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