package com.nkuppan.expensemanager.presentation.settings.export

import androidx.annotation.ColorRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.ExportFileType
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme


@Composable
fun ExportFileTypeSelectionView(
    selectedExportFileType: ExportFileType,
    onExportFileTypeChange: ((ExportFileType) -> Unit),
    modifier: Modifier = Modifier
) {

    Row(modifier = modifier) {
        CustomExportTypeFilterChip(
            selectedExportFileType = selectedExportFileType,
            categoryType = ExportFileType.CSV,
            filterName = stringResource(id = R.string.csv),
            filterSelectedColor = R.color.blue_500,
            onExportFileTypeChange = onExportFileTypeChange,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
        )
        CustomExportTypeFilterChip(
            selectedExportFileType = selectedExportFileType,
            categoryType = ExportFileType.PDF,
            filterName = stringResource(id = R.string.pdf),
            filterSelectedColor = R.color.blue_500,
            onExportFileTypeChange = onExportFileTypeChange,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp)
                .weight(1f)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RowScope.CustomExportTypeFilterChip(
    selectedExportFileType: ExportFileType,
    categoryType: ExportFileType,
    filterName: String,
    @ColorRes filterSelectedColor: Int,
    onExportFileTypeChange: ((ExportFileType) -> Unit),
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier = modifier,
        selected = selectedExportFileType == categoryType,
        onClick = {
            onExportFileTypeChange.invoke(categoryType)
        },
        label = {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
                text = filterName,
                textAlign = TextAlign.Center
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedLabelColor = Color.White,
            selectedContainerColor = colorResource(id = filterSelectedColor),
            selectedLeadingIconColor = Color.White
        )
    )
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
