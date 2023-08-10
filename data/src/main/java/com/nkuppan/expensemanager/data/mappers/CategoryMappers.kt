package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.data.db.entity.CategoryEntity

fun Category.toEntityModel(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type.ordinal,
        isFavorite = isFavorite,
        backgroundColor = backgroundColor,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        type = CategoryType.values()[type],
        isFavorite = isFavorite,
        backgroundColor = backgroundColor,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}