package com.nkuppan.expensemanager.domain.model

import java.io.Serializable

data class Theme(
    val mode: Int,
    val titleResId: Int
) : Serializable