package com.naveenapps.expensemanager.feature.account.reorder

import com.naveenapps.expensemanager.core.model.Account

data class AccountReOrderState(
    val accounts: List<Account>,
    val showSaveButton: Boolean
)