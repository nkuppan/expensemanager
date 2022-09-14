package com.nkuppan.expensemanager.core.model

import java.io.Serializable

data class Theme(
    val mode: Int,
    val titleResId: Int
) : Serializable