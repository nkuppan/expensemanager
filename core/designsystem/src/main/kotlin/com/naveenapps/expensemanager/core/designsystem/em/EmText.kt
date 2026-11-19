package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/** Typography roles; see EmTypography for what each is for. */
enum class EmTextRole {
    Display,
    ScreenTitle,
    SectionTitle,
    ItemTitle,
    Body,
    Supporting,
    Label,
    Overline,

    /** Monospace text: licence texts, codes. */
    Code,
}

/** Text colours by meaning. Anything else is a new token, not a one-off colour. */
enum class EmTextColor {
    /** Inherits the container's content colour (onSurface; onPrimary inside a filled button, …). */
    Default,
    Supporting,
    Primary,
    Income,
    Expense,
    Warning,
    Error,
    OnPrimary,
    Disabled,
}

@Composable
@ReadOnlyComposable
internal fun EmTextRole.style(): TextStyle {
    val type = EmTheme.type
    return when (this) {
        EmTextRole.Display -> type.display
        EmTextRole.ScreenTitle -> type.screenTitle
        EmTextRole.SectionTitle -> type.sectionTitle
        EmTextRole.ItemTitle -> type.itemTitle
        EmTextRole.Body -> type.body
        EmTextRole.Supporting -> type.supporting
        EmTextRole.Label -> type.label
        EmTextRole.Overline -> type.overline
        EmTextRole.Code -> type.code
    }
}

@Composable
@ReadOnlyComposable
internal fun EmTextColor.color(): Color {
    val colors = EmTheme.colors
    return when (this) {
        EmTextColor.Default -> LocalContentColor.current.takeOrElse { colors.scheme.onSurface }
        EmTextColor.Supporting -> colors.scheme.onSurfaceVariant
        EmTextColor.Primary -> colors.scheme.primary
        EmTextColor.Income -> colors.income
        EmTextColor.Expense -> colors.expense
        EmTextColor.Warning -> colors.warning
        EmTextColor.Error -> colors.scheme.error
        EmTextColor.OnPrimary -> colors.scheme.onPrimary
        EmTextColor.Disabled -> colors.scheme.onSurface.copy(alpha = EmTheme.alpha.disabled)
    }
}

/**
 * The only text component features use. Pick a [role] and a [color] by meaning; size, weight and
 * tracking come from the role. Overline text is upper-cased here so callers pass normal strings.
 */
@Composable
fun EmText(
    text: String,
    modifier: Modifier = Modifier,
    role: EmTextRole = EmTextRole.Body,
    color: EmTextColor = EmTextColor.Default,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
    textDecoration: TextDecoration? = null,
) {
    EmText(
        text = text,
        color = color.color(),
        modifier = modifier,
        role = role,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        textDecoration = textDecoration,
    )
}

/**
 * [EmText] in a colour that carries data or comes from a token outside [EmTextColor]: a category's
 * own colour, EmTheme.colors.debt, onPrimaryContainer on a highlight card. Never a literal.
 */
@Composable
fun EmText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    role: EmTextRole = EmTextRole.Body,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
    textDecoration: TextDecoration? = null,
) {
    Text(
        text = if (role == EmTextRole.Overline) text.uppercase() else text,
        modifier = modifier,
        style = role.style(),
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        textDecoration = textDecoration,
    )
}

/** [EmText] for styled spans (highlighted search matches, bold parts of a sentence). */
@Composable
fun EmText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    role: EmTextRole = EmTextRole.Body,
    color: EmTextColor = EmTextColor.Default,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
) {
    Text(
        text = text,
        modifier = modifier,
        style = role.style(),
        color = color.color(),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

enum class EmAmountKind { Income, Expense, Transfer, Debt, Neutral }

/**
 * Amount sizes. The rule: one [Large] figure per card (its main number: balance, budget spent,
 * debt total, donut total); [Medium] for amounts in list rows and in small tiles; [Small] only
 * in compact rows inside Home cards (accounts strip, category legend, upcoming).
 */
enum class EmAmountSize {
    /** 28 sp: the main figure of a card. */
    Large,

    /** 16 sp: list rows and tiles. */
    Medium,

    /** 14 sp: compact rows inside Home cards. */
    Small,
}

/**
 * An already formatted amount (currency and sign come from the caller's formatter). Colour comes
 * from [kind]; digits are tabular so columns of amounts line up, and a long amount shrinks to fit
 * instead of being cut off.
 */
@Composable
fun EmAmountText(
    amount: String,
    modifier: Modifier = Modifier,
    kind: EmAmountKind = EmAmountKind.Neutral,
    size: EmAmountSize = EmAmountSize.Medium,
    textAlign: TextAlign? = null,
) {
    val colors = EmTheme.colors
    EmAmountText(
        amount = amount,
        color = when (kind) {
            EmAmountKind.Income -> colors.income
            EmAmountKind.Expense -> colors.expense
            EmAmountKind.Transfer -> colors.transfer
            EmAmountKind.Debt -> colors.debt
            EmAmountKind.Neutral -> colors.scheme.onSurface
        },
        modifier = modifier,
        size = size,
        textAlign = textAlign,
    )
}

/** [EmAmountText] in a colour that comes from data or a token (emColorOf, a category colour). */
@Composable
fun EmAmountText(
    amount: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: EmAmountSize = EmAmountSize.Medium,
    textAlign: TextAlign? = null,
) {
    val type = EmTheme.type
    val style = when (size) {
        EmAmountSize.Large -> type.amountLarge
        EmAmountSize.Medium -> type.amountMedium
        EmAmountSize.Small -> type.amountSmall
    }
    val max = style.fontSize
    BasicText(
        text = amount,
        modifier = modifier,
        style = style.copy(
            color = color.takeOrElse { LocalContentColor.current.takeOrElse { EmTheme.colors.scheme.onSurface } },
            textAlign = textAlign ?: TextAlign.Unspecified,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(minFontSize = max * MIN_FIT_SCALE, maxFontSize = max),
    )
}

/**
 * One line of text that shrinks (down to 60% of the role's size) instead of being cut off: totals
 * inside a ring, amounts in a narrow tile.
 */
@Composable
fun EmFitText(
    text: String,
    modifier: Modifier = Modifier,
    role: EmTextRole = EmTextRole.Body,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
) {
    val style = role.style()
    val max = style.fontSize
    BasicText(
        text = if (role == EmTextRole.Overline) text.uppercase() else text,
        modifier = modifier,
        style = style.copy(
            color = color.takeOrElse { LocalContentColor.current.takeOrElse { EmTheme.colors.scheme.onSurface } },
            textAlign = textAlign ?: TextAlign.Unspecified,
        ),
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(minFontSize = max * MIN_FIT_SCALE, maxFontSize = max),
    )
}

private const val MIN_FIT_SCALE = 0.6f
