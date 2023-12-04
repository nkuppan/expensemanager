package com.naveenapps.expensemanager.core.designsystem.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * Returns `true` if dark theme should be used, as a function of the [theme] and the
 * current system context.
 */
@Composable
fun shouldUseDarkTheme(theme: Int): Boolean = when (theme) {
    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> isSystemInDarkTheme()
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> {
        isSystemInDarkTheme()
    }
}
