package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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

/**
 * Same year-stepper affordance used inside [MonthPicker], pulled out on its own for
 * [com.naveenapps.expensemanager.core.model.BudgetPeriod.YEARLY] budgets, which only need a
 * year — not a month+year — selection.
 */
@Composable
fun YearPicker(
    currentYear: Int,
    confirmButtonCLicked: (Int) -> Unit,
    cancelClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                            confirmButtonCLicked(year)
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
fun YearPickerPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        YearPicker(
            modifier = Modifier
                .wrapContentSize()
                .padding(EmTheme.spacing.lg)
                .background(
                    color = EmTheme.colors.scheme.background,
                    shape = EmTheme.shapes.sm,
                ),
            currentYear = Clock.System.now().toYearInt(),
            confirmButtonCLicked = { _ -> },
            cancelClicked = {},
        )
    }
}
