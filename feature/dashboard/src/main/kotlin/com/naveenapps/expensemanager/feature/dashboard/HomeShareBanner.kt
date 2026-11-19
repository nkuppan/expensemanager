package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmColors
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint

/**
 * The share banner at the bottom of Home: a gradient card in the brand colours with a short
 * pitch, a "Share the app" button and a close button. Its colours come from the app's theme
 * (see [shareBannerColors]), so it looks right in both light and dark mode. Shown to users who
 * have logged a few expenses; see DashboardState.showShareBanner.
 */
@Composable
internal fun HomeShareBanner(
    onShare: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = shareBannerColors(EmTheme.colors)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(EmTheme.shapes.lg)
            .background(Brush.linearGradient(colors.gradient)),
    ) {
        // Two soft circles in the corners for depth.
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = colors.decoration,
                radius = size.height * 0.75f,
                center = Offset(size.width * 0.98f, -size.height * 0.15f),
            )
            drawCircle(
                color = colors.decoration.copy(alpha = colors.decoration.alpha * 0.7f),
                radius = size.height * 0.45f,
                center = Offset(size.width * 0.80f, size.height * 1.05f),
            )
        }

        Row(
            modifier = Modifier.padding(
                start = EmTheme.spacing.xl,
                end = EmTheme.spacing.xl,
                top = EmTheme.spacing.xl,
                bottom = EmTheme.spacing.lg,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = EmTheme.spacing.md),
            ) {
                EmText(
                    text = stringResource(R.string.home_share_title),
                    role = EmTextRole.SectionTitle,
                    color = colors.content,
                )
                Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
                EmText(
                    text = stringResource(R.string.home_share_description),
                    role = EmTextRole.Supporting,
                    color = colors.content,
                )
                Spacer(modifier = Modifier.height(EmTheme.spacing.md))
                EmButton(
                    text = stringResource(R.string.home_share_button),
                    onClick = onShare,
                    leadingIcon = Icons.Outlined.Share,
                    variant = if (colors.invertButton) EmButtonVariant.Inverse else EmButtonVariant.Primary,
                )
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(EmTheme.shapes.pill)
                    .background(colors.decoration.copy(alpha = colors.decoration.alpha * 1.8f)),
                contentAlignment = Alignment.Center,
            ) {
                EmIcon(
                    icon = Icons.Outlined.VolunteerActivism,
                    contentDescription = null,
                    size = EmTheme.icons.tile,
                    tint = colors.content,
                )
            }
        }

        EmIconButton(
            icon = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.getting_started_hide),
            onClick = onDismiss,
            tint = colors.content,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(EmTheme.spacing.sm),
        )
    }
}

@Immutable
private data class ShareBannerColors(
    val gradient: List<Color>,
    val content: Color,
    val invertButton: Boolean,
    val decoration: Color,
)

/**
 * Light theme: a vivid primary → tertiary gradient with light text and a surface-coloured button.
 * Dark theme: the deeper primary → tertiary *container* colours with their matching text and a
 * primary button, so the card stays rich without glaring and every pair keeps its contrast.
 * Decided from EmTheme.colors.isDark, so it follows the app's theme setting, not just the
 * system one.
 */
private fun shareBannerColors(colors: EmColors): ShareBannerColors {
    val scheme = colors.scheme
    val isDark = colors.isDark
    return if (isDark) {
        ShareBannerColors(
            gradient = listOf(scheme.primaryContainer, scheme.tertiaryContainer),
            content = scheme.onPrimaryContainer,
            invertButton = false,
            decoration = scheme.onPrimaryContainer.tint(),
        )
    } else {
        ShareBannerColors(
            gradient = listOf(scheme.primary, scheme.tertiary),
            content = scheme.onPrimary,
            invertButton = true,
            decoration = scheme.onPrimary.tint(),
        )
    }
}

@EmPreviews
@Composable
private fun HomeShareBannerPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        HomeShareBanner(onShare = {}, onDismiss = {})
    }
}
