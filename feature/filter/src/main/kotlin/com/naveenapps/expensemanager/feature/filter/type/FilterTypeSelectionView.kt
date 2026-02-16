package com.naveenapps.expensemanager.feature.filter.type

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.filter.R
import org.koin.compose.viewmodel.koinViewModel
import java.util.Date

@Composable
fun FilterTypeSelectionView(
    applyChanges: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FilterTypeSelectionViewModel = koinViewModel(),
) {
    ObserveAsEvents(viewModel.event) {
        when (it) {
            FilterTypeEvent.Saved -> applyChanges.invoke()
        }
    }

    val state by viewModel.state.collectAsState()

    FilterSelectionView(
        modifier = modifier,
        state = state,
        onAction = viewModel::processAction
    )
}

@Composable
private fun FilterSelectionView(
    modifier: Modifier,
    state: FilterTypeState,
    onAction: (FilterTypeAction) -> Unit,
) {
    Column(modifier = modifier) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.filters),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )

            val totalSelected = state.selectedTransactionTypes.size +
                    state.selectedAccounts.size +
                    state.selectedCategories.size

            if (totalSelected > 0) {
                TextButton(
                    onClick = { onAction.invoke(FilterTypeAction.ClearAll) },
                ) {
                    Text(
                        text = stringResource(R.string.clear_all),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .wrapContentSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Transaction type
            FilterSectionHeader(
                title = stringResource(id = R.string.transaction_type),
                selectedCount = state.selectedTransactionTypes.size,
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.transactionTypes.forEach { type ->
                    val isSelected = state.selectedTransactionTypes.fastAny { it == type }
                    FilterChipView(
                        selected = isSelected,
                        label = type.toCapitalize(),
                        onSelection = { onAction.invoke(FilterTypeAction.SelectTransactionType(type)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Accounts
            FilterSectionHeader(
                title = stringResource(id = R.string.accounts),
                selectedCount = state.selectedAccounts.size,
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.accounts.forEach { account ->
                    val isSelected = state.selectedAccounts.fastAny { it.id == account.id }
                    FilterChipView(
                        selected = isSelected,
                        label = account.name,
                        iconName = account.storedIcon.name,
                        onSelection = { onAction.invoke(FilterTypeAction.SelectAccount(account)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Categories
            FilterSectionHeader(
                title = stringResource(id = R.string.categories),
                selectedCount = state.selectedCategories.size,
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.categories.forEach { category ->
                    val isSelected = state.selectedCategories.fastAny { it.id == category.id }
                    FilterChipView(
                        selected = isSelected,
                        label = category.name,
                        iconName = category.storedIcon.name,
                        onSelection = { onAction.invoke(FilterTypeAction.SelectCategory(category)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom action
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        )

        val totalSelected = state.selectedTransactionTypes.size +
                state.selectedAccounts.size +
                state.selectedCategories.size

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (totalSelected > 0) {
                Text(
                    text = pluralStringResource(
                        id = R.plurals.filters_selected,
                        count = totalSelected,
                        totalSelected,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            TextButton(
                onClick = { onAction.invoke(FilterTypeAction.SaveChanges) },
            ) {
                Text(
                    text = stringResource(id = R.string.apply),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun FilterSectionHeader(
    title: String,
    selectedCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (selectedCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = selectedCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipView(
    selected: Boolean,
    label: String,
    iconName: String? = null,
    onSelection: (() -> Unit) = { },
) {
    val context = LocalContext.current
    FilterChip(
        onClick = onSelection,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            )
        },
        selected = selected,
        leadingIcon = when {
            selected && iconName == null -> {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                }
            }

            iconName != null -> {
                {
                    Icon(
                        painter = painterResource(id = context.getDrawable(iconName)),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                }
            }

            else -> null
        },
    )
}

@Composable
fun InputChipView(
    label: String,
    selected: Boolean,
    iconName: String? = null,
    onSelection: (() -> Unit) = { },
) {
    val context = LocalContext.current
    InputChip(
        onClick = onSelection,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        selected = selected,
        avatar = if (iconName != null) {
            {
                Icon(
                    painter = painterResource(id = context.getDrawable(iconName)),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize),
            )
        },
    )
}

@AppPreviewsLightAndDarkMode
@Composable
fun FilterTypeSelectionPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        FilterSelectionView(
            modifier = Modifier.fillMaxSize(),
            state = FilterTypeState(
                categories = listOf(getCategory(1), getCategory(2)),
                selectedCategories = listOf(getCategory(1)),
                accounts = listOf(getAccount(1), getAccount(2)),
                selectedAccounts = listOf(getAccount(1)),
                transactionTypes = TransactionType.entries,
                selectedTransactionTypes = listOf(TransactionType.INCOME),
            ),
            onAction = {},
        )
    }
}

fun getAccount(index: Int): AccountUiModel {
    return AccountUiModel(
        id = index.toString(),
        name = "Account 1",
        storedIcon = StoredIcon(
            "account_balance",
            "#000000",
        ),
        amount = Amount(0.0, "$ 0.0", currency = null),
        amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
    )
}

fun getCategory(index: Int): Category {
    return Category(
        id = index.toString(),
        name = "Category $index",
        type = CategoryType.INCOME,
        storedIcon = StoredIcon(
            "account_balance",
            "#000000",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    )
}