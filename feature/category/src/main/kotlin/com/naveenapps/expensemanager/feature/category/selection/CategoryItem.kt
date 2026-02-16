package com.naveenapps.expensemanager.feature.category.selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.feature.filter.type.getCategory

@Composable
fun CategoryItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    border: BorderStroke = CardDefaults.outlinedCardBorder(),
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    AppCardView(
        shape = shape,
        border = border,
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
                    }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAndBackgroundView(
                icon = icon,
                iconBackgroundColor = iconBackgroundColor,
                name = name,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            )
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun CategoryItemPreview() {

    val category = getCategory(0)
    NaveenAppsPreviewTheme {
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
            border = CategoryItemDefaults.border(true),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            trailingContent = { CategoryItemDefaults.MultiCheckedTrailing(true) },
        )
        CategoryItem(
            name = category.name,
            icon = category.storedIcon.name,
            iconBackgroundColor = category.storedIcon.backgroundColor,
            border = CategoryItemDefaults.border(false),
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