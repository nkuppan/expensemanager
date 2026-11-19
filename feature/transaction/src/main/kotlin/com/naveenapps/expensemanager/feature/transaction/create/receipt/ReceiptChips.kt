package com.naveenapps.expensemanager.feature.transaction.create.receipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.em.EmChipVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.feature.transaction.R

/** "Scan receipt", or a spinner with "Reading receipt…" while the text is being recognised. */
@Composable
internal fun ScanReceiptChip(
    isReading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmChip(
        label = stringResource(if (isReading) R.string.reading_receipt else R.string.scan_receipt),
        onClick = onClick,
        modifier = modifier,
        variant = EmChipVariant.Suggestion,
        leadingIcon = Icons.Outlined.DocumentScanner,
        loading = isReading,
    )
}

/** The categories used before for a receipt's payee, as one-tap chips under the category. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SuggestedCategoryRow(
    categories: List<Category>,
    selected: Category,
    onSelect: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
    ) {
        EmText(
            text = stringResource(R.string.suggested_categories),
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
            categories.forEach { category ->
                val isSelected = category.id == selected.id
                EmChip(
                    label = category.titleResId?.let { stringResource(it) } ?: category.name,
                    selected = isSelected,
                    onClick = { onSelect(category) },
                    leadingIcon = if (isSelected) Icons.Outlined.AutoAwesome else null,
                )
            }
        }
    }
}
