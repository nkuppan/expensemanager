package com.naveenapps.expensemanager.core.designsystem.em

import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.naveenapps.expensemanager.core.common.R as CommonR
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.model.AmountTone

/**
 * Bridge for models that still carry a colour resource (budgetProgressColor): maps the legacy core:common colours to theme tokens so they follow
 * light/dark. Goes away when those models carry a meaning (income/expense/status) instead.
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
