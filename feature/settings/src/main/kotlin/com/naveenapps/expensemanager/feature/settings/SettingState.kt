package com.naveenapps.expensemanager.feature.settings

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AppLocale
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Theme

@Stable
data class SettingState(
    val currency: Currency,
    val theme: Theme?,
    val locale: AppLocale? = null,
    val showThemeSelection: Boolean,
    val showLanguageSelection: Boolean = false,
    // Defaults section (moved here from AdvancedSettingState so it's reachable from the main
    // Settings screen instead of being buried inside Advanced).
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val expenseCategories: List<Category> = emptyList(),
    val selectedExpenseCategory: Category? = null,
    val incomeCategories: List<Category> = emptyList(),
    val selectedIncomeCategory: Category? = null,
    val isCompactSummary: Boolean = false,
    // Security section
    val isAppLockEnabled: Boolean = false,
    // Data & Backup section
    val showRestoreConfirmation: Boolean = false,
    val isBackupInProgress: Boolean = false,
    // Non-null while the backup/restore passphrase dialog is showing.
    val passphrasePrompt: PassphrasePrompt? = null,
    // Developer section — debug builds only (see DemoDataRepository).
    val isDemoDataAvailable: Boolean = false,
    val showDemoDataConfirmation: Boolean = false,
    val isDemoDataLoading: Boolean = false,
)

enum class PassphrasePrompt {
    /** Optional passphrase before creating a backup; empty means the built-in key. */
    BACKUP,

    /** The chosen backup is passphrase-protected; required to restore it. */
    RESTORE,
}
