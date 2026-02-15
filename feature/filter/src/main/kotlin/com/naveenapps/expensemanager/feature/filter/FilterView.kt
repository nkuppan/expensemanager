package com.naveenapps.expensemanager.feature.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterSelectionView
import com.naveenapps.expensemanager.feature.filter.type.FilterTypeSelectionView
import com.naveenapps.expensemanager.feature.filter.type.InputChipView
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterView(
    modifier: Modifier = Modifier,
    viewModel: FilterViewModel = koinViewModel()
) {
    val filterState by viewModel.filterState.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (filterState.showDateFilter) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.processAction(FilterAction.DismissDateFilter)
            },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            DateFilterSelectionView(
                onComplete = {
                    viewModel.processAction(FilterAction.DismissDateFilter)
                }
            )
        }
    }

    if (filterState.showTypeFilter) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.processAction(FilterAction.DismissTypeFilter)
            },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            FilterTypeSelectionView(
                applyChanges = {
                    viewModel.processAction(FilterAction.DismissTypeFilter)
                },
            )
        }
    }

    Column(modifier = modifier) {
        FilterContentView(
            modifier = modifier,
            filterState = filterState,
            onAction = viewModel::processAction,
        )
    }
}

@Composable
private fun FilterContentView(
    filterState: FilterState,
    onAction: (FilterAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Date navigation row — flat, no card background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onAction.invoke(FilterAction.MoveDateBackward) },
                enabled = filterState.showBackward,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Date label
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onAction.invoke(FilterAction.ShowDateFilter) }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = filterState.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = { onAction.invoke(FilterAction.MoveDateForward) },
                enabled = filterState.showForward,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Filter button with subtle badge
            val hasActiveFilters = filterState.selectedTransactionTypes.isNotEmpty()
                    || filterState.selectedAccounts.isNotEmpty()
                    || filterState.selectedCategories.isNotEmpty()

            IconButton(
                onClick = { onAction.invoke(FilterAction.ShowTypeFilter) },
                modifier = Modifier.size(36.dp),
            ) {
                BadgedBox(
                    badge = {
                        if (hasActiveFilters) {
                            val count = filterState.selectedTransactionTypes.size +
                                    filterState.selectedAccounts.size +
                                    filterState.selectedCategories.size
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        // Active filter chips
        TypeFilter(
            filterState = filterState,
            onAction = onAction,
        )
    }
}

@Composable
fun TypeFilter(
    filterState: FilterState,
    onAction: (FilterAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasFilters = filterState.selectedTransactionTypes.isNotEmpty()
            || filterState.selectedAccounts.isNotEmpty()
            || filterState.selectedCategories.isNotEmpty()

    AnimatedVisibility(
        visible = hasFilters,
        enter = fadeIn(tween(200)) + expandVertically(tween(200)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(150)),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            filterState.selectedTransactionTypes.forEach { type ->
                InputChipView(
                    label = type.toCapitalize(),
                    selected = true,
                ) {
                    onAction.invoke(FilterAction.RemoveTransactionType(type))
                }
            }
            filterState.selectedAccounts.forEach { account ->
                InputChipView(
                    label = account.name,
                    selected = true,
                    iconName = account.storedIcon.name,
                ) {
                    onAction.invoke(FilterAction.RemoveAccount(account))
                }
            }
            filterState.selectedCategories.forEach { category ->
                InputChipView(
                    label = category.name,
                    selected = true,
                    iconName = category.storedIcon.name,
                ) {
                    onAction.invoke(FilterAction.RemoveCategory(category))
                }
            }
        }
    }
}

@Preview
@Composable
fun FilterViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        Column {
            FilterContentView(
                filterState = FilterState(
                    date = "This Month (11/2023)",
                    showBackward = false,
                    showForward = false,
                    selectedTransactionTypes = emptyList(),
                    selectedAccounts = emptyList(),
                    selectedCategories = emptyList(),
                    showDateFilter = false,
                    showTypeFilter = false,
                    dateRangeType = DateRangeType.ALL,
                ),
                onAction = {},
                modifier = Modifier.fillMaxWidth(),
            )
            FilterContentView(
                filterState = FilterState(
                    date = "This Month (11/2023)",
                    showBackward = true,
                    showForward = true,
                    selectedTransactionTypes = listOf(
                        TransactionType.TRANSFER,
                        TransactionType.INCOME
                    ),
                    selectedAccounts = emptyList(),
                    selectedCategories = emptyList(),
                    showDateFilter = false,
                    showTypeFilter = false,
                    dateRangeType = DateRangeType.ALL,
                ),
                onAction = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}