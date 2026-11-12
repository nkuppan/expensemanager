package com.naveenapps.expensemanager.feature.settings

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AppLocale
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Theme

@Stable
data class SettingState(
    val currency: Currency,
    val theme: Theme?,
    val locale: AppLocale? = null,
    val showThemeSelection: Boolean,
    val showLanguageSelection: Boolean = false,
    // Security section
    val isAppLockEnabled: Boolean = false,
    val isScreenshotBlocked: Boolean = false,
    // Developer section — debug builds only (see DemoDataRepository).
    val isDemoDataAvailable: Boolean = false,
    val showDemoDataConfirmation: Boolean = false,
    val isDemoDataLoading: Boolean = false,
)
