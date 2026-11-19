package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/** Trailing text action of a section header ("View all"). */
data class EmHeaderAction(val label: String, val onClick: () -> Unit)

/**
 * The one section header: an upper-case overline caption above a group of rows or a card, with an
 * optional trailing action. Replaces SettingsSection's title, DashboardWidgetTitle, SelectionTitle,
 * SelectionHeader and WidgetHeader.
 */
@Composable
fun EmSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: EmHeaderAction? = null,
    /** Shortcut for the common "View all" action. */
    onViewAllClick: (() -> Unit)? = null,
    /** Shortcut for the "Add new" action of pickers. */
    onAddClick: (() -> Unit)? = null,
) {
    val viewAll = stringResource(R.string.view_all)
    val addNew = stringResource(R.string.add_new)
    val trailing = action
        ?: onViewAllClick?.let { EmHeaderAction(label = viewAll, onClick = it) }
        ?: onAddClick?.let { EmHeaderAction(label = addNew, onClick = it) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = HEADER_HEIGHT.dp)
            .padding(start = EmTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmText(
            text = title,
            modifier = Modifier.weight(1f),
            role = EmTextRole.Overline,
            color = EmTextColor.Supporting,
            maxLines = 1,
        )
        if (trailing != null) {
            // The action matches the header's own style (overline, in the primary colour), so it
            // never outweighs the title it belongs to.
            Row(
                modifier = Modifier
                    .clip(EmTheme.shapes.pill)
                    .clickable(role = Role.Button, onClick = trailing.onClick)
                    .heightIn(min = HEADER_HEIGHT.dp)
                    .padding(horizontal = EmTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs),
            ) {
                EmText(text = trailing.label, role = EmTextRole.Overline, color = EmTextColor.Primary, maxLines = 1)
                EmIcon(
                    icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    size = EmTheme.icons.sm,
                    tint = EmTheme.colors.scheme.primary,
                )
            }
        }
    }
}

/**
 * Every header is this tall, with or without an action, so titles line up across sections (the
 * action's tap area fills the full height).
 */
private const val HEADER_HEIGHT = 36

/** A titled group: [EmSectionHeader] above its content (usually an EmCard or EmGroupedList). */
@Composable
fun EmSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        EmSectionHeader(title = title, modifier = Modifier.padding(bottom = EmTheme.spacing.xs))
        content()
    }
}
