package com.nkuppan.expensemanager.domain.model

import java.io.Serializable
import java.util.Date

data class Account(
    val id: String,
    val name: String,
    val type: PaymentMode,
    val backgroundColor: String,
    val createdOn: Date,
    val updatedOn: Date
) : Serializable