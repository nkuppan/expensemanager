package com.naveenapps.expensemanager.feature.filter.datefilter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.filter.R
import org.koin.compose.viewmodel.koinViewModel
import java.util.Date

@Composable
fun DateFilterSelectionView(
    onComplete: () -> Unit,
    viewModel: DateFilterViewModel = koinViewModel()
) {

    ObserveAsEvents(viewModel.event) {
        when (it) {
            DateFilterEvent.Saved -> onComplete.invoke()
        }
    }

    val state by viewModel.state.collectAsState()

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
            .wrapContentSize()
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
        Text(
            text = stringResource(id = R.string.date_filter),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
        )

        // Filter options
        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(state.dateRangeTypeList) { index, filter ->
                val isSelected = state.dateRangeType.value == filter.type

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerLow,
                    animationSpec = tween(200),
                    label = "bg_color",
                )

                Surface(
                    onClick = {
                        state.dateRangeType.onValueChange?.invoke(filter.type)
                    },
                    shape = AppCardViewDefaults.cardShape(index, state.dateRangeTypeList),
                    color = bgColor,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Leading icon based on filter type
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(40.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = filter.type.toIcon(),
                                    contentDescription = null,
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = filter.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                            )
                            if (filter.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = filter.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
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
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer { alpha = checkAlpha },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp),
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 4.dp),
                )

                Text(
                    text = stringResource(R.string.custom).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onAction.invoke(DateFilterAction.Save) },
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onAction.invoke(DateFilterAction.Save) },
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.select),
                    fontWeight = FontWeight.SemiBold,
                )
            }
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
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
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
private fun DateRangeType.toIcon(): ImageVector {
    return when (this) {
        DateRangeType.TODAY -> Icons.Rounded.Today
        DateRangeType.THIS_WEEK -> Icons.Rounded.DateRange
        DateRangeType.THIS_MONTH -> Icons.Rounded.CalendarMonth
        DateRangeType.THIS_YEAR -> Icons.Rounded.CalendarToday
        DateRangeType.ALL -> Icons.Rounded.AllInclusive
        DateRangeType.CUSTOM -> Icons.Rounded.EditCalendar
    }
}

@Preview
@Composable
private fun FilterNormalViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        val dateRange = TextFieldValue(DateRangeType.THIS_MONTH, false, {})
        val dateFilter = TextFieldValue(Date(), false, {})
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
                        listOf(Date().time, Date().time),
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

