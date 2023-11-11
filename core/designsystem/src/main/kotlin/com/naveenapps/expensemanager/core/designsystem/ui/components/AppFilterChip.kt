package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RowScope.AppFilterChip(
    filterName: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    @DrawableRes filterIcon: Int? = null,
    @ColorRes filterSelectedColor: Int? = null,
    centerAlign: Boolean = false,
    onClick: () -> Unit = {},
) {
    val selectedContainerColor = if (filterSelectedColor != null) {
        colorResource(id = filterSelectedColor)
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val labelColor = if (filterSelectedColor != null) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSecondary
    }

    FilterChip(
        modifier = modifier,
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .then(
                        if (centerAlign) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.wrapContentWidth()
                        }
                    ),
                text = filterName,
                textAlign = TextAlign.Center
            )
        },
        leadingIcon = {
            if (filterIcon != null) {
                Icon(
                    painter = painterResource(id = filterIcon),
                    contentDescription = ""
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedLabelColor = labelColor,
            selectedContainerColor = selectedContainerColor,
            selectedLeadingIconColor = labelColor
        )
    )
}