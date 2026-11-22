package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.CategoryEntity
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.StoredIcon

fun Category.toEntityModel(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    type = type,
    iconName = storedIcon.name,
    iconBackgroundColor = storedIcon.backgroundColor,
    createdOn = createdOn,
    updatedOn = updatedOn,
    defaultCategoryKey = defaultKey,
    customImagePath = storedIcon.customImagePath,
)

fun CategoryEntity.toDomainModel(): Category = Category(
    id = id,
    name = name,
    type = type,
    storedIcon = StoredIcon(
        name = iconName,
        backgroundColor = iconBackgroundColor,
        customImagePath = customImagePath,
    ),
    createdOn = createdOn,
    updatedOn = updatedOn,
    defaultKey = defaultCategoryKey,
)
