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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.naveenapps.expensemanager.core.common.utils.toMonth
import com.naveenapps.expensemanager.core.common.utils.toYearInt
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import java.util.Date

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
                LazyVerticalGrid(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    columns = GridCells.Adaptive(minSize = 96.dp),
                ) {
                    items(months) {
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
                                        shape = CircleShape,
                                    ),
                            )

                            Text(
                                text = it,
                                color = if (month == it) Color.White else Color.Black,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
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
                            confirmButtonCLicked(
                                months.indexOf(month) + 1,
                                year,
                            )
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

@Preview
@Composable
fun MonthPickerPreview() {
    ExpenseManagerTheme {
        MonthPicker(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp),
                ),
            currentMonth = Date().toMonth(),
            currentYear = Date().toYearInt(),
            confirmButtonCLicked = { month, year ->

            },
            cancelClicked = {

            },
        )
    }
}
