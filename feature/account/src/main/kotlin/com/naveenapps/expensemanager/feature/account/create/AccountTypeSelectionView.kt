package com.naveenapps.expensemanager.feature.account.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmSegmented
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.feature.account.R

@Composable
fun AccountTypeSelectionView(
    selectedAccountType: AccountType,
    onAccountTypeChange: ((AccountType) -> Unit),
    modifier: Modifier = Modifier,
) {
    val types = listOf(AccountType.REGULAR, AccountType.CREDIT)
    EmSegmented(
        options = listOf(stringResource(id = R.string.bank_account), stringResource(id = R.string.credit)),
        selectedIndex = types.indexOf(selectedAccountType),
        onSelect = { onAccountTypeChange(types[it]) },
        modifier = modifier,
        icons = listOf(Icons.Rounded.AccountBalance, Icons.Rounded.CreditCard),
    )
}

@EmPreviews
@Composable
private fun AccountTypeSelectionViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        Column {
            AccountTypeSelectionView(
                modifier = Modifier
                    .padding(top = EmTheme.spacing.lg, start = EmTheme.spacing.lg, end = EmTheme.spacing.lg)
                    .fillMaxWidth(),
                selectedAccountType = AccountType.REGULAR,
                onAccountTypeChange = {},
            )
            AccountTypeSelectionView(
                modifier = Modifier
                    .padding(EmTheme.spacing.lg)
                    .fillMaxWidth(),
                selectedAccountType = AccountType.CREDIT,
                onAccountTypeChange = {},
            )
        }
    }
}
