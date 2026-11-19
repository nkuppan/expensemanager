package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint

/**
 * The one list row: optional leading slot (usually [EmIconTile] or an avatar), a title, optional
 * supporting text and an optional trailing slot. Fixed padding and text roles.
 */
@Composable
fun EmListItem(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    titleMaxLines: Int = 2,
) {
    val spacing = EmTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
            .heightIn(min = MIN_HEIGHT.dp)
            .padding(horizontal = spacing.lg, vertical = spacing.md)
            .alpha(if (enabled) 1f else EmTheme.alpha.disabled),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        leading?.invoke()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            EmText(text = title, role = EmTextRole.ItemTitle, maxLines = titleMaxLines)
            if (!supporting.isNullOrBlank()) {
                EmText(
                    text = supporting,
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                    maxLines = 2,
                )
            }
        }
        if (trailing != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                content = trailing,
            )
        }
    }
}

private const val MIN_HEIGHT = 56

/** Keeps a long value (a currency name, say) from squeezing the row title. */
private const val VALUE_MAX_WIDTH = 140

/**
 * An icon on a 12% tinted square: the leading element of rows and cards. [color] defaults to the
 * primary colour; pass a money colour (EmTheme.colors.expense, a category colour) when it means one.
 */
@Composable
fun EmIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = EmTheme.colors.scheme.primary,
) {
    Box(
        modifier = modifier
            .size(EmTheme.icons.tile)
            .background(color.tint(), EmTheme.shapes.md),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(EmTheme.icons.md),
        )
    }
}

/** What sits at the end of a settings row. */
sealed interface EmSettingTrailing {
    /** Opens another screen. */
    data object Chevron : EmSettingTrailing

    /** Shows the current choice, then a chevron. */
    data class Value(val text: String) : EmSettingTrailing

    /** On/off right in the row. */
    data class Toggle(val checked: Boolean, val onCheckedChange: (Boolean) -> Unit) : EmSettingTrailing

    companion object {
        /** The value when there is one, otherwise just the chevron. */
        fun valueOrChevron(value: String?): EmSettingTrailing = if (value.isNullOrBlank()) Chevron else Value(value)
    }
}

/** A settings row: icon tile, title, optional description, and a chevron, value or switch. */
@Composable
fun EmSettingItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    trailing: EmSettingTrailing = EmSettingTrailing.Chevron,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val click = when (trailing) {
        is EmSettingTrailing.Toggle -> ({ trailing.onCheckedChange(!trailing.checked) })
        else -> onClick
    }
    EmListItem(
        title = title,
        modifier = modifier,
        supporting = supporting,
        leading = { EmIconTile(icon = icon) },
        onClick = click,
        enabled = enabled,
        trailing = {
            when (trailing) {
                is EmSettingTrailing.Toggle -> Switch(
                    checked = trailing.checked,
                    onCheckedChange = trailing.onCheckedChange,
                    enabled = enabled,
                )

                is EmSettingTrailing.Value -> {
                    EmText(
                        text = trailing.text,
                        role = EmTextRole.Label,
                        color = EmTextColor.Primary,
                        maxLines = 1,
                        modifier = Modifier.widthIn(max = VALUE_MAX_WIDTH.dp),
                    )
                    Chevron()
                }

                EmSettingTrailing.Chevron -> Chevron()
            }
        },
    )
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = EmTheme.colors.scheme.onSurfaceVariant,
        modifier = Modifier.size(EmTheme.icons.md),
    )
}

/** Divider between rows. [inset] aligns it with the row text after an icon tile. */
@Composable
fun EmDivider(modifier: Modifier = Modifier, inset: Boolean = false) {
    val spacing = EmTheme.spacing
    HorizontalDivider(
        modifier = modifier.padding(start = if (inset) spacing.lg + EmTheme.icons.tile + spacing.lg else 0.dp),
        color = EmTheme.colors.scheme.outlineVariant,
    )
}
