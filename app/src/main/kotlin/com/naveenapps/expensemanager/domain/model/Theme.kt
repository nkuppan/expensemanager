package com.naveenapps.expensemanager.domain.model

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate.NightMode

data class Theme(
    @NightMode val mode: Int,
    @StringRes val titleResId: Int
)