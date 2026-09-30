package com.naveenapps.expensemanager.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsTheme

/*
 * Colour tokens mirrored 1:1 from the marketing website
 * (expensemanager-web/src/app/globals.css) so the app and
 * https://expensemanager.naveenapps.com share one visual identity.
 */

// ---- Dark (website default) ----
private val DarkBg = Color(0xFF0E0F14)
private val DarkBg2 = Color(0xFF14161F)
private val DarkBg3 = Color(0xFF1C1E29)
private val DarkBgCard = Color(0xFF181A24)
private val DarkAccent = Color(0xFF22C97A)
private val DarkAccent2 = Color(0xFF16A35F)
private val DarkInk = Color(0xFFF0F1F5)
private val DarkInk2 = Color(0xFFA8AABC)
private val DarkInk3 = Color(0xFF6B6E85)
private val DarkInk4 = Color(0xFF3A3C4D)
private val DarkRed = Color(0xFFF0616D)
private val DarkAmber = Color(0xFFF5A623)
private val DarkBlue = Color(0xFF6D93FF)

// ---- Light ----
private val LightBg = Color(0xFFF4F5F9)
private val LightBg2 = Color(0xFFECEDF3)
private val LightBg3 = Color(0xFFE3E5EE)
private val LightBgCard = Color(0xFFFFFFFF)
private val LightAccent = Color(0xFF0F9D5B)
private val LightAccent2 = Color(0xFF0B7A46)
private val LightInk = Color(0xFF14161C)
private val LightInk2 = Color(0xFF565A6E)
private val LightInk3 = Color(0xFF868AA0)
private val LightInk4 = Color(0xFFC7CADA)
private val LightRed = Color(0xFFD93A46)
private val LightAmber = Color(0xFFB9790A)
private val LightBlue = Color(0xFF3E63D6)

val ExpenseManagerDarkColorScheme: ColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color(0xFF062014),
    primaryContainer = Color(0xFF123A28),
    onPrimaryContainer = Color(0xFF9FF0C8),
    inversePrimary = LightAccent,
    secondary = DarkAccent2,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF123A28),
    onSecondaryContainer = Color(0xFF9FF0C8),
    tertiary = DarkBlue,
    onTertiary = DarkBg,
    tertiaryContainer = Color(0xFF1F2A4D),
    onTertiaryContainer = Color(0xFFC9D6FF),
    error = DarkRed,
    onError = DarkBg,
    errorContainer = Color(0xFF3D1C22),
    onErrorContainer = Color(0xFFFFB3B9),
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkBg,
    onSurface = DarkInk,
    surfaceVariant = DarkBg3,
    onSurfaceVariant = DarkInk2,
    surfaceTint = DarkAccent,
    inverseSurface = DarkInk,
    inverseOnSurface = LightInk,
    outline = DarkInk3,
    outlineVariant = Color(0xFF262833),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF262836),
    surfaceDim = DarkBg,
    surfaceContainerLowest = DarkBgCard, // cards
    surfaceContainerLow = DarkBgCard,
    surfaceContainer = DarkBg2,
    surfaceContainerHigh = DarkBg3,
    surfaceContainerHighest = Color(0xFF242634),
)

val ExpenseManagerLightColorScheme: ColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDF2E7),
    onPrimaryContainer = Color(0xFF053D22),
    inversePrimary = DarkAccent,
    secondary = LightAccent2,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDF2E7),
    onSecondaryContainer = Color(0xFF053D22),
    tertiary = LightBlue,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE2E8FA),
    onTertiaryContainer = Color(0xFF172B6B),
    error = LightRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFBE3E5),
    onErrorContainer = Color(0xFF5C0F16),
    background = LightBg,
    onBackground = LightInk,
    surface = LightBg,
    onSurface = LightInk,
    surfaceVariant = LightBg3,
    onSurfaceVariant = LightInk2,
    surfaceTint = LightAccent,
    inverseSurface = LightInk,
    inverseOnSurface = DarkInk,
    outline = LightInk3,
    outlineVariant = Color(0xFFE3E5EC),
    scrim = Color(0xFF000000),
    surfaceBright = LightBgCard,
    surfaceDim = Color(0xFFDDE0EA),
    surfaceContainerLowest = LightBgCard, // cards
    surfaceContainerLow = LightBgCard,
    surfaceContainer = LightBgCard, // bottom bar
    surfaceContainerHigh = LightBg3,
    surfaceContainerHighest = Color(0xFFDDE0EA),
)

/** Semantic money colours that Material's ColorScheme has no slot for. */
@Immutable
data class ExpenseManagerExtendedColors(
    val income: Color,
    val expense: Color,
    val warning: Color,
    val info: Color,
)

val LightExtendedColors = ExpenseManagerExtendedColors(
    income = LightAccent,
    expense = LightRed,
    warning = LightAmber,
    info = LightBlue,
)

val DarkExtendedColors = ExpenseManagerExtendedColors(
    income = DarkAccent,
    expense = DarkRed,
    warning = DarkAmber,
    info = DarkBlue,
)

val LocalExpenseManagerColors = staticCompositionLocalOf { LightExtendedColors }

/**
 * App theme. Keeps the typography and shapes from [NaveenAppsTheme] and swaps in
 * the website colour scheme.
 */
@Composable
fun ExpenseManagerTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    NaveenAppsTheme(isDarkTheme = isDarkTheme) {
        CompositionLocalProvider(
            LocalExpenseManagerColors provides if (isDarkTheme) DarkExtendedColors else LightExtendedColors,
        ) {
            MaterialTheme(
                colorScheme = if (isDarkTheme) ExpenseManagerDarkColorScheme else ExpenseManagerLightColorScheme,
                typography = MaterialTheme.typography,
                shapes = MaterialTheme.shapes,
                content = content,
            )
        }
    }
}

/** Drop-in replacement for ExpenseManagerPreviewTheme that previews the website colours. */
@Composable
fun ExpenseManagerPreviewTheme(
    padding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    ExpenseManagerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    }
}
