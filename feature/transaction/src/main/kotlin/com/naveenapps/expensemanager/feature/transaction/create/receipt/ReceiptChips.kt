package com.naveenapps.expensemanager.feature.transaction.create.receipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.feature.transaction.R

/** "Scan receipt", or a spinner with "Reading receipt…" while the text is being recognised. */
@Composable
internal fun ScanReceiptChip(
    isReading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        enabled = !isReading,
        label = {
            Text(text = stringResource(if (isReading) R.string.reading_receipt else R.string.scan_receipt))
        },
        leadingIcon = {
            if (isReading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.DocumentScanner,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            }
        },
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
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.suggested_categories),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                val isSelected = category.id == selected.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(category) },
                    label = {
                        Text(
                            text = category.titleResId?.let { stringResource(it) } ?: category.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
