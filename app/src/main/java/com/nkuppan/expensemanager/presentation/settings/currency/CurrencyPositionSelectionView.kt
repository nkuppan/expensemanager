package com.nkuppan.expensemanager.presentation.settings.currency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.CurrencySymbolPosition
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme


@Composable
fun CurrencyPositionTypeSelectionView(
    selectedCurrencyPositionType: CurrencySymbolPosition,
    onCurrencyPositionTypeChange: ((CurrencySymbolPosition) -> Unit),
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomCategoryFilterChip(
            selectedCurrencyPositionType = selectedCurrencyPositionType,
            currencySymbolPosition = CurrencySymbolPosition.PREFIX,
            name = stringResource(id = R.string.prefix_amount),
            onCurrencyPositionTypeChange = onCurrencyPositionTypeChange,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
        )
        CustomCategoryFilterChip(
            selectedCurrencyPositionType = selectedCurrencyPositionType,
            currencySymbolPosition = CurrencySymbolPosition.SUFFIX,
            name = stringResource(id = R.string.suffix_amount),
            onCurrencyPositionTypeChange = onCurrencyPositionTypeChange,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RowScope.CustomCategoryFilterChip(
    selectedCurrencyPositionType: CurrencySymbolPosition,
    currencySymbolPosition: CurrencySymbolPosition,
    name: String,
    onCurrencyPositionTypeChange: ((CurrencySymbolPosition) -> Unit),
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier = modifier,
        selected = selectedCurrencyPositionType == currencySymbolPosition,
        onClick = {
            onCurrencyPositionTypeChange.invoke(currencySymbolPosition)
        },
        label = {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = name
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
        )
    )
}

@Preview
@Composable
private fun CurrencyPositionTypeSelectionViewPreview() {
    ExpenseManagerTheme {
        Column {
            CurrencyPositionTypeSelectionView(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                selectedCurrencyPositionType = CurrencySymbolPosition.PREFIX,
                onCurrencyPositionTypeChange = {}
            )
            CurrencyPositionTypeSelectionView(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                selectedCurrencyPositionType = CurrencySymbolPosition.SUFFIX,
                onCurrencyPositionTypeChange = {}
            )
        }
    }
}
