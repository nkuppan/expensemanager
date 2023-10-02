package com.nkuppan.expensemanager.presentation.settings.currency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.data.repository.availableCurrencies
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.CurrencySymbolPosition


@Composable
fun CurrencyDialogView(
    complete: () -> Unit
) {

    val viewModel: CurrencyViewModel = hiltViewModel()

    val selectedCurrency by viewModel.currentCurrency.collectAsState()
    val currencies by viewModel.currencies.collectAsState()

    CurrencyDialogViewContent(
        currencies = currencies,
        selectedCurrency = selectedCurrency,
        onCurrencySelection = viewModel::selectThisCurrency,
        onCurrencyPositionTypeChange = viewModel::setCurrencyPositionType,
        onDismiss = complete,
        onSave = {
            viewModel.saveSelectedCurrency()
            complete.invoke()
        },
    )
}

@Composable
fun CurrencyDialogViewContent(
    currencies: List<Currency>,
    selectedCurrency: Currency,
    onCurrencySelection: (Currency?) -> Unit,
    onCurrencyPositionTypeChange: ((CurrencySymbolPosition) -> Unit),
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        LazyColumn(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    text = stringResource(id = R.string.choose_currency),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                CurrencyPositionTypeSelectionView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp),
                    selectedCurrencyPositionType = selectedCurrency.position,
                    onCurrencyPositionTypeChange = onCurrencyPositionTypeChange
                )
            }
            items(currencies) { currency ->
                val isSelectedCurrency = selectedCurrency.type == currency.type
                Row(
                    modifier = Modifier
                        .clickable {
                            onCurrencySelection.invoke(currency)
                        }
                        .fillMaxWidth()
                        .then(
                            if (isSelectedCurrency) {
                                Modifier
                                    .padding(4.dp)
                                    .background(
                                        color = colorResource(id = R.color.green_100),
                                        shape = RoundedCornerShape(size = 12.dp)
                                    )
                            } else {
                                Modifier
                            }
                        )
                        .padding(16.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "${stringResource(id = currency.name)} (${stringResource(id = currency.type)})"
                    )
                    if (isSelectedCurrency) {
                        Icon(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            painter = painterResource(id = R.drawable.ic_done),
                            contentDescription = null
                        )
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.End)
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(text = stringResource(id = R.string.cancel).uppercase())
                        }
                        TextButton(onClick = onSave) {
                            Text(text = stringResource(id = R.string.ok).uppercase())
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CurrencyDialogViewPreview() {
    ExpenseManagerTheme {
        CurrencyDialogViewContent(
            onCurrencySelection = {},
            selectedCurrency = Currency(
                R.string.dollar_type,
                R.string.dollar_name,
                R.drawable.currency_dollar
            ),
            currencies = availableCurrencies,
            onCurrencyPositionTypeChange = {

            },
            onDismiss = {},
            onSave = {},
        )
    }
}