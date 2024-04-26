package com.naveenapps.expensemanager.core.designsystem.ui.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.naveenapps.expensemanager.core.common.R

@ColorInt
fun getColorValue(colorValue: String?): Int {
    return runCatching {
       Color.parseColor(if (colorValue?.isNotEmpty() == true) colorValue else "#000000")
    }.getOrNull() ?: Color.BLACK
}

@Composable
fun getIncomeColor() = colorResource(id = R.color.green_500)

@Composable
fun getExpenseColor() = colorResource(id = R.color.red_500)

@Composable
fun getBalanceColor() = colorResource(id = R.color.black_100)

@Composable
fun getIncomeBGColor() = getIncomeColor().copy(alpha = .1f)

@Composable
fun getExpenseBGColor() = getExpenseColor().copy(alpha = .1f)

@Composable
fun getBalanceBGColor() = getBalanceColor()

@Composable
fun getSelectedBGColor() = getIncomeColor().copy(alpha = .1f)
