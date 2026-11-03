package com.naveenapps.expensemanager.core.designsystem.ui.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.naveenapps.expensemanager.core.common.R
import com.naveenapps.expensemanager.core.designsystem.theme.LocalExpenseManagerColors
import java.util.concurrent.ConcurrentHashMap

/** Parsed colors by hex string: rows call this for every icon on every recomposition. */
private val colorValueCache = ConcurrentHashMap<String, Int>()

@ColorInt
fun getColorValue(colorValue: String?): Int {
    val key = if (colorValue?.isNotEmpty() == true) colorValue else "#000000"
    return colorValueCache.getOrPut(key) {
        runCatching { Color.parseColor(key) }.getOrNull() ?: Color.BLACK
    }
}

@Composable
fun getIncomeColor() = LocalExpenseManagerColors.current.income

@Composable
fun getExpenseColor() = LocalExpenseManagerColors.current.expense

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
