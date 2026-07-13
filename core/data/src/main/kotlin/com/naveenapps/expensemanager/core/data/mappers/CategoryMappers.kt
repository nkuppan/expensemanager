package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.data.R
import com.naveenapps.expensemanager.core.database.entity.CategoryEntity
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.StoredIcon

fun Category.toEntityModel(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type,
        iconName = storedIcon.name,
        iconBackgroundColor = storedIcon.backgroundColor,
        createdOn = createdOn,
        updatedOn = updatedOn,
        defaultCategoryKey = titleResId.toDefaultCategoryKey(),
    )
}

fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        type = type,
        storedIcon = StoredIcon(
            name = iconName,
            backgroundColor = iconBackgroundColor,
        ),
        createdOn = createdOn,
        updatedOn = updatedOn,
        titleResId = defaultCategoryKey.toCategoryTitleResId(),
    )
}

/**
 * Maps the stable key stored for a built-in category (see `MIGRATION_4_5`) to the string
 * resource that should be shown for it, so the display name follows the app's selected
 * language. Returns null for user-created categories (no key stored) so their raw [Category.name]
 * is shown unchanged.
 */
internal fun String?.toCategoryTitleResId(): Int? = when (this) {
    "clothing" -> R.string.category_clothing
    "entertainment" -> R.string.category_entertainment
    "food" -> R.string.category_food
    "health" -> R.string.category_health
    "leisure" -> R.string.category_leisure
    "shopping" -> R.string.category_shopping
    "transportation" -> R.string.category_transportation
    "utilities" -> R.string.category_utilities
    "salary" -> R.string.category_salary
    "gift" -> R.string.category_gift
    "coupons" -> R.string.category_coupons
    else -> null
}

/** The reverse of [toCategoryTitleResId], used when persisting a [Category] back to the DB. */
internal fun Int?.toDefaultCategoryKey(): String? = when (this) {
    R.string.category_clothing -> "clothing"
    R.string.category_entertainment -> "entertainment"
    R.string.category_food -> "food"
    R.string.category_health -> "health"
    R.string.category_leisure -> "leisure"
    R.string.category_shopping -> "shopping"
    R.string.category_transportation -> "transportation"
    R.string.category_utilities -> "utilities"
    R.string.category_salary -> "salary"
    R.string.category_gift -> "gift"
    R.string.category_coupons -> "coupons"
    else -> null
}
