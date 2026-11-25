package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.naveenapps.expensemanager.core.common.utils.toMonth
import com.naveenapps.expensemanager.core.common.utils.toYearInt
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import kotlin.time.Clock

@Composable
fun MonthPicker(
    currentMonth: Int,
    currentYear: Int,
    confirmButtonCLicked: (Int, Int) -> Unit,
    cancelClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val months = listOf(
        "JAN",
        "FEB",
        "MAR",
        "APR",
        "MAY",
        "JUN",
        "JUL",
        "AUG",
        "SEP",
        "OCT",
        "NOV",
        "DEC",
    )

    var month by remember { mutableStateOf(months[if (currentMonth - 1 < 0) 0 else currentMonth - 1]) }

    var year by remember { mutableIntStateOf(currentYear) }

    val interactionSource = remember { MutableInteractionSource() }

    Dialog(
        content = {
            Column(modifier = modifier) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EmIcon(
                        icon = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier
                            .rotate(90f)
                            .clickable(
                                indication = null,
                                interactionSource = interactionSource,
                                onClick = {
                                    year--
                                },
                            ),
                        size = EmTheme.icons.tile,
                    )

                    EmText(
                        text = year.toString(),
                        modifier = Modifier.padding(horizontal = EmTheme.spacing.xl),
                        role = EmTextRole.Label,
                    )

                    EmIcon(
                        icon = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier
                            .rotate(-90f)
                            .clickable(
                                indication = null,
                                interactionSource = interactionSource,
                                onClick = {
                                    year++
                                },
                            ),
                        size = EmTheme.icons.tile,
                    )
                }
                LazyVerticalGrid(
                    modifier = Modifier
                        .padding(top = EmTheme.spacing.lg)
                        .fillMaxWidth(),
                    columns = GridCells.Adaptive(minSize = 96.dp),
                ) {
                    items(months, key = { it }) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = interactionSource,
                                    onClick = {
                                        month = it
                                    },
                                )
                                .background(
                                    color = Color.Transparent,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            val animatedSize by animateDpAsState(
                                targetValue = if (month == it) 60.dp else 0.dp,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = LinearOutSlowInEasing,
                                ),
                                label = "",
                            )

                            Box(
                                modifier = Modifier
                                    .size(animatedSize)
                                    .background(
                                        color = if (month == it) Color.Blue else Color.Transparent,
                                        shape = EmTheme.shapes.pill,
                                    ),
                            )

                            EmText(
                                text = it,
                                color = if (month == it) Color.White else Color.Black,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .height(60.dp)
                        .align(Alignment.End),
                ) {
                    EmButton(
                        text = stringResource(id = R.string.cancel),
                        onClick = {
                            cancelClicked()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = EmTheme.spacing.lg),
                        variant = EmButtonVariant.Tertiary,
                    )

                    EmButton(
                        text = stringResource(id = R.string.ok),
                        onClick = {
                            confirmButtonCLicked(
                                months.indexOf(month) + 1,
                                year,
                            )
                        },
                        modifier = Modifier.align(Alignment.CenterVertically),
                        variant = EmButtonVariant.Tertiary,
                    )
                }
            }
        },
        onDismissRequest = cancelClicked,
    )
}

@Composable
@EmPreviews
fun MonthPickerPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        MonthPicker(
            modifier = Modifier
                .wrapContentSize()
                .padding(EmTheme.spacing.lg)
                .background(
                    color = EmTheme.colors.scheme.background,
                    shape = EmTheme.shapes.sm,
                ),
            currentMonth = Clock.System.now().toMonth(),
            currentYear = Clock.System.now().toYearInt(),
            confirmButtonCLicked = { _, _ -> },
            cancelClicked = {},
        )
    }
}
