package com.naveenapps.expensemanager.feature.category.create

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.feature.category.R

@Composable
fun CategoryTypeSelectionView(
    selectedCategoryType: CategoryType,
    onCategoryTypeChange: ((CategoryType) -> Unit),
    modifier: Modifier = Modifier,
) {
    val categoryTypes = remember {
        listOf(
            CategoryTypeUi(
                type = CategoryType.INCOME,
                labelRes = R.string.income,
                icon = Icons.AutoMirrored.Rounded.TrendingDown,
            ),
            CategoryTypeUi(
                type = CategoryType.EXPENSE,
                labelRes = R.string.expense,
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
            ),
        )
    }

    val isExpenseSelected = selectedCategoryType == CategoryType.EXPENSE

    val borderColor by animateColorAsState(
        targetValue = if (isExpenseSelected)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(250),
        label = "border_color",
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth(),
    ) {
        categoryTypes.forEachIndexed { index, item ->
            val isSelected = selectedCategoryType == item.type

            val colors = if (item.type == CategoryType.EXPENSE) {
                SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.errorContainer,
                    activeContentColor = MaterialTheme.colorScheme.onErrorContainer,
                    activeBorderColor = MaterialTheme.colorScheme.error,
                )
            } else {
                SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    activeBorderColor = MaterialTheme.colorScheme.primary,
                )
            }

            SegmentedButton(
                selected = isSelected,
                onClick = { onCategoryTypeChange.invoke(item.type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = categoryTypes.size,
                ),
                colors = colors,
                border = SegmentedButtonDefaults.borderStroke(
                    color = borderColor,
                ),
                icon = {
                    SegmentedButtonDefaults.Icon(active = isSelected) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(id = item.labelRes),
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

private data class CategoryTypeUi(
    val type: CategoryType,
    val labelRes: Int,
    val icon: ImageVector,
)

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryTypeSelectionViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        Column {
            CategoryTypeSelectionView(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                selectedCategoryType = CategoryType.INCOME,
                onCategoryTypeChange = {},
            )
            CategoryTypeSelectionView(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                selectedCategoryType = CategoryType.EXPENSE,
                onCategoryTypeChange = {},
            )
        }
    }
}
