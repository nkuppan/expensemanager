package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.data.db.entity.CategoryEntity
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType

fun Category.toEntityModel(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type.ordinal,
        iconName = iconName,
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
        iconName = iconName,
        backgroundColor = backgroundColor,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}