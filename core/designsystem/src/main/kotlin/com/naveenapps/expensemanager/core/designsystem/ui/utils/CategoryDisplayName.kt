package com.naveenapps.expensemanager.core.designsystem.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.common.utils.defaultCategoryTitleRes
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.PieChartData
import com.naveenapps.expensemanager.core.model.TransactionUiItem

/*
 * The one way to show a category's name. Built-in categories (with a defaultKey) are shown in the
 * app's language; user-created ones show their stored name exactly as typed. When the UI moves to
 * Compose Multiplatform resources, only these functions change.
 */

@Composable
fun Category.displayName(): String = categoryName(defaultKey, name)

@Composable
fun TransactionUiItem.categoryDisplayName(): String = categoryName(categoryDefaultKey, categoryName)

@Composable
fun PieChartData.displayName(): String = categoryName(categoryDefaultKey, name)

@Composable
private fun categoryName(defaultKey: String?, name: String): String = defaultCategoryTitleRes(defaultKey)?.let { stringResource(it) } ?: name
