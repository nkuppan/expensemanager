package com.naveenapps.expensemanager.feature.category.create

import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.TextFieldValue

data class CategoryCreateState(
    val name: TextFieldValue<String>,
    val color: TextFieldValue<String>,
    val icon: TextFieldValue<String>,
    val type: TextFieldValue<CategoryType>,
    val showDeleteButton: Boolean,
    val showDeleteDialog: Boolean,
)
