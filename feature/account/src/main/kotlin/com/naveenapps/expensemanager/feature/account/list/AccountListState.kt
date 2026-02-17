package com.naveenapps.expensemanager.feature.account.list

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel

@Stable
data class AccountListState(
    val showReOrder: Boolean,
    val accounts: List<AccountUiModel>
)