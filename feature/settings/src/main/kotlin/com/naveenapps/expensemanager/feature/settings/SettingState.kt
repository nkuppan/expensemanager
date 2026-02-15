package com.naveenapps.expensemanager.feature.settings

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Theme

@Stable
data class SettingState(
    val currency: Currency,
    val theme: Theme?,
    val showThemeSelection: Boolean
)