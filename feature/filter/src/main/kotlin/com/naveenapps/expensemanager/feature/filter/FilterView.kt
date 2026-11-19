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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.em.EmBadge
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmModalSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.filter.datefilter.DateFilterSelectionView
import com.naveenapps.expensemanager.feature.filter.type.FilterTypeSelectionView
import com.naveenapps.expensemanager.feature.filter.type.InputChipView
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FilterView(
    modifier: Modifier = Modifier,
    viewModel: FilterViewModel = koinViewModel(),
) {
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

    if (filterState.showDateFilter) {
        EmModalSheet(
            onDismissRequest = {
                viewModel.processAction(FilterAction.DismissDateFilter)
            },
        ) {
            DateFilterSelectionView(
                onComplete = {
                    viewModel.processAction(FilterAction.DismissDateFilter)
                },
            )
        }
    }

    if (filterState.showTypeFilter) {
        EmModalSheet(
            onDismissRequest = {
                viewModel.processAction(FilterAction.DismissTypeFilter)
            },
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
                .padding(horizontal = EmTheme.spacing.sm)
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmIconButton(
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = stringResource(DesignSystemR.string.a11y_previous_period),
                onClick = { onAction.invoke(FilterAction.MoveDateBackward) },
                enabled = filterState.showBackward,
            )

            // Date label
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(EmTheme.shapes.sm)
                    .clickable { onAction.invoke(FilterAction.ShowDateFilter) }
                    .padding(vertical = EmTheme.spacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmIcon(
                    icon = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    size = EmTheme.icons.sm,
                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
                EmText(
                    text = filterState.date,
                    role = EmTextRole.SectionTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            EmIconButton(
                icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(DesignSystemR.string.a11y_next_period),
                onClick = { onAction.invoke(FilterAction.MoveDateForward) },
                enabled = filterState.showForward,
            )

            // Filter button with subtle badge
            val hasActiveFilters = filterState.selectedTransactionTypes.isNotEmpty() ||
                filterState.selectedAccounts.isNotEmpty() ||
                filterState.selectedCategories.isNotEmpty()

            val count = filterState.selectedTransactionTypes.size +
                filterState.selectedAccounts.size +
                filterState.selectedCategories.size
            EmIconButton(
                icon = Icons.Rounded.FilterList,
                contentDescription = stringResource(R.string.filters),
                onClick = { onAction.invoke(FilterAction.ShowTypeFilter) },
                badge = if (hasActiveFilters) EmBadge.Count(count) else null,
            )
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
    val hasFilters = filterState.selectedTransactionTypes.isNotEmpty() ||
        filterState.selectedAccounts.isNotEmpty() ||
        filterState.selectedCategories.isNotEmpty()

    AnimatedVisibility(
        visible = hasFilters,
        enter = fadeIn(tween(200)) + expandVertically(tween(200)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(150)),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
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
                    label = category.titleResId?.let { stringResource(it) } ?: category.name,
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
    ExpenseManagerPreviewTheme(padding = 0.dp) {
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
                        TransactionType.INCOME,
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
