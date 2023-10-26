package com.nkuppan.expensemanager.presentation.account.create

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.AccountType
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme


@Composable
fun AccountTypeSelectionView(
    selectedAccountType: AccountType,
    onAccountTypeChange: ((AccountType) -> Unit),
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CustomAccountFilterChip(
            selectedAccountType = selectedAccountType,
            accountType = AccountType.REGULAR,
            filterName = stringResource(id = R.string.bank_account),
            filterIcon = R.drawable.account_balance,
            filterSelectedColor = R.color.green_500,
            onAccountTypeChange = onAccountTypeChange,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        CustomAccountFilterChip(
            selectedAccountType = selectedAccountType,
            accountType = AccountType.CREDIT,
            filterName = stringResource(id = R.string.credit),
            filterIcon = R.drawable.credit_card,
            filterSelectedColor = R.color.green_500,
            onAccountTypeChange = onAccountTypeChange,
            modifier = Modifier
                .align(Alignment.CenterVertically)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RowScope.CustomAccountFilterChip(
    selectedAccountType: AccountType,
    accountType: AccountType,
    filterName: String,
    @DrawableRes filterIcon: Int,
    @ColorRes filterSelectedColor: Int,
    onAccountTypeChange: ((AccountType) -> Unit),
    modifier: Modifier = Modifier,
) {
    FilterChip(
        modifier = modifier,
        selected = selectedAccountType == accountType,
        onClick = {
            onAccountTypeChange.invoke(accountType)
        },
        label = {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = filterName
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = filterIcon),
                contentDescription = ""
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedLabelColor = Color.White,
            selectedContainerColor = colorResource(id = filterSelectedColor),
            selectedLeadingIconColor = Color.White
        )
    )
}

@Preview
@Composable
private fun AccountTypeSelectionViewPreview() {
    ExpenseManagerTheme {
        Column {
            AccountTypeSelectionView(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                selectedAccountType = AccountType.REGULAR,
                onAccountTypeChange = {}
            )
            AccountTypeSelectionView(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                selectedAccountType = AccountType.CREDIT,
                onAccountTypeChange = {}
            )
        }
    }
}
