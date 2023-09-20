package com.nkuppan.expensemanager.domain.model

import androidx.annotation.StringRes

data class Theme(
    val mode: Int,
    @StringRes val titleResId: Int
)