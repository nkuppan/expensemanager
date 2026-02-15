package com.naveenapps.expensemanager.feature.category.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.feature.category.R

@Composable
fun CategoryTypeSelectionView(
    selectedCategoryType: CategoryType,
    onCategoryTypeChange: ((CategoryType) -> Unit),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val stroke = if (selectedCategoryType == CategoryType.INCOME) {
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = true,
                borderColor = MaterialTheme.colorScheme.primary,
                selectedBorderColor = MaterialTheme.colorScheme.primary,
                borderWidth = 2.dp,
                selectedBorderWidth = 2.dp
            )
        } else {
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false
            )
        }
        AppFilterChip(
            title = stringResource(id = R.string.income),
            icon = Icons.Outlined.ArrowDownward,
            isSelected = selectedCategoryType == CategoryType.INCOME,
            onClick = { onCategoryTypeChange.invoke(CategoryType.INCOME) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            stroke = stroke
        )
        AppFilterChip(
            title = stringResource(id = R.string.expense),
            icon = Icons.Outlined.ArrowUpward,
            isSelected = selectedCategoryType == CategoryType.EXPENSE,
            onClick = { onCategoryTypeChange.invoke(CategoryType.EXPENSE) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            stroke = if (selectedCategoryType == CategoryType.EXPENSE) {
                FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = true,
                    borderColor = MaterialTheme.colorScheme.error,
                    selectedBorderColor = MaterialTheme.colorScheme.error,
                    borderWidth = 2.dp,
                    selectedBorderWidth = 2.dp
                )
            } else {
                FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false
                )
            }
        )
    }
}

@Composable
private fun RowScope.AppFilterChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    colors: SelectableChipColors,
    stroke: BorderStroke,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = { onClick.invoke() },
        label = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium, // Larger text
                fontWeight = if (isSelected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                }
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp) // Larger icon
            )
        },
        modifier = Modifier
            .weight(1f)
            .height(48.dp), // Much taller
        colors = colors,
        border = stroke,
        shape = RoundedCornerShape(12.dp) // More rounded
    )
}

@Preview
@Composable
private fun CategoryTypeSelectionViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        CategoryTypeSelectionView(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            selectedCategoryType = CategoryType.EXPENSE,
            onCategoryTypeChange = {},
        )
        CategoryTypeSelectionView(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            selectedCategoryType = CategoryType.INCOME,
            onCategoryTypeChange = {},
        )
    }
}
