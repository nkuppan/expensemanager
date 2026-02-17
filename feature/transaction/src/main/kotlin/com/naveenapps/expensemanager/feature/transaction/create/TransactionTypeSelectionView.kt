package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.SwapHoriz
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.transaction.R

@Composable
fun TransactionTypeSelectionView(
    selectedTransactionType: TransactionType,
    onTransactionTypeChange: ((TransactionType) -> Unit),
    modifier: Modifier = Modifier,
) {
    val transactionTypes = remember {
        listOf(
            TransactionTypeUi(
                type = TransactionType.INCOME,
                labelRes = R.string.income,
                icon = Icons.AutoMirrored.Rounded.TrendingDown,
                activeContainerColor = { MaterialTheme.colorScheme.primaryContainer },
                activeContentColor = { MaterialTheme.colorScheme.onPrimaryContainer },
                activeBorderColor = { MaterialTheme.colorScheme.primary },
            ),
            TransactionTypeUi(
                type = TransactionType.EXPENSE,
                labelRes = R.string.expense,
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                activeContainerColor = { MaterialTheme.colorScheme.errorContainer },
                activeContentColor = { MaterialTheme.colorScheme.onErrorContainer },
                activeBorderColor = { MaterialTheme.colorScheme.error },
            ),
            TransactionTypeUi(
                type = TransactionType.TRANSFER,
                labelRes = R.string.transfer,
                icon = Icons.Rounded.SwapHoriz,
                activeContainerColor = { MaterialTheme.colorScheme.tertiaryContainer },
                activeContentColor = { MaterialTheme.colorScheme.onTertiaryContainer },
                activeBorderColor = { MaterialTheme.colorScheme.tertiary },
            ),
        )
    }

    val selectedItem = transactionTypes.first { it.type == selectedTransactionType }

    val borderColor by animateColorAsState(
        targetValue = selectedItem.activeBorderColor(),
        animationSpec = tween(250),
        label = "border_color",
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth(),
    ) {
        transactionTypes.forEachIndexed { index, item ->
            val isSelected = selectedTransactionType == item.type

            SegmentedButton(
                selected = isSelected,
                onClick = { onTransactionTypeChange.invoke(item.type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = transactionTypes.size,
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = item.activeContainerColor(),
                    activeContentColor = item.activeContentColor(),
                    activeBorderColor = item.activeBorderColor(),
                ),
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
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

private data class TransactionTypeUi(
    val type: TransactionType,
    val labelRes: Int,
    val icon: ImageVector,
    val activeContainerColor: @Composable () -> Color,
    val activeContentColor: @Composable () -> Color,
    val activeBorderColor: @Composable () -> Color,
)

@Preview
@Composable
private fun TransactionTypeSelectionViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        Column {
            TransactionTypeSelectionView(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                selectedTransactionType = TransactionType.INCOME,
                onTransactionTypeChange = {},
            )
            TransactionTypeSelectionView(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                selectedTransactionType = TransactionType.EXPENSE,
                onTransactionTypeChange = {},
            )
            TransactionTypeSelectionView(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                selectedTransactionType = TransactionType.TRANSFER,
                onTransactionTypeChange = {},
            )
        }
    }
}
