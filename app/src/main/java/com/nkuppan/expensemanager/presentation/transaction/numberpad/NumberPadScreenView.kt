package com.nkuppan.expensemanager.presentation.transaction.numberpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.common.ui.theme.ExpenseManagerTheme


@Composable
fun NumberPadDialogView(
    onConfirm: ((String?) -> Unit)
) {
    Dialog(
        onDismissRequest = {
            onConfirm.invoke(null)
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            NumberPadScreen(onConfirm)
        }
    }
}

@Composable
fun NumberPadScreen(
    onConfirm: ((String?) -> Unit)
) {

    val viewModel: NumberPadViewModel = hiltViewModel()
    val calculatedAmount by viewModel.calculatedAmount.collectAsState()
    val calculatedAmountString by viewModel.calculatedAmountString.collectAsState()

    NumberPadScreenView(
        modifier = Modifier.wrapContentHeight(),
        value = calculatedAmount,
        amountString = calculatedAmountString,
        onChange = {
            viewModel.appendString(it)
        },
        confirm = {
            onConfirm.invoke(calculatedAmount)
        },
        cancel = {
            onConfirm.invoke("0")
        },
        clear = {
            viewModel.clearAmount()
        }
    )
}

@Composable
private fun NumberPadScreenView(
    modifier: Modifier = Modifier,
    value: String = "0.0",
    amountString: String = "",
    onChange: ((String) -> Unit),
    confirm: (() -> Unit),
    cancel: (() -> Unit),
    clear: (() -> Unit),
) {
    Column(modifier) {

        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.7f),
                    text = amountString,
                    textAlign = TextAlign.Right,
                    letterSpacing = 1.sp
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = value,
                    textAlign = TextAlign.Right,
                    fontSize = 28.sp,
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
                    .height(60.dp)
            )

            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp),
                onClick = {
                    onChange.invoke("")
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_backspace),
                    contentDescription = null
                )
            }
        }
        Surface(
            shadowElevation = 2.dp
        ) {

            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("1")
                        },
                        text = stringResource(id = R.string.number_one)
                    )
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("2")
                        },
                        text = stringResource(id = R.string.number_two)
                    )
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("3")
                        },
                        text = stringResource(id = R.string.number_three)
                    )
                    NumberPadActionText(
                        modifier = Modifier
                            .clickable {
                                onChange.invoke("/")
                            },
                        text = stringResource(id = R.string.divide)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("4")
                        },
                        text = stringResource(id = R.string.number_four)
                    )
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("5")
                        },
                        text = stringResource(id = R.string.number_five)
                    )
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("6")
                        },
                        text = stringResource(id = R.string.number_six)
                    )
                    NumberPadActionText(
                        modifier = Modifier
                            .clickable {
                                onChange.invoke("*")
                            },
                        text = stringResource(id = R.string.multiply)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("7")
                        },
                        text = stringResource(id = R.string.number_seven)
                    )
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("8")
                        },
                        text = stringResource(id = R.string.number_eight)
                    )
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("9")
                        },
                        text = stringResource(id = R.string.number_nine)
                    )
                    NumberPadActionText(
                        modifier = Modifier
                            .clickable {
                                onChange.invoke("-")
                            },
                        text = stringResource(id = R.string.minus)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke(".")
                        },
                        text = stringResource(id = R.string.dot)
                    )
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("0")
                        },
                        text = stringResource(id = R.string.zero)
                    )
                    NumberPadActionText(
                        modifier = Modifier.clickable {
                            onChange.invoke("00")
                        },
                        text = stringResource(id = R.string.double_zero)
                    )
                    NumberPadActionText(
                        modifier = Modifier
                            .clickable {
                                onChange.invoke("+")
                            },
                        text = stringResource(id = R.string.plus)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                TextButton(
                    modifier = modifier.fillMaxHeight(),
                    onClick = {
                        clear.invoke()
                    }
                ) {
                    Text(text = stringResource(id = R.string.clear).uppercase())
                }
            }
            TextButton(
                modifier = modifier.fillMaxHeight(),
                onClick = {
                    cancel.invoke()
                }) {
                Text(text = stringResource(id = R.string.cancel).uppercase())
            }
            TextButton(
                modifier = modifier.fillMaxHeight(),
                onClick = {
                    confirm.invoke()
                }
            ) {
                Text(text = stringResource(id = R.string.ok).uppercase())
            }
        }
    }
}

@Composable
fun RowScope.NumberPadActionText(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .weight(1f)
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500
        )
    }
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
) {
    val targetThickness = if (thickness == Dp.Hairline) {
        (1f / LocalDensity.current.density).dp
    } else {
        thickness
    }
    Box(
        modifier
            .width(targetThickness)
            .background(color = color)
    )
}

@Preview
@Composable
fun NumberPadScreenPreview() {
    ExpenseManagerTheme {
        NumberPadScreenView(
            modifier = Modifier.wrapContentHeight(),
            value = "0",
            amountString = "5697+2367*227", {}, {}, {}, {}
        )
    }
}

@Preview
@Composable
fun NumberPadScreenDialogPreview() {
    ExpenseManagerTheme {
        NumberPadDialogView {

        }
    }
}