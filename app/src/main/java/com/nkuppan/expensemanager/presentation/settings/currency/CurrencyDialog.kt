package com.nkuppan.expensemanager.presentation.settings.currency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.nkuppan.expensemanager.domain.model.Currency


@Composable
fun CurrencyDialogView(
    complete: () -> Unit
) {

    val viewModel: CurrencyViewModel = hiltViewModel()

    val selectedCurrency by viewModel.currentCurrency.collectAsState()
    val currencies by viewModel.currencies.collectAsState()

    CurrencyDialogViewContent(
        selectedCurrency = selectedCurrency,
        currencies = currencies,
        onConfirm = {
            viewModel.setCurrency(it)
            complete.invoke()
        }
    )
}

@Composable
fun CurrencyDialogViewContent(
    onConfirm: (Currency?) -> Unit,
    selectedCurrency: Currency,
    currencies: List<Currency> = emptyList()
) {
    Dialog(
        onDismissRequest = {
            onConfirm.invoke(null)
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
            items(currencies) { currency ->
                Row(
                    modifier = Modifier
                        .clickable {
                            onConfirm.invoke(currency)
                        }
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "${stringResource(id = currency.name)} (${stringResource(id = currency.type)})"
                    )
                    if (selectedCurrency.type == currency.type) {
                        Icon(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            painter = painterResource(id = R.drawable.ic_done),
                            contentDescription = null
                        )
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
            onConfirm = {},
            selectedCurrency = Currency(
                R.string.dollar_type,
                R.string.dollar_name,
                R.drawable.currency_dollar
            ),
            currencies =
            listOf(
                Currency(
                    R.string.dollar_type,
                    R.string.dollar_name,
                    R.drawable.currency_dollar
                ),
                Currency(
                    R.string.pound_type,
                    R.string.pound_name,
                    R.drawable.currency_pound
                ),
                Currency(
                    R.string.euro_type,
                    R.string.euro_name,
                    R.drawable.currency_euro
                ),
                Currency(
                    R.string.yen_type,
                    R.string.yen_name,
                    R.drawable.currency_yen
                ),
                Currency(
                    R.string.swiss_franc_type,
                    R.string.swiss_franc_name,
                    R.drawable.currency_franc
                ),
                Currency(
                    R.string.lira_type,
                    R.string.lira_name,
                    R.drawable.currency_lira
                ),
                Currency(
                    R.string.ruble_type,
                    R.string.ruble_name,
                    R.drawable.currency_ruble
                ),
                Currency(
                    R.string.yuan_type,
                    R.string.yuan_name,
                    R.drawable.currency_yuan
                ),
                Currency(
                    R.string.rupee_type,
                    R.string.rupee_name,
                    R.drawable.currency_rupee
                )
            )
        )
    }
}