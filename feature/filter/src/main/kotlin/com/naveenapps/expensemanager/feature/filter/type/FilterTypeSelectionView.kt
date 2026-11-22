package com.naveenapps.expensemanager.feature.filter.type

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.em.EmChipVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.AmountTone
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.filter.R
import java.util.Date
import org.koin.compose.viewmodel.koinViewModel

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

    val state by viewModel.state.collectAsStateWithLifecycle()

    FilterSelectionView(
        modifier = modifier,
        state = state,
        onAction = viewModel::processAction,
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
                .padding(start = EmTheme.spacing.lg, end = EmTheme.spacing.sm, top = EmTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmText(
                text = stringResource(id = R.string.filters),
                modifier = Modifier.weight(1f),
                role = EmTextRole.SectionTitle,
            )

            val totalSelected = state.selectedTransactionTypes.size +
                state.selectedAccounts.size +
                state.selectedCategories.size

            if (totalSelected > 0) {
                EmButton(
                    text = stringResource(R.string.clear_all),
                    onClick = { onAction.invoke(FilterTypeAction.ClearAll) },
                    variant = EmButtonVariant.Tertiary,
                )
            }
        }

        // weight(fill = false): the list takes only the height it needs, but with many accounts
        // or categories it stops at what's left and scrolls, so the Apply row below always stays
        // on screen. (Measured first without it, the list used all the height and pushed Apply
        // out of the sheet.)
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
        ) {
            // Transaction type
            FilterSectionHeader(
                title = stringResource(id = R.string.transaction_type),
                selectedCount = state.selectedTransactionTypes.size,
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                state.transactionTypes.forEach { type ->
                    val isSelected = state.selectedTransactionTypes.fastAny { it == type }
                    FilterChipView(
                        selected = isSelected,
                        label = stringResource(type.labelRes()),
                        onSelection = { onAction.invoke(FilterTypeAction.SelectTransactionType(type)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(EmTheme.spacing.sm))

            // Accounts
            FilterSectionHeader(
                title = stringResource(id = R.string.accounts),
                selectedCount = state.selectedAccounts.size,
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
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

            Spacer(modifier = Modifier.height(EmTheme.spacing.sm))

            // Categories
            FilterSectionHeader(
                title = stringResource(id = R.string.categories),
                selectedCount = state.selectedCategories.size,
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                state.categories.forEach { category ->
                    val isSelected = state.selectedCategories.fastAny { it.id == category.id }
                    FilterChipView(
                        selected = isSelected,
                        label = category.titleResId?.let { stringResource(it) } ?: category.name,
                        iconName = category.storedIcon.name,
                        onSelection = { onAction.invoke(FilterTypeAction.SelectCategory(category)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(EmTheme.spacing.lg))
        }

        // Bottom action
        EmDivider()

        val totalSelected = state.selectedTransactionTypes.size +
            state.selectedAccounts.size +
            state.selectedCategories.size

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (totalSelected > 0) {
                EmText(
                    text = pluralStringResource(
                        id = R.plurals.filters_selected,
                        count = totalSelected,
                        totalSelected,
                    ),
                    modifier = Modifier.weight(1f),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            EmButton(
                text = stringResource(id = R.string.apply),
                onClick = { onAction.invoke(FilterTypeAction.SaveChanges) },
                variant = EmButtonVariant.Tertiary,
            )
        }
    }
}

/** Translated chip label; the enum name was shown before ("Income" in every language). */
private fun TransactionType.labelRes(): Int = when (this) {
    TransactionType.INCOME -> DesignSystemR.string.income
    TransactionType.EXPENSE -> DesignSystemR.string.expense
    TransactionType.TRANSFER -> R.string.filter_type_transfer
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
            .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmText(
            text = title,
            role = EmTextRole.Label,
            color = EmTextColor.Supporting,
        )
        if (selectedCount > 0) {
            Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
            Box(
                modifier = Modifier
                    .size(EmTheme.icons.md)
                    .clip(EmTheme.shapes.pill)
                    .background(EmTheme.colors.scheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    EmText(
                        text = selectedCount.toString(),
                        role = EmTextRole.Supporting,
                        color = EmTextColor.OnPrimary,
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
    EmChip(
        label = label,
        selected = selected,
        onClick = onSelection,
        leadingPainter = iconName?.let { painterResource(id = context.getDrawable(it)) },
    )
}

/** An applied filter shown above the list; tapping it (or its close icon) removes it. */
@Composable
fun InputChipView(
    label: String,
    selected: Boolean,
    iconName: String? = null,
    onSelection: (() -> Unit) = { },
) {
    val context = LocalContext.current
    EmChip(
        label = label,
        onClick = onSelection,
        variant = EmChipVariant.Input,
        selected = selected,
        leadingPainter = iconName?.let { painterResource(id = context.getDrawable(it)) },
        onRemove = onSelection,
    )
}

@EmPreviews
@Composable
fun FilterTypeSelectionPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
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

fun getAccount(index: Int): AccountUiModel = AccountUiModel(
    id = index.toString(),
    name = "Account 1",
    storedIcon = StoredIcon(
        "account_balance",
        "#000000",
    ),
    amount = Amount(0.0, "$ 0.0", currency = null),
    amountTone = AmountTone.POSITIVE,
)

fun getCategory(index: Int): Category = Category(
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
