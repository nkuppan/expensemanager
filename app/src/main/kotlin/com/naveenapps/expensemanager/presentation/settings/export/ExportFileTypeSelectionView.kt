package com.naveenapps.expensemanager.presentation.settings.export

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
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.ui.components.AppFilterChip
import com.naveenapps.expensemanager.ui.theme.ExpenseManagerTheme


@Composable
fun ExportFileTypeSelectionView(
    selectedExportFileType: ExportFileType,
    onExportFileTypeChange: ((ExportFileType) -> Unit),
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppFilterChip(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            centerAlign = true,
            filterName = stringResource(id = R.string.csv),
            isSelected = selectedExportFileType == ExportFileType.CSV,
            onClick = {
                onExportFileTypeChange.invoke(ExportFileType.CSV)
            }
        )
        AppFilterChip(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            centerAlign = true,
            filterName = stringResource(id = R.string.pdf),
            isSelected = selectedExportFileType == ExportFileType.PDF,
            onClick = {
                onExportFileTypeChange.invoke(ExportFileType.PDF)
            }
        )
    }
}

@Preview
@Composable
private fun ExportFileTypeSelectionViewPreview() {
    ExpenseManagerTheme {
        Column {
            ExportFileTypeSelectionView(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                selectedExportFileType = ExportFileType.CSV,
                onExportFileTypeChange = {}
            )
            ExportFileTypeSelectionView(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                selectedExportFileType = ExportFileType.PDF,
                onExportFileTypeChange = {}
            )
        }
    }
}
