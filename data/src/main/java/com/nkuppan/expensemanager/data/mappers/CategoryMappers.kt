package com.nkuppan.expensemanager.data.mappers

import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.data.db.entity.CategoryEntity

class CategoryDomainEntityMapper : Mapper<Category, CategoryEntity> {
    override fun convert(fromObject: Category): CategoryEntity {
        return CategoryEntity(
            id = fromObject.id,
            name = fromObject.name,
            type = fromObject.type.ordinal,
            isFavorite = fromObject.isFavorite,
            backgroundColor = fromObject.backgroundColor,
            createdOn = fromObject.createdOn,
            updatedOn = fromObject.updatedOn,
        )
    }
}

class CategoryEntityDomainMapper : Mapper<CategoryEntity, Category> {
    override fun convert(fromObject: CategoryEntity): Category {
        return Category(
            id = fromObject.id,
            name = fromObject.name,
            type = CategoryType.values()[fromObject.type],
            isFavorite = fromObject.isFavorite,
            backgroundColor = fromObject.backgroundColor,
            createdOn = fromObject.createdOn,
            updatedOn = fromObject.updatedOn,
        )
    }
}