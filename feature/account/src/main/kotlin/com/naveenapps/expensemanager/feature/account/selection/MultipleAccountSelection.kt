package com.naveenapps.expensemanager.feature.account.selection

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.MultiSelectionSheet
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountUiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MultipleAccountSelectionScreen(
    viewModel: AccountSelectionViewModel = koinViewModel(),
    selectedAccounts: List<AccountUiModel> = emptyList(),
    onItemSelection: ((List<AccountUiModel>, Boolean) -> Unit)? = null,
    /** Closes the sheet without applying; shown as an × in the header when set. */
    onClose: (() -> Unit)? = null,
) {
    viewModel.selectAllThisAccount(selectedAccounts)

    AccountSelectionView(viewModel, onItemSelection, onClose)
}

@Composable
private fun AccountSelectionView(
    viewModel: AccountSelectionViewModel,
    onItemSelection: ((List<AccountUiModel>, Boolean) -> Unit)?,
    onClose: (() -> Unit)?,
) {
    val activity = LocalActivity.current
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val selectedAccounts by viewModel.selectedAccounts.collectAsStateWithLifecycle()

    MultipleAccountSelectionScreen(
        modifier = Modifier,
        accounts = accounts,
        selectedAccounts = selectedAccounts,
        onApplyChanges = {
            if (selectedAccounts.isNotEmpty()) {
                onItemSelection?.invoke(
                    selectedAccounts,
                    selectedAccounts.size == accounts.size,
                )
            } else {
                if (activity != null && !activity.isFinishing) {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.account_selection_message),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        },
        onSetSelection = viewModel::setSelection,
        onItemSelection = viewModel::selectThisAccount,
        onClose = onClose,
    )
}

@Composable
fun MultipleAccountSelectionScreen(
    modifier: Modifier = Modifier,
    accounts: List<AccountUiModel>,
    selectedAccounts: List<AccountUiModel>,
    onApplyChanges: (() -> Unit),
    onSetSelection: ((List<AccountUiModel>, Boolean) -> Unit),
    onItemSelection: ((AccountUiModel, Boolean) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    // Search only narrows what's listed; "Select all" acts on what's listed.
    var query by rememberSaveable { mutableStateOf("") }
    val visibleAccounts = remember(accounts, query) {
        val search = query.trim()
        if (search.isEmpty()) accounts else accounts.filter { it.name.contains(search, ignoreCase = true) }
    }
    val selectedIds = remember(selectedAccounts) { selectedAccounts.map { it.id }.toSet() }
    val selectedVisible = visibleAccounts.count { it.id in selectedIds }
    val selectAllState = when {
        visibleAccounts.isEmpty() || selectedVisible == 0 -> ToggleableState.Off
        selectedVisible == visibleAccounts.size -> ToggleableState.On
        else -> ToggleableState.Indeterminate
    }

    MultiSelectionSheet(
        title = stringResource(id = R.string.select_account),
        searchPlaceholder = stringResource(id = R.string.search_accounts),
        query = query,
        onQueryChange = { query = it },
        selectedText = pluralStringResource(
            id = R.plurals.items_selected,
            count = selectedAccounts.size,
            selectedAccounts.size,
        ),
        selectAllState = selectAllState,
        onSelectAllClick = { onSetSelection(visibleAccounts, selectAllState != ToggleableState.On) },
        showNoResults = visibleAccounts.isEmpty() && query.isNotBlank(),
        onDone = onApplyChanges,
        onClose = onClose,
        modifier = modifier,
    ) {
        items(visibleAccounts, key = { it.id }) { account ->
            val isSelected = account.id in selectedIds
            AccountItem(
                name = account.name,
                icon = account.storedIcon.name,
                iconBackgroundColor = account.storedIcon.backgroundColor,
                amount = account.amount.amountString,
                amountTextColor = account.amountTextColor,
                customImagePath = account.storedIcon.customImagePath,
                border = AccountItemDefaults.border(isSelected),
                onClick = {
                    onItemSelection?.invoke(account, isSelected.not())
                },
                trailingContent = {
                    AccountItemDefaults.MultiCheckedTrailing(isSelected)
                },
            )
        }
    }
}

@Preview
@Composable
private fun MultipleAccountSelectionScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        val accounts = getRandomAccountUiModel(10)
        MultipleAccountSelectionScreen(
            accounts = accounts,
            selectedAccounts = listOf(accounts[0], accounts[1]),
            onApplyChanges = {},
            onSetSelection = { _, _ -> },
            onItemSelection = null,
        )
    }
}
