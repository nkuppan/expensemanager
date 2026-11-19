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
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
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
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        EmChip(
            label = stringResource(id = R.string.none),
            selected = textFormat == NumberFormatType.WITHOUT_ANY_SEPARATOR,
            onClick = {
                onTextFormatChange.invoke(NumberFormatType.WITHOUT_ANY_SEPARATOR)
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
        )
        EmChip(
            label = stringResource(id = R.string.number_format),
            selected = textFormat == NumberFormatType.WITH_COMMA_SEPARATOR,
            onClick = {
                onTextFormatChange.invoke(NumberFormatType.WITH_COMMA_SEPARATOR)
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
        )
    }
}

@Preview
@Composable
private fun CurrencyPositionTypeSelectionViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        Column {
            TextFormatSelectionView(
                modifier = Modifier
                    .padding(top = EmTheme.spacing.lg, start = EmTheme.spacing.lg, end = EmTheme.spacing.lg)
                    .fillMaxWidth(),
                currency = "$",
                selectedCurrencyPositionType = CurrencyPosition.PREFIX,
                onCurrencyPositionTypeChange = {},
            )
            TextFormatSelectionView(
                modifier = Modifier
                    .padding(EmTheme.spacing.lg)
                    .fillMaxWidth(),
                currency = "$",
                selectedCurrencyPositionType = CurrencyPosition.SUFFIX,
                onCurrencyPositionTypeChange = {},
            )
        }
    }
}
