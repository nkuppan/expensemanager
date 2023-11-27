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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.nkuppan.countrycompose.presentation.currency.CountryCurrencySelectionDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.utils.shouldUseDarkTheme
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFormat
import com.naveenapps.expensemanager.core.model.TextPosition


typealias CountryPickerCountry = com.github.nkuppan.country.domain.model.Country

@Composable
fun CurrencyCustomiseScreen(
    viewModel: CurrencyViewModel = hiltViewModel()
) {
    var showCurrencyPage by remember { mutableStateOf(false) }
    val selectedCurrency by viewModel.currentCurrency.collectAsState()
    val theme by viewModel.theme.collectAsState()

    if (showCurrencyPage) {
        CountryCurrencySelectionDialog(
            isDarkTheme = shouldUseDarkTheme(theme = theme.mode),
            onDismissRequest = {
                showCurrencyPage = false
            }
        ) { country ->
            viewModel.selectThisCurrency(
                country.toExpenseCurrency()
            )
            showCurrencyPage = false
        }
    }

    CurrencyScreen(
        selectedCurrency = selectedCurrency,
        onTextPositionChange = viewModel::setCurrencyPositionType,
        onTextFormatChange = viewModel::setTextFormatChange,
        openCurrencySelection = {
            showCurrencyPage = true
        },
        closePage = viewModel::closePage,
    )
}

private fun CountryPickerCountry.toExpenseCurrency(): Currency {
    return Currency(
        name = currency?.name ?: name ?: "",
        symbol = currency?.symbol ?: currency?.nativeSymbol ?: countryCode ?: ""
    )
}

@Composable
private fun CurrencyScreen(
    selectedCurrency: Currency,
    onTextPositionChange: (TextPosition) -> Unit,
    onTextFormatChange: (TextFormat) -> Unit,
    openCurrencySelection: () -> Unit,
    closePage: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopNavigationBar(
                onClick = {
                    closePage.invoke()
                }, title = stringResource(R.string.currency)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {

            CurrencyItem(
                modifier = Modifier
                    .clickable {
                        openCurrencySelection.invoke()
                    }
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                title = stringResource(id = R.string.select_currency),
                description = "${selectedCurrency.name}(${selectedCurrency.symbol})",
                icon = Icons.Default.CurrencyExchange
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp),
                text = stringResource(id = R.string.currency_position),
                style = MaterialTheme.typography.titleMedium
            )

            TextFormatSelectionView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp),
                selectedCurrencyPositionType = selectedCurrency.position,
                onCurrencyPositionTypeChange = onTextPositionChange
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp),
                text = stringResource(id = R.string.currency_format),
                style = MaterialTheme.typography.titleMedium
            )

            TextFormatSelectionView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp),
                textFormat = selectedCurrency.format,
                onTextFormatChange = onTextFormatChange
            )
        }
    }
}

@Composable
private fun CurrencyItem(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            imageVector = icon,
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
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
            selectedCurrency = Currency("$", "Dollar"),
            onTextPositionChange = {},
            onTextFormatChange = {},
            openCurrencySelection = {},
            closePage = {}
        )
    }
}