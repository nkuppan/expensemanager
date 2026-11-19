package com.naveenapps.expensemanager.feature.category.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.feature.filter.type.getCategory

@Composable
fun CategoryItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    position: EmCardPosition = EmCardPosition.Single,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    customImagePath: String? = null,
) {
    EmCard(
        position = position,
        selected = selected,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick.invoke() }
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAndBackgroundView(
                icon = icon,
                iconBackgroundColor = iconBackgroundColor,
                name = name,
                customImagePath = customImagePath,
            )
            EmText(
                text = name,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = EmTheme.spacing.lg),
                role = EmTextRole.ItemTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@EmPreviews
@Composable
fun CategoryItemPreview() {
    val category = getCategory(0)
    ExpenseManagerPreviewTheme {
        // List screen — chevron
        CategoryItem(
            name = category.name,
            icon = category.storedIcon.name,
            iconBackgroundColor = category.storedIcon.backgroundColor,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            trailingContent = { CategoryItemDefaults.ChevronTrailing() },
        )

        // Selection screen — checkmark
        CategoryItem(
            name = category.name,
            icon = category.storedIcon.name,
            iconBackgroundColor = category.storedIcon.backgroundColor,
            selected = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            trailingContent = { CategoryItemDefaults.MultiCheckedTrailing(true) },
        )
        CategoryItem(
            name = category.name,
            icon = category.storedIcon.name,
            iconBackgroundColor = category.storedIcon.backgroundColor,
            selected = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            trailingContent = { CategoryItemDefaults.MultiCheckedTrailing(false) },
        )

        // Custom end icon
        CategoryItem(
            name = category.name,
            icon = category.storedIcon.name,
            iconBackgroundColor = category.storedIcon.backgroundColor,
            modifier = Modifier.fillMaxWidth(),
            trailingContent = { CategoryItemDefaults.IconTrailing(Icons.Default.Edit) },
        )
    }
}
