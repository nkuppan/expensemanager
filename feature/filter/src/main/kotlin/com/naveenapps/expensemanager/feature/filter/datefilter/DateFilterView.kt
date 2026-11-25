package com.naveenapps.expensemanager.feature.filter.datefilter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconTile
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.emGroupedShape
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.filter.R
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DateFilterSelectionView(
    onComplete: () -> Unit,
    viewModel: DateFilterViewModel = koinViewModel(),
) {
    ObserveAsEvents(viewModel.event) {
        when (it) {
            DateFilterEvent.Saved -> onComplete.invoke()
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.showDateFilter) {
        AppDatePickerDialog(
            selectedDate = if (state.dateFilterType == DateFilterType.FROM_DATE) {
                state.fromDate.value
            } else {
                state.toDate.value
            },
            onDateSelected = { date ->
                if (state.dateFilterType == DateFilterType.FROM_DATE) {
                    viewModel.processAction(DateFilterAction.SaveFromDate(date))
                } else {
                    viewModel.processAction(DateFilterAction.SaveToDate(date))
                }
            },
        ) {
            viewModel.processAction(DateFilterAction.DismissDateSelection)
        }
    }

    FilterTypesAndViewContent(
        state = state,
        onAction = viewModel::processAction,
        modifier = Modifier
            .wrapContentSize(),
    )
}

@Composable
private fun FilterTypesAndViewContent(
    state: DateFilterState,
    onAction: (DateFilterAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        EmText(
            text = stringResource(id = R.string.date_filter),
            modifier = Modifier.padding(
                start = EmTheme.spacing.xl,
                end = EmTheme.spacing.xl,
                top = EmTheme.spacing.sm,
                bottom = EmTheme.spacing.md,
            ),
            role = EmTextRole.SectionTitle,
        )

        // Filter options
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(horizontal = EmTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs),
        ) {
            itemsIndexed(state.dateRangeTypeList) { index, filter ->
                val isSelected = state.dateRangeType.value == filter.type

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        EmTheme.colors.scheme.primaryContainer
                    } else {
                        EmTheme.colors.scheme.surfaceContainerLow
                    },
                    animationSpec = tween(200),
                    label = "bg_color",
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(emGroupedShape(index, state.dateRangeTypeList.size))
                        .background(bgColor)
                        .selectable(selected = isSelected, role = Role.RadioButton) {
                            state.dateRangeType.onValueChange?.invoke(filter.type)
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
                    ) {
                        // Leading icon based on filter type
                        EmIconTile(
                            icon = filter.type.toIcon(),
                            color = if (isSelected) {
                                EmTheme.colors.scheme.primary
                            } else {
                                EmTheme.colors.scheme.onSurfaceVariant
                            },
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            EmText(
                                text = filter.name,
                                role = EmTextRole.ItemTitle,
                                color = if (isSelected) {
                                    EmTheme.colors.scheme.onPrimaryContainer
                                } else {
                                    EmTheme.colors.scheme.onSurface
                                },
                            )
                            if (filter.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(EmTheme.spacing.xxs))
                                EmText(
                                    text = filter.description,
                                    role = EmTextRole.Supporting,
                                    color = if (isSelected) {
                                        EmTheme.colors.scheme.onPrimaryContainer
                                    } else {
                                        EmTheme.colors.scheme.onSurfaceVariant
                                    },
                                )
                            }
                        }

                        // Check indicator
                        val checkAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = tween(200),
                            label = "check",
                        )

                        if (checkAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .size(EmTheme.icons.lg)
                                    .graphicsLayer { alpha = checkAlpha }
                                    .clip(EmTheme.shapes.pill)
                                    .background(EmTheme.colors.scheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    EmIcon(
                                        icon = Icons.Rounded.Check,
                                        contentDescription = null,
                                        size = EmTheme.icons.sm,
                                        tint = EmTheme.colors.scheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom range section
        AnimatedVisibility(
            visible = state.showCustomRangeSelection,
            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                EmDivider(modifier = Modifier.padding(bottom = EmTheme.spacing.xs))

                EmText(
                    text = stringResource(R.string.custom),
                    modifier = Modifier.padding(start = EmTheme.spacing.xs),
                    role = EmTextRole.Overline,
                    color = EmTextColor.Supporting,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
                ) {
                    DatePickerField(
                        label = stringResource(R.string.from_date),
                        value = state.fromDate.value.toCompleteDateWithDate(),
                        onClick = { onAction.invoke(DateFilterAction.ShowFromDateSelection) },
                        modifier = Modifier.weight(1f),
                    )
                    DatePickerField(
                        label = stringResource(R.string.to_date),
                        value = state.toDate.value.toCompleteDateWithDate(),
                        onClick = { onAction.invoke(DateFilterAction.ShowToDateSelection) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Action buttons
        EmDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmButton(
                text = stringResource(id = R.string.cancel),
                onClick = { onAction.invoke(DateFilterAction.Cancel) },
                variant = EmButtonVariant.Tertiary,
            )
            Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
            EmButton(
                text = stringResource(id = R.string.select),
                onClick = { onAction.invoke(DateFilterAction.Save) },
            )
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(EmTheme.shapes.md)
            .background(EmTheme.colors.scheme.surfaceContainerLow)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = EmTheme.spacing.md, vertical = EmTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        ) {
            EmIcon(
                icon = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                size = EmTheme.icons.md,
                tint = EmTheme.colors.scheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                EmText(
                    text = label,
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
                Spacer(modifier = Modifier.height(EmTheme.spacing.xxs))
                EmText(
                    text = value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Map DateRangeType to a relevant icon.
 * Adjust the mapping to match your actual DateRangeType enum values.
 */
private fun DateRangeType.toIcon(): ImageVector = when (this) {
    DateRangeType.TODAY -> Icons.Rounded.Today
    DateRangeType.THIS_WEEK -> Icons.Rounded.DateRange
    DateRangeType.THIS_MONTH -> Icons.Rounded.CalendarMonth
    DateRangeType.THIS_YEAR -> Icons.Rounded.CalendarToday
    DateRangeType.ALL -> Icons.Rounded.AllInclusive
    DateRangeType.CUSTOM -> Icons.Rounded.EditCalendar
}

@Preview
@Composable
private fun FilterNormalViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        val dateRange = TextFieldValue(DateRangeType.THIS_MONTH, false, {})
        val dateFilter = TextFieldValue(Clock.System.now(), false, {})
        FilterTypesAndViewContent(
            state = DateFilterState(
                dateFilterType = DateFilterType.FROM_DATE,
                showCustomRangeSelection = true,
                showDateFilter = false,
                dateRangeTypeList = DateRangeType.entries.map {
                    DateRangeModel(
                        name = it.toCapitalize(),
                        description = "Sample",
                        type = it,
                        listOf(Clock.System.now().toEpochMilliseconds(), Clock.System.now().toEpochMilliseconds()),
                    )
                },
                dateRangeType = dateRange,
                fromDate = dateFilter,
                toDate = dateFilter,
            ),
            onAction = {},
        )
    }
}
