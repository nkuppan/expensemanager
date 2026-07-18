package com.naveenapps.expensemanager.feature.settings.advanced

// Defaults, Backup & Restore, and App Lock/Compact Summary moved to the main SettingsState —
// they're common enough that hiding them behind "Advanced" made them hard to find. This screen
// now only holds the genuinely rare, set-once-and-forget items.
data class AdvancedSettingState(
    val showDateFilter: Boolean = false,
)
