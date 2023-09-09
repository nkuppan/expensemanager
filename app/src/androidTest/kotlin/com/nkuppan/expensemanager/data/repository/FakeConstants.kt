package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.domain.model.Account
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.PaymentMode
import java.util.Date

val FAKE_CATEGORY = Category(
    id = "1",
    name = "Test",
    type = CategoryType.EXPENSE,
    isFavorite = false,
    backgroundColor = "#FFFFFF",
    createdOn = Date(),
    updatedOn = Date()
)

val FAKE_FAVORITE_CATEGORY = Category(
    id = "1",
    name = "Test",
    type = CategoryType.EXPENSE,
    isFavorite = true,
    backgroundColor = "#FFFFFF",
    createdOn = Date(),
    updatedOn = Date()
)

val FAKE_ACCOUNT = Account(
    id = "1",
    name = "Test",
    type = PaymentMode.WALLET,
    backgroundColor = "#FFFFFF",
    createdOn = Date(),
    updatedOn = Date()
)