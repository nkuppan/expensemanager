package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint

/** Optional button of an empty state ("Add account"). */
data class EmEmptyAction(val label: String, val onClick: () -> Unit)

/**
 * The one empty state: illustration in a tinted circle, a title, optional body and up to two
 * buttons ([action] is the main one). Centred; the caller decides where it sits. [compact] is
 * for empty states inside a card (smaller circle, less padding); full screens use the default.
 */
@Composable
fun EmEmptyState(
    illustration: Painter,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    action: EmEmptyAction? = null,
    secondaryAction: EmEmptyAction? = null,
    compact: Boolean = false,
) {
    EmEmptyStateLayout(title, modifier, body, action, secondaryAction, compact) {
        Image(
            painter = illustration,
            contentDescription = null,
            modifier = Modifier.size(if (compact) EmTheme.icons.lg else EmTheme.icons.tile),
        )
    }
}

/** [EmEmptyState] with a vector icon, drawn in the primary colour (or [iconColor]). */
@Composable
fun EmEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    action: EmEmptyAction? = null,
    secondaryAction: EmEmptyAction? = null,
    iconColor: Color = EmTheme.colors.scheme.primary,
    compact: Boolean = false,
) {
    EmEmptyStateLayout(title, modifier, body, action, secondaryAction, compact, circle = iconColor.tint()) {
        EmIcon(
            icon = icon,
            contentDescription = null,
            size = if (compact) EmTheme.icons.lg else EmTheme.icons.tile,
            tint = iconColor,
        )
    }
}

@Composable
private fun EmEmptyStateLayout(
    title: String,
    modifier: Modifier,
    body: String?,
    action: EmEmptyAction?,
    secondaryAction: EmEmptyAction?,
    compact: Boolean,
    circle: Color = EmTheme.colors.scheme.surfaceContainerHighest,
    illustration: @Composable () -> Unit,
) {
    val spacing = EmTheme.spacing
    Column(
        modifier = modifier
            .testTag(EMPTY_STATE_TAG)
            .fillMaxWidth()
            .padding(
                horizontal = if (compact) spacing.lg else spacing.xxl,
                vertical = if (compact) spacing.lg else spacing.xxxl,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) EmTheme.spacing.xxxl else EmTheme.icons.hero)
                .background(circle, EmTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            illustration()
        }
        EmText(
            text = title,
            role = EmTextRole.ItemTitle,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = spacing.sm),
        )
        if (body != null) {
            EmText(text = body, color = EmTextColor.Supporting, textAlign = TextAlign.Center)
        }
        if (action != null || secondaryAction != null) {
            Row(
                modifier = Modifier.padding(top = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                action?.let { EmButton(text = it.label, onClick = it.onClick, variant = EmButtonVariant.Secondary) }
                secondaryAction?.let {
                    EmButton(text = it.label, onClick = it.onClick, variant = EmButtonVariant.Secondary)
                }
            }
        }
    }
}

/** Test tag of every empty state (UI tests look for it). */
const val EMPTY_STATE_TAG = "EmptyItem"
