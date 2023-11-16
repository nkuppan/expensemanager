package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.CategoryEntity
import com.naveenapps.expensemanager.core.model.Category

fun Category.toEntityModel(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type,
        iconName = iconName,
        iconBackgroundColor = iconBackgroundColor,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        type = type,
        iconName = iconName,
        iconBackgroundColor = iconBackgroundColor,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}