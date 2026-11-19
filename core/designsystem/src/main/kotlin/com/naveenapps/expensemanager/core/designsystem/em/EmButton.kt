package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

enum class EmButtonVariant {
    /** The one main action of a screen or sheet (filled). */
    Primary,

    /** A second, lower-emphasis action next to a Primary (tonal). */
    Secondary,

    /** Inline and dismissive actions: Cancel, View all, Undo (text only). */
    Tertiary,

    /** Deleting or anything that loses data (filled, error colour). */
    Destructive,

    /** Main action on a coloured / brand surface (share banner): surface-coloured fill. */
    Inverse,
}

enum class EmButtonSize(internal val height: Dp) {
    /** Full width, bottom of a screen or sheet. */
    Large(52.dp),

    /** Inline in content. */
    Medium(40.dp),

    /** Header actions and dense rows. */
    Small(32.dp),
}

/**
 * The one button. Shape, colours, height and text style come from [variant] and [size];
 * [loading] swaps the label for a spinner and blocks clicks.
 */
@Composable
fun EmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EmButtonVariant = EmButtonVariant.Primary,
    size: EmButtonSize = EmButtonSize.Medium,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val sized = modifier
        .heightIn(min = size.height)
        .then(if (size == EmButtonSize.Large) Modifier.fillMaxWidth() else Modifier)
    val shape = EmTheme.shapes.pill
    val padding = PaddingValues(
        horizontal = if (size == EmButtonSize.Small) EmTheme.spacing.md else EmTheme.spacing.xl,
        vertical = EmTheme.spacing.xs,
    )
    val clickable = enabled && !loading
    val content: @Composable RowScope.() -> Unit = {
        EmButtonContent(text = text, leadingIcon = leadingIcon, loading = loading)
    }
    when (variant) {
        EmButtonVariant.Primary -> Button(
            onClick = onClick,
            modifier = sized,
            enabled = clickable,
            shape = shape,
            contentPadding = padding,
            content = content,
        )

        EmButtonVariant.Destructive -> Button(
            onClick = onClick,
            modifier = sized,
            enabled = clickable,
            shape = shape,
            contentPadding = padding,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmTheme.colors.scheme.error,
                contentColor = EmTheme.colors.scheme.onError,
            ),
            content = content,
        )

        EmButtonVariant.Inverse -> Button(
            onClick = onClick,
            modifier = sized,
            enabled = clickable,
            shape = shape,
            contentPadding = padding,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmTheme.colors.scheme.surface,
                contentColor = EmTheme.colors.scheme.primary,
            ),
            content = content,
        )

        EmButtonVariant.Secondary -> FilledTonalButton(
            onClick = onClick,
            modifier = sized,
            enabled = clickable,
            shape = shape,
            contentPadding = padding,
            content = content,
        )

        EmButtonVariant.Tertiary -> TextButton(
            onClick = onClick,
            modifier = sized,
            enabled = clickable,
            shape = shape,
            contentPadding = padding,
            content = content,
        )
    }
}

@Composable
private fun EmButtonContent(text: String, leadingIcon: ImageVector?, loading: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.size(EmTheme.icons.sm),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )

            leadingIcon != null -> Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(EmTheme.icons.md),
            )
        }
        Text(
            text = text,
            style = EmTheme.type.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

enum class EmIconButtonVariant {
    /** Plain icon (top bars, row actions). */
    Standard,

    /** Icon on a tonal circle, for a stand-alone action inside content. */
    Tonal,
}

/**
 * Icon-only button. [contentDescription] is required: TalkBack reads it, and an icon-only control
 * without one is unusable for screen reader users.
 */
@Composable
fun EmIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EmIconButtonVariant = EmIconButtonVariant.Standard,
    enabled: Boolean = true,
    badge: EmBadge? = null,
    tint: Color = Color.Unspecified,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = when (variant) {
            EmIconButtonVariant.Standard -> IconButtonDefaults.iconButtonColors()
            EmIconButtonVariant.Tonal -> IconButtonDefaults.filledTonalIconButtonColors()
        },
    ) {
        val iconContent: @Composable () -> Unit = {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(EmTheme.icons.lg),
                tint = tint.takeOrElse { LocalContentColor.current },
            )
        }
        if (badge == null) {
            iconContent()
        } else {
            BadgedBox(
                badge = {
                    Badge(containerColor = EmTheme.colors.scheme.error) {
                        if (badge is EmBadge.Count) Text(text = badge.text, style = EmTheme.type.overline)
                    }
                },
                content = { iconContent() },
            )
        }
    }
}

/** [EmIconButton] for drawable icons (brand logos). Drawn in their own colours. */
@Composable
fun EmIconButton(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(EmTheme.icons.lg),
            tint = Color.Unspecified,
        )
    }
}

/** Badge on an icon button: a dot ("something new") or a short count. */
sealed interface EmBadge {
    data object Dot : EmBadge

    data class Count(val count: Int) : EmBadge {
        val text: String get() = if (count > MAX_BADGE) "$MAX_BADGE+" else count.toString()
    }
}

private const val MAX_BADGE = 99

/** The "Add" button of a list screen. Only for creating something; never for Save. */
@Composable
fun EmFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = EmTheme.colors.scheme
    if (label == null) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = EmTheme.shapes.lg,
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    } else {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            shape = EmTheme.shapes.lg,
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            icon = { Icon(imageVector = icon, contentDescription = null) },
            text = { Text(text = label, style = EmTheme.type.label) },
        )
    }
}

/**
 * The bar at the bottom of a form: one Primary button (Save), optionally a Secondary one beside it.
 * Goes in EmScaffold's bottomBar; it sits above the keyboard and the navigation bar.
 */
@Composable
fun EmBottomActionBar(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryIcon: ImageVector? = null,
    primaryEnabled: Boolean = true,
    loading: Boolean = false,
    secondaryText: String? = null,
    onSecondaryClick: () -> Unit = {},
) {
    val spacing = EmTheme.spacing
    Surface(color = EmTheme.colors.scheme.background, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = spacing.screenGutter, vertical = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (secondaryText != null) {
                EmButton(
                    text = secondaryText,
                    onClick = onSecondaryClick,
                    variant = EmButtonVariant.Secondary,
                    size = EmButtonSize.Large,
                    modifier = Modifier.weight(1f),
                )
            }
            EmButton(
                text = primaryText,
                onClick = onPrimaryClick,
                size = EmButtonSize.Large,
                leadingIcon = primaryIcon,
                enabled = primaryEnabled,
                loading = loading,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
