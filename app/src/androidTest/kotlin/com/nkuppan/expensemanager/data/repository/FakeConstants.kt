package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.AccountType
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import java.util.Date

val FAKE_CATEGORY = Category(
    id = "1",
    name = "Test",
    type = CategoryType.EXPENSE,
    iconName = "#FFFFFF",
    backgroundColor = "#FFFFFF",
    createdOn = Date(),
    updatedOn = Date()
)

val FAKE_FAVORITE_CATEGORY = Category(
    id = "1",
    name = "Test",
    type = CategoryType.EXPENSE,
    iconName = "#FFFFFF",
    backgroundColor = "#FFFFFF",
    createdOn = Date(),
    updatedOn = Date()
)

val FAKE_ACCOUNT = Account(
    id = "1",
    name = "Test",
    type = AccountType.CASH,
    iconName = "",
    iconBackgroundColor = "#FFFFFF",
    createdOn = Date(),
    updatedOn = Date()
)