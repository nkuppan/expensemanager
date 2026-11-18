package com.naveenapps.expensemanager.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalEmColors = staticCompositionLocalOf { emColors(isDark = false) }
internal val LocalEmTypography = staticCompositionLocalOf<EmTypography> {
    error("EmTypography not provided: wrap content in ExpenseManagerTheme")
}
internal val LocalEmSpacing = staticCompositionLocalOf { EmSpacing() }
internal val LocalEmShapes = staticCompositionLocalOf { EmShapes() }
internal val LocalEmIconSize = staticCompositionLocalOf { EmIconSize() }
internal val LocalEmElevation = staticCompositionLocalOf { EmElevation() }
internal val LocalEmMotion = staticCompositionLocalOf { EmMotion() }
internal val LocalEmAlpha = staticCompositionLocalOf { EmAlpha() }

/**
 * The single entry point to design tokens. Features read tokens from here (never from
 * MaterialTheme directly, never as literals). Provided by [ExpenseManagerTheme].
 */
object EmTheme {
    val colors: EmColors
        @Composable @ReadOnlyComposable
        get() = LocalEmColors.current

    val type: EmTypography
        @Composable @ReadOnlyComposable
        get() = LocalEmTypography.current

    val spacing: EmSpacing
        @Composable @ReadOnlyComposable
        get() = LocalEmSpacing.current

    val shapes: EmShapes
        @Composable @ReadOnlyComposable
        get() = LocalEmShapes.current

    val icons: EmIconSize
        @Composable @ReadOnlyComposable
        get() = LocalEmIconSize.current

    val elevation: EmElevation
        @Composable @ReadOnlyComposable
        get() = LocalEmElevation.current

    val motion: EmMotion
        @Composable @ReadOnlyComposable
        get() = LocalEmMotion.current

    val alpha: EmAlpha
        @Composable @ReadOnlyComposable
        get() = LocalEmAlpha.current
}
