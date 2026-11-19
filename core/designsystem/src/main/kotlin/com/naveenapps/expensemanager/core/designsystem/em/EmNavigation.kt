package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/** Tabs under a top bar (Expense / Income categories). Labels are upper-case overline style. */
@Composable
fun EmTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = EmTheme.colors.scheme
    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        containerColor = scheme.background,
    ) {
        tabs.forEachIndexed { index, label ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                selectedContentColor = scheme.primary,
                unselectedContentColor = scheme.onSurfaceVariant,
                text = {
                    Text(
                        text = label.uppercase(),
                        style = EmTheme.type.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

/** The app's bottom navigation bar. Holds [EmNavigationBarItem]s. */
@Composable
fun EmNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = EmTheme.colors.scheme.surfaceContainer,
        tonalElevation = EmTheme.elevation.none,
        content = content,
    )
}

@Composable
fun RowScope.EmNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    selectedIcon: ImageVector = icon,
) {
    EmNavigationBarItemImpl(selected, onClick, label, modifier) {
        EmIcon(icon = if (selected) selectedIcon else icon, contentDescription = null)
    }
}

/** [EmNavigationBarItem] with a drawable icon. */
@Composable
fun RowScope.EmNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    painter: Painter,
    label: String,
    modifier: Modifier = Modifier,
) {
    EmNavigationBarItemImpl(selected, onClick, label, modifier) {
        EmIcon(painter = painter, contentDescription = null)
    }
}

@Composable
private fun RowScope.EmNavigationBarItemImpl(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier,
    icon: @Composable () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        // One line that shrinks to fit, instead of wrapping at large font sizes.
        label = { EmFitText(text = label, role = EmTextRole.Label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = EmTheme.colors.scheme.onPrimaryContainer,
            indicatorColor = EmTheme.colors.scheme.primaryContainer,
        ),
    )
}

/** One action in an overflow menu. [destructive] paints it in the error colour. */
data class EmMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val destructive: Boolean = false,
)

/**
 * The "⋮" button and its menu: secondary actions of a row or screen (rename, delete, stop).
 * The menu closes itself when an item is chosen.
 */
@Composable
fun EmOverflowMenu(
    contentDescription: String,
    items: List<EmMenuItem>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        EmIconButton(
            icon = Icons.Default.MoreVert,
            contentDescription = contentDescription,
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = EmTheme.shapes.md,
            containerColor = EmTheme.colors.scheme.surfaceContainerLow,
        ) {
            items.forEach { item ->
                val color = if (item.destructive) EmTheme.colors.scheme.error else EmTheme.colors.scheme.onSurface
                DropdownMenuItem(
                    text = { Text(text = item.label, style = EmTheme.type.body, color = color) },
                    leadingIcon = item.icon?.let { icon ->
                        { EmIcon(icon = icon, contentDescription = null, size = EmTheme.icons.md, tint = color) }
                    },
                    onClick = {
                        expanded = false
                        item.onClick()
                    },
                )
            }
        }
    }
}
