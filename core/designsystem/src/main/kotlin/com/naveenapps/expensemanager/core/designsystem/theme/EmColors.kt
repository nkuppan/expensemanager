package com.naveenapps.expensemanager.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Every colour a feature may use: the Material [scheme] plus the money and status colours
 * Material has no slot for. Replaces colorResource(R.color.*) and hex literals in features.
 */
@Immutable
data class EmColors(
    val scheme: ColorScheme,
    val income: Color,
    val expense: Color,
    val transfer: Color,
    val debt: Color,
    val warning: Color,
    val info: Color,
    /** Budget progress, by how much of the budget is spent (see budgetProgressColor). */
    val budgetSafe: Color,
    val budgetWatch: Color,
    val budgetRisk: Color,
    val budgetOver: Color,
    /** Branded hero surfaces (onboarding, share banner). */
    val brandSurface: Color,
    val onBrandSurface: Color,
    val isDark: Boolean,
)

/** The 12% tinted background used behind icons in icon tiles and status badges. */
fun Color.tint(): Color = copy(alpha = EmAlpha().subtle)

private val DebtPurple = Color(0xFF7E57C2)
private val DebtPurpleDark = Color(0xFFB39DDB)
private val BudgetWatchDark = Color(0xFFCDDC39)

// Light theme: #CDDC39 is 1.5:1 on white, too faint for a progress bar; this is 3.1:1.
private val BudgetWatchLight = Color(0xFF8C9A1B)

internal fun emColors(isDark: Boolean): EmColors {
    val scheme = if (isDark) ExpenseManagerDarkColorScheme else ExpenseManagerLightColorScheme
    val extended = if (isDark) DarkExtendedColors else LightExtendedColors
    return EmColors(
        scheme = scheme,
        income = extended.income,
        expense = extended.expense,
        transfer = extended.info,
        debt = if (isDark) DebtPurpleDark else DebtPurple,
        warning = extended.warning,
        info = extended.info,
        budgetSafe = extended.income,
        budgetWatch = if (isDark) BudgetWatchDark else BudgetWatchLight,
        budgetRisk = extended.warning,
        budgetOver = extended.expense,
        brandSurface = scheme.primaryContainer,
        onBrandSurface = scheme.onPrimaryContainer,
        isDark = isDark,
    )
}
