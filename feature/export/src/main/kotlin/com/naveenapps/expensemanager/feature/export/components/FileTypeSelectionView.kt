package com.naveenapps.expensemanager.feature.export.components

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
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.feature.export.R

@Composable
fun ExportFileTypeSelectionView(
    selectedExportFileType: ExportFileType,
    onExportFileTypeChange: ((ExportFileType) -> Unit),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        EmChip(
            label = stringResource(id = R.string.csv),
            selected = selectedExportFileType == ExportFileType.CSV,
            onClick = {
                onExportFileTypeChange.invoke(ExportFileType.CSV)
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
        )
        EmChip(
            label = stringResource(id = R.string.pdf),
            selected = selectedExportFileType == ExportFileType.PDF,
            onClick = {
                onExportFileTypeChange.invoke(ExportFileType.PDF)
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
        )
    }
}

@Preview
@Composable
private fun ExportFileTypeSelectionViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        Column {
            ExportFileTypeSelectionView(
                modifier = Modifier
                    .padding(top = EmTheme.spacing.lg, start = EmTheme.spacing.lg, end = EmTheme.spacing.lg)
                    .fillMaxWidth(),
                selectedExportFileType = ExportFileType.CSV,
                onExportFileTypeChange = {},
            )
            ExportFileTypeSelectionView(
                modifier = Modifier
                    .padding(EmTheme.spacing.lg)
                    .fillMaxWidth(),
                selectedExportFileType = ExportFileType.PDF,
                onExportFileTypeChange = {},
            )
        }
    }
}
