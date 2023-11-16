package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import java.util.Date

val FAKE_CATEGORY = Category(
    id = "1",
    name = "Test",
    type = CategoryType.EXPENSE,
    iconName = "#FFFFFF",
    iconBackgroundColor = "#FFFFFF",
    createdOn = Date(),
    updatedOn = Date()
)

val FAKE_FAVORITE_CATEGORY = Category(
    id = "1",
    name = "Test",
    type = CategoryType.EXPENSE,
    iconName = "#FFFFFF",
    iconBackgroundColor = "#FFFFFF",
    createdOn = Date(),
    updatedOn = Date()
)

val FAKE_ACCOUNT = Account(
    id = "1",
    name = "Test",
    type = AccountType.REGULAR,
    iconName = "",
    iconBackgroundColor = "#FFFFFF",
    createdOn = Date(),
    updatedOn = Date()
)