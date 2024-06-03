package com.naveenapps.expensemanager.feature.settings

import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Theme

data class SettingState(
    val currency: Currency,
    val theme: Theme?,
    val showThemeSelection: Boolean
)