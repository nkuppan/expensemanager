package com.naveenapps.expensemanager.feature.category.list

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.Category

@Stable
data class CategoryListState(
    val categories: List<Category>,
    val filteredCategories: List<Category>,
    val selectedTab: CategoryTabItems,
    val tabs: List<CategoryTabItems>
)