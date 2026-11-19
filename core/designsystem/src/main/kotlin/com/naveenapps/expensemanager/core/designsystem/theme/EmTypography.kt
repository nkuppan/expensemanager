package com.naveenapps.expensemanager.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography roles, named by job. A feature picks a role; it never sets fontWeight or fontSize.
 */
@Immutable
data class EmTypography(
    /** Big amounts: balance, Add-screen amount, debt totals. */
    val display: TextStyle,
    /** Top bar and sheet titles. */
    val screenTitle: TextStyle,
    /** Section and card headers. */
    val sectionTitle: TextStyle,
    /** Primary text of a list row. */
    val itemTitle: TextStyle,
    /** Paragraphs, messages, descriptions. */
    val body: TextStyle,
    /** Secondary row text, helper text, dates. */
    val supporting: TextStyle,
    /** Buttons, chips, tabs. */
    val label: TextStyle,
    /** Uppercase group captions and badges. */
    val overline: TextStyle,
    val amountLarge: TextStyle,
    val amountMedium: TextStyle,
    val amountSmall: TextStyle,
    /** Monospace small text (licences, codes). */
    val code: TextStyle,
)

private const val TABULAR_FIGURES = "tnum"

/**
 * App-owned Material type scale: Material 3 sizes with the app's weights. The font family is
 * kept from [base] (the naveenapps library theme).
 */
internal fun emMaterialTypography(base: Typography): Typography {
    val family = base.bodyLarge.fontFamily
    fun style(size: Int, line: Int, weight: FontWeight, tracking: Double) = TextStyle(
        fontFamily = family,
        fontSize = size.sp,
        lineHeight = line.sp,
        fontWeight = weight,
        letterSpacing = tracking.sp,
    )
    return Typography(
        displayLarge = style(57, 64, FontWeight.Normal, -0.25),
        displayMedium = style(45, 52, FontWeight.Normal, 0.0),
        displaySmall = style(36, 44, FontWeight.Normal, 0.0),
        headlineLarge = style(32, 40, FontWeight.SemiBold, 0.0),
        headlineMedium = style(28, 36, FontWeight.SemiBold, 0.0),
        headlineSmall = style(24, 32, FontWeight.SemiBold, 0.0),
        titleLarge = style(22, 28, FontWeight.SemiBold, 0.0),
        titleMedium = style(16, 24, FontWeight.SemiBold, 0.15),
        titleSmall = style(14, 20, FontWeight.SemiBold, 0.1),
        bodyLarge = style(16, 24, FontWeight.Normal, 0.5),
        bodyMedium = style(14, 20, FontWeight.Normal, 0.25),
        bodySmall = style(12, 16, FontWeight.Normal, 0.4),
        labelLarge = style(14, 20, FontWeight.Medium, 0.1),
        labelMedium = style(12, 16, FontWeight.Medium, 0.5),
        labelSmall = style(11, 16, FontWeight.Medium, 0.5),
    )
}

internal fun emTypography(material: Typography): EmTypography = EmTypography(
    display = material.headlineMedium,
    screenTitle = material.titleLarge,
    sectionTitle = material.titleSmall,
    itemTitle = material.bodyLarge.copy(fontWeight = FontWeight.Medium),
    body = material.bodyMedium,
    supporting = material.bodySmall,
    label = material.labelLarge,
    overline = material.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
    amountLarge = material.headlineMedium.copy(fontFeatureSettings = TABULAR_FIGURES),
    amountMedium = material.titleMedium.copy(fontFeatureSettings = TABULAR_FIGURES),
    amountSmall = material.bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    code = material.bodySmall.copy(fontFamily = FontFamily.Monospace),
)
