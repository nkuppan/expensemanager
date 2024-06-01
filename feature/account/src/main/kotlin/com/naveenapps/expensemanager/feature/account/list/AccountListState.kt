package com.naveenapps.expensemanager.feature.account.list

import com.naveenapps.expensemanager.core.model.AccountUiModel

data class AccountListState(
    val showReOrder: Boolean,
    val accounts: List<AccountUiModel>
)