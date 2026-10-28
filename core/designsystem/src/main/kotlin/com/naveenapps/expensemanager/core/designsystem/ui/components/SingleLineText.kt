package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/**
 * Text that always stays on one line: it shrinks (down to [minFontSize]) instead of wrapping
 * letter by letter in narrow slots such as bottom-bar labels and buttons, which happened with
 * large font sizes ("Transac / tion", "D / o / n / e"). Ellipsized only if even that won't fit.
 */
@Composable
fun SingleLineText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    minFontSize: TextUnit = 9.sp,
) {
    val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }
    val maxFontSize = if (style.fontSize.isSpecified) style.fontSize else DEFAULT_FONT_SIZE
    val minSize = if (minFontSize.value < maxFontSize.value) minFontSize else maxFontSize
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = textColor),
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(minFontSize = minSize, maxFontSize = maxFontSize, stepSize = 0.5.sp),
    )
}

private val DEFAULT_FONT_SIZE = 14.sp
