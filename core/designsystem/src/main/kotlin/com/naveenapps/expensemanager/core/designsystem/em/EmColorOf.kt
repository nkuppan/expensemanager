package com.naveenapps.expensemanager.core.designsystem.em

import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.naveenapps.expensemanager.core.common.R as CommonR
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.model.AmountTone
import com.naveenapps.expensemanager.core.model.BudgetStatus

/**
 * Bridge for code that still passes a legacy core:common colour resource: maps it to the theme
 * token so it follows light/dark. Models carry a meaning instead ([AmountTone], [BudgetStatus]).
 */
@Composable
@ReadOnlyComposable
fun emColorOf(@ColorRes colorRes: Int): Color {
    val colors = EmTheme.colors
    return when (colorRes) {
        CommonR.color.red_500 -> colors.expense
        CommonR.color.green_500 -> colors.income
        CommonR.color.orange_500 -> colors.warning
        CommonR.color.blue_500 -> colors.info
        CommonR.color.light_green_500 -> colors.budgetWatch
        0 -> colors.scheme.onSurface
        else -> colorResource(colorRes)
    }
}

/** Colour for an amount's meaning: income green for money in (or zero), expense red for money out. */
@Composable
@ReadOnlyComposable
fun emColorOf(tone: AmountTone): Color = when (tone) {
    AmountTone.POSITIVE -> EmTheme.colors.income
    AmountTone.NEGATIVE -> EmTheme.colors.expense
}

/** Colour for a budget's [BudgetStatus]: the budget tokens (green, light green, orange, red). */
@Composable
@ReadOnlyComposable
fun emColorOf(status: BudgetStatus): Color = when (status) {
    BudgetStatus.SAFE -> EmTheme.colors.budgetSafe
    BudgetStatus.WATCH -> EmTheme.colors.budgetWatch
    BudgetStatus.RISK -> EmTheme.colors.budgetRisk
    BudgetStatus.OVER -> EmTheme.colors.budgetOver
}
