package com.nkuppan.expensemanager.presentation.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.nkuppan.expensemanager.R

enum class HomeScreenBottomBarItems(
    @StringRes val labelResourceID: Int,
    @DrawableRes val iconResourceID: Int
) {
    Home(R.string.title_home, R.drawable.ic_home),
    Analysis(R.string.analysis, R.drawable.ic_chart),
    Transaction(R.string.transaction, R.drawable.ic_transfer),
    Category(R.string.category, R.drawable.ic_category),
}
