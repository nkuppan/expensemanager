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
    // Set only when editing one of the built-in default categories (see Category.defaultKey).
    // Its name field shows the localized string below and can't be renamed, since the stored
    // `name` is just the original English seed value used as a lookup key, not real user text.
    val nameResId: Int? = null,
    // Absolute path to a user-picked/captured photo (see StoredIcon.customImagePath). When set,
    // the Appearance section shows this photo instead of the icon+color combination above.
    val customImagePath: String? = null,
)

val CategoryCreateState.isDefaultCategory: Boolean get() = nameResId != null
