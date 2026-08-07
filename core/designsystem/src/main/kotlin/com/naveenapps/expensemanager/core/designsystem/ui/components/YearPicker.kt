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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.toYearInt
import com.naveenapps.expensemanager.core.designsystem.R
import java.util.Date

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
                    Icon(
                        modifier = Modifier
                            .size(35.dp)
                            .rotate(90f)
                            .clickable(
                                indication = null,
                                interactionSource = interactionSource,
                                onClick = {
                                    year--
                                },
                            ),
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                    )

                    Text(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        text = year.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Icon(
                        modifier = Modifier
                            .size(35.dp)
                            .rotate(-90f)
                            .clickable(
                                indication = null,
                                interactionSource = interactionSource,
                                onClick = {
                                    year++
                                },
                            ),
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }

                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .height(60.dp)
                        .align(Alignment.End)
                ) {
                    TextButton(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(end = 16.dp),
                        onClick = {
                            cancelClicked()
                        },
                    ) {
                        Text(text = stringResource(id = R.string.cancel).uppercase())
                    }

                    TextButton(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            confirmButtonCLicked(year)
                        },
                    ) {
                        Text(text = stringResource(id = R.string.ok).uppercase())
                    }
                }
            }
        },
        onDismissRequest = cancelClicked,
    )
}

@Composable
@AppPreviewsLightAndDarkMode
fun YearPickerPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        YearPicker(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp),
                ),
            currentYear = Date().toYearInt(),
            confirmButtonCLicked = { _ -> },
            cancelClicked = {},
        )
    }
}
