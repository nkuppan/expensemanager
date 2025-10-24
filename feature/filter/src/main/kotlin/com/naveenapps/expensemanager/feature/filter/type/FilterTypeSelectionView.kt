package com.naveenapps.expensemanager.feature.filter.type

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import org.koin.compose.viewmodel.koinViewModel
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.filter.R
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
    onAction: (FilterTypeAction) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(id = R.string.transaction_type),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            TransactionTypeFilter(
                transactionTypes = state.transactionTypes,
                selectedTransactionType = state.selectedTransactionTypes,
                onSelection = {
                    onAction.invoke(FilterTypeAction.SelectTransactionType(it))
                }
            )
        }
        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(id = R.string.accounts),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            AccountFilter(
                accounts = state.accounts,
                selectedAccounts = state.selectedAccounts,
                onSelection = {
                    onAction.invoke(FilterTypeAction.SelectAccount(it))
                }
            )
        }
        Text(
            modifier = Modifier.padding(16.dp),
            text = stringResource(id = R.string.categories),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            CategoryFilter(
                categories = state.categories,
                selectedCategories = state.selectedCategories,
                onSelection = {
                    onAction.invoke(FilterTypeAction.SelectCategory(it))
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        ) {
            TextButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { onAction.invoke(FilterTypeAction.SaveChanges) }
            ) {
                Text(text = stringResource(id = R.string.apply))
            }
        }
    }
}

@Composable
fun TransactionTypeFilter(
    transactionTypes: List<TransactionType>,
    selectedTransactionType: List<TransactionType>,
    onSelection: (TransactionType) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        transactionTypes.forEach { type ->
            val isSelected = selectedTransactionType.fastAny { it == type }
            FilterChipView(isSelected, type.toCapitalize()) {
                onSelection.invoke(type)
            }
        }
    }
}

@Composable
fun AccountFilter(
    accounts: List<AccountUiModel>,
    selectedAccounts: List<AccountUiModel>,
    onSelection: (AccountUiModel) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        accounts.forEach { account ->
            val isSelected = selectedAccounts.fastAny { it.id == account.id }
            FilterChipView(isSelected, account.name, iconName = account.storedIcon.name) {
                onSelection.invoke(account)
            }
        }
    }
}

@Composable
fun CategoryFilter(
    categories: List<Category>,
    selectedCategories: List<Category>,
    onSelection: (Category) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategories.fastAny { it.id == category.id }
            FilterChipView(isSelected, category.name, iconName = category.storedIcon.name) {
                onSelection.invoke(category)
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
            Text(label, style = MaterialTheme.typography.bodySmall)
        },
        selected = selected,
        leadingIcon = if (iconName != null) {
            {
                Icon(
                    painter = painterResource(id = context.getDrawable(iconName)),
                    contentDescription = "Localized description",
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
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
            Text(label, style = MaterialTheme.typography.bodySmall)
        },
        selected = selected,
        avatar = if (iconName != null) {
            {
                Icon(
                    painter = painterResource(id = context.getDrawable(iconName)),
                    contentDescription = "Localized description",
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Localized description",
                modifier = Modifier.size(FilterChipDefaults.IconSize),
            )
        },
    )
}

@AppPreviewsLightAndDarkMode
@Composable
fun FilterTypeSelectionPreview() {
    ExpenseManagerTheme {
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
        storedIcon = StoredIcon("", ""),
        amount = Amount(0.0, "$ 0.0", currency = null),
        amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500
    )
}

fun getCategory(index: Int): Category {
    return Category(
        id = index.toString(),
        name = "Account 1",
        type = CategoryType.INCOME,
        storedIcon = StoredIcon("", ""),
        createdOn = Date(),
        updatedOn = Date(),
    )
}
