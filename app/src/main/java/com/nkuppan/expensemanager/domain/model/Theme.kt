package com.nkuppan.expensemanager.domain.model

import androidx.annotation.StringRes
import java.io.Serializable

data class Theme(
    val mode: Int,
    @StringRes val titleResId: Int
) : Serializable