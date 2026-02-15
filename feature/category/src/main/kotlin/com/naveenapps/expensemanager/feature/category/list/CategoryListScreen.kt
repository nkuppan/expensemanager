package com.naveenapps.expensemanager.feature.category.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.feature.category.R
import org.koin.compose.viewmodel.koinViewModel
import java.util.Date

enum class CategoryTabItems(
    val labelResourceID: Int,
    val categoryType: CategoryType,
    val index: Int,
) {
    Expense(R.string.expense, CategoryType.EXPENSE, 0),
    Income(R.string.income, CategoryType.INCOME, 1),
}

@Composable
fun CategoryListScreen(viewModel: CategoryListViewModel = koinViewModel()) {

    val state by viewModel.state.collectAsState()

    CategoryListScreenContentView(
        state = state,
        onAction = viewModel::processAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryListScreenContentView(
    state: CategoryListState,
    onAction: (CategoryListAction) -> Unit,
) {
    Scaffold(
        topBar = {
            Surface {
                Column {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    onAction.invoke(CategoryListAction.ClosePage)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        title = {
                            Text(stringResource(R.string.category))
                        },
                    )
                    PrimaryTabRow(selectedTabIndex = state.selectedTab.index) {
                        state.tabs.forEach { item ->
                            Tab(
                                selected = state.selectedTab.categoryType == item.categoryType,
                                onClick = {
                                    onAction.invoke(
                                        CategoryListAction.ChangeCategory(item)
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = item.labelResourceID).uppercase(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (state.selectedTab.categoryType == item.categoryType)
                                            FontWeight.Bold else FontWeight.Normal,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction.invoke(CategoryListAction.Create) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(text = stringResource(R.string.add_category))
                },
            )
        },
    ) { innerPadding ->
        CategoryListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            state = state,
            onItemClick = {
                onAction.invoke(CategoryListAction.Edit(it))
            },
        )
    }
}

@Composable
private fun CategoryListScreenContent(
    state: CategoryListState,
    onItemClick: ((Category) -> Unit),
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = state.filteredCategories.isEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "category_list_transition",
        ) { isEmpty ->
            if (isEmpty) {
                EmptyItem(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    emptyItemText = stringResource(id = R.string.no_category_available),
                    icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_category,
                )
            } else {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = pluralStringResource(
                                id = R.plurals.category_count,
                                count = state.filteredCategories.size,
                                state.filteredCategories.size,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 88.dp, // room for FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        itemsIndexed(
                            items = state.filteredCategories,
                            key = { _, item -> item.id },
                        ) { index, category ->
                            CategoryItem(
                                name = category.name,
                                icon = category.storedIcon.name,
                                iconBackgroundColor = category.storedIcon.backgroundColor,
                                shape = when {
                                    state.filteredCategories.size == 1 -> MaterialTheme.shapes.large
                                    index == 0 -> RoundedCornerShape(
                                        topStart = 16.dp, topEnd = 16.dp,
                                        bottomStart = 4.dp, bottomEnd = 4.dp,
                                    )

                                    index == state.filteredCategories.lastIndex -> RoundedCornerShape(
                                        topStart = 4.dp, topEnd = 4.dp,
                                        bottomStart = 16.dp, bottomEnd = 16.dp,
                                    )

                                    else -> RoundedCornerShape(4.dp)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { onItemClick.invoke(category) }
                                    .animateItem(),
                            )
                        }
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
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    endIcon: ImageVector? = null,
) {
    AppCardView(
        shape = shape,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAndBackgroundView(
                icon = icon,
                iconBackgroundColor = iconBackgroundColor,
                name = name,
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp),
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (endIcon != null) {
                Icon(
                    imageVector = endIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                // Subtle edit indicator
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun CategoryCheckedItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (isSelected) 3.dp else 1.dp,
        color = if (isSelected)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAndBackgroundView(
                icon = icon,
                iconBackgroundColor = iconBackgroundColor,
                name = name,
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp),
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            )
            Checkbox(
                modifier = Modifier.padding(start = 8.dp),
                checked = isSelected,
                onCheckedChange = onCheckedChange,
            )
        }
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
        repeat(totalCount) {
            val isEven = (it + 1) % 2 == 0
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
    NaveenAppsPreviewTheme(padding = 0.dp) {
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
private fun CategoryListItemEmptyStatePreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CategoryListScreenContentView(
            state = CategoryListState(
                categories = emptyList(),
                filteredCategories = emptyList(),
                selectedTab = CategoryTabItems.Expense,
                tabs = CategoryTabItems.entries
            ),
            onAction = {},
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryListItemSuccessStatePreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CategoryListScreenContentView(
            state = CategoryListState(
                categories = emptyList(),
                filteredCategories = getRandomCategoryData(),
                selectedTab = CategoryTabItems.Expense,
                tabs = CategoryTabItems.entries
            ),
            onAction = {},
        )
    }
}
