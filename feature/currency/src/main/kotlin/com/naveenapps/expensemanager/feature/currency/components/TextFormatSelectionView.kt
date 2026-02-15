package com.naveenapps.expensemanager.feature.currency.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppFilterChip
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import com.naveenapps.expensemanager.feature.currency.R

@Composable
fun TextFormatSelectionView(
    textFormat: NumberFormatType,
    onTextFormatChange: ((NumberFormatType) -> Unit),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppFilterChip(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            filterName = stringResource(id = R.string.none),
            isSelected = textFormat == NumberFormatType.WITHOUT_ANY_SEPARATOR,
            onClick = {
                onTextFormatChange.invoke(NumberFormatType.WITHOUT_ANY_SEPARATOR)
            },
        )
        AppFilterChip(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            filterName = stringResource(id = R.string.number_format),
            isSelected = textFormat == NumberFormatType.WITH_COMMA_SEPARATOR,
            onClick = {
                onTextFormatChange.invoke(NumberFormatType.WITH_COMMA_SEPARATOR)
            },
        )
    }
}

@Preview
@Composable
private fun CurrencyPositionTypeSelectionViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        Column {
            TextFormatSelectionView(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                currency = "$",
                selectedCurrencyPositionType = CurrencyPosition.PREFIX,
                onCurrencyPositionTypeChange = {},
            )
            TextFormatSelectionView(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                currency = "$",
                selectedCurrencyPositionType = CurrencyPosition.SUFFIX,
                onCurrencyPositionTypeChange = {},
            )
        }
    }
}
