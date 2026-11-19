package com.naveenapps.expensemanager.feature.category.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmFab
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmTabs
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.category.selection.CategoryItem
import com.naveenapps.expensemanager.feature.category.selection.CategoryItemDefaults
import java.util.Date
import org.koin.compose.viewmodel.koinViewModel

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
    val state by viewModel.state.collectAsStateWithLifecycle()

    CategoryListScreenContentView(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun CategoryListScreenContentView(
    state: CategoryListState,
    onAction: (CategoryListAction) -> Unit,
) {
    EmScaffold(
        topBar = {
            Column {
                EmTopBar(
                    title = stringResource(R.string.category),
                    navigation = EmTopBarNavigation.Back,
                    onNavigationClick = { onAction.invoke(CategoryListAction.ClosePage) },
                )
                EmTabs(
                    tabs = state.tabs.map { stringResource(id = it.labelResourceID) },
                    selectedIndex = state.selectedTab.index,
                    onSelect = { onAction.invoke(CategoryListAction.ChangeCategory(state.tabs[it])) },
                )
            }
        },
        floatingActionButton = {
            EmFab(
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_category),
                onClick = { onAction.invoke(CategoryListAction.Create) },
                label = stringResource(R.string.add_category),
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
                EmEmptyState(
                    illustration = painterResource(id = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_category),
                    title = stringResource(id = R.string.no_category_available),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                )
            } else {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = EmTheme.spacing.xl, vertical = EmTheme.spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
                    ) {
                        EmIcon(
                            icon = Icons.Rounded.Info,
                            contentDescription = null,
                            size = EmTheme.icons.sm,
                            tint = EmTheme.colors.scheme.onSurfaceVariant,
                        )
                        EmText(
                            text = pluralStringResource(
                                id = R.plurals.category_count,
                                count = state.filteredCategories.size,
                                state.filteredCategories.size,
                            ),
                            role = EmTextRole.Supporting,
                            color = EmTextColor.Supporting,
                        )
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = EmTheme.spacing.lg,
                            end = EmTheme.spacing.lg,
                            bottom = 78.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs),
                    ) {
                        val categories = state.filteredCategories

                        itemsIndexed(
                            items = categories,
                            key = { _, item -> item.id },
                        ) { index, category ->
                            CategoryItem(
                                name = category.titleResId?.let { stringResource(it) }
                                    ?: category.name,
                                icon = category.storedIcon.name,
                                iconBackgroundColor = category.storedIcon.backgroundColor,
                                customImagePath = category.storedIcon.customImagePath,
                                position = EmCardPosition.of(index, categories),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                onClick = { onItemClick.invoke(category) },
                                trailingContent = {
                                    CategoryItemDefaults.ChevronTrailing()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryData(
    index: Int,
    categoryType: CategoryType,
): Category = Category(
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

fun getRandomCategoryData(totalCount: Int = 10): List<Category> = buildList {
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

@EmPreviews
@Composable
private fun CategoryItemPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        Column {
            CategoryItem(
                name = "Utilities",
                icon = "account_balance_wallet",
                iconBackgroundColor = "#000000",
                modifier = Modifier
                    .fillMaxWidth()
                    .then(ItemSpecModifier),
                trailingContent = {
                    CategoryItemDefaults.ChevronTrailing()
                },
            )
        }
    }
}

@EmPreviews
@Composable
private fun CategoryListItemEmptyStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CategoryListScreenContentView(
            state = CategoryListState(
                categories = emptyList(),
                filteredCategories = emptyList(),
                selectedTab = CategoryTabItems.Expense,
                tabs = CategoryTabItems.entries,
            ),
            onAction = {},
        )
    }
}

@EmPreviews
@Composable
private fun CategoryListItemSuccessStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CategoryListScreenContentView(
            state = CategoryListState(
                categories = emptyList(),
                filteredCategories = getRandomCategoryData(),
                selectedTab = CategoryTabItems.Expense,
                tabs = CategoryTabItems.entries,
            ),
            onAction = {},
        )
    }
}
