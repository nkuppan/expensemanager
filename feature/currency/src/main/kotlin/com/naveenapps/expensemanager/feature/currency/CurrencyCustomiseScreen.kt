package com.naveenapps.expensemanager.feature.currency

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.feature.country.CountryCurrencySelectionDialog

@Composable
fun CurrencyCustomiseScreen(
    viewModel: CurrencyViewModel = hiltViewModel(),
) {

    val state by viewModel.state.collectAsState()

    CurrencyScreen(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun CurrencyScreen(
    state: CurrencyState,
    onAction: (CurrencyAction) -> Unit,
) {

    if (state.showCurrencySelection) {
        CountryCurrencySelectionDialog { country ->
            onAction.invoke(CurrencyAction.SelectCurrency(country?.currency))
        }
    }

    Scaffold(
        topBar = {
            TopNavigationBar(
                onClick = {
                    onAction.invoke(CurrencyAction.ClosePage)
                },
                title = stringResource(R.string.currency),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
        ) {
            CurrencyItem(
                modifier = Modifier
                    .clickable {
                        onAction.invoke(CurrencyAction.OpenCurrencySelection)
                    }
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                title = stringResource(id = R.string.select_currency),
                description = "${state.currency.name}(${state.currency.symbol})",
                icon = Icons.Default.CurrencyExchange,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp),
                text = stringResource(id = R.string.currency_position),
                style = MaterialTheme.typography.titleMedium,
            )

            TextFormatSelectionView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp),
                currency = state.currency.symbol,
                selectedCurrencyPositionType = state.currency.position,
                onCurrencyPositionTypeChange = {
                    onAction.invoke(CurrencyAction.ChangeCurrencyType(it))
                },
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp),
                text = stringResource(id = R.string.currency_format),
                style = MaterialTheme.typography.titleMedium,
            )

            TextFormatSelectionView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp),
                textFormat = state.currency.format,
                onTextFormatChange = {
                    onAction.invoke(CurrencyAction.ChangeCurrencyNumberFormat(it))
                },
            )
        }
    }
}

@Composable
private fun CurrencyItem(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            imageVector = icon,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
        ) {
            Text(text = title)
            Text(text = description, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview
@Composable
fun CurrencyCustomiseScreenPreview() {
    ExpenseManagerTheme {
        CurrencyScreen(
            state = CurrencyState(
                showCurrencySelection = false,
                currency = Currency("$", "Dollar")
            ),
            onAction = {}
        )
    }
}
