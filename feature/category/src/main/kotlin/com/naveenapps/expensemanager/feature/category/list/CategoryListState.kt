package com.naveenapps.expensemanager.feature.category.list

import com.naveenapps.expensemanager.core.model.Category

data class CategoryListState(
    val categories: List<Category>,
    val filteredCategories: List<Category>,
    val selectedTab: CategoryTabItems,
    val tabs: List<CategoryTabItems>
)