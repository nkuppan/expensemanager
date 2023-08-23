package com.nkuppan.expensemanager.core.model

import java.io.Serializable
import java.util.*

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val isFavorite: Boolean,
    val backgroundColor: String,
    val createdOn: Date,
    val updatedOn: Date
) : Serializable