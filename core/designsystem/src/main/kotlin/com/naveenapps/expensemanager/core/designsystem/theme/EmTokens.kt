package com.naveenapps.expensemanager.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale (4 dp grid). Features read these through [EmTheme.spacing] instead of
 * writing `n.dp` for padding and gaps.
 */
@Immutable
data class EmSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
) {
    /** Horizontal padding between the screen edge and content. */
    val screenGutter: Dp get() = lg

    /** Gap between items in a list or a stack of cards. */
    val listItemGap: Dp get() = sm
}

/** Corner radii. Also fed into MaterialTheme.shapes so wrapped Material components match. */
@Immutable
data class EmShapes(
    val xs: Shape = RoundedCornerShape(4.dp),
    val sm: Shape = RoundedCornerShape(8.dp),
    val md: Shape = RoundedCornerShape(12.dp),
    val lg: Shape = RoundedCornerShape(16.dp),
    val xl: Shape = RoundedCornerShape(28.dp),
    val pill: Shape = CircleShape,
    /** Top-only corners for bottom sheets. */
    val sheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
)

@Immutable
data class EmIconSize(
    /** Inline with text. */
    val sm: Dp = 16.dp,
    /** Row trailing icons, chips. */
    val md: Dp = 20.dp,
    /** Default: top bar, navigation, buttons. */
    val lg: Dp = 24.dp,
    /** Container of an icon tile (holds an md/lg icon). */
    val tile: Dp = 40.dp,
    /** Empty states and onboarding. */
    val hero: Dp = 72.dp,
)

@Immutable
data class EmElevation(
    val none: Dp = 0.dp,
    val raised: Dp = 1.dp,
    val overlay: Dp = 3.dp,
)

@Immutable
data class EmMotion(
    val shortMillis: Int = 150,
    val mediumMillis: Int = 250,
    val longMillis: Int = 400,
    val easing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
)

/** The only alpha values used on colours. */
@Immutable
data class EmAlpha(
    val disabled: Float = 0.38f,
    val subtle: Float = 0.12f,
    val muted: Float = 0.6f,
    val scrim: Float = 0.32f,
)

internal val EmMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
