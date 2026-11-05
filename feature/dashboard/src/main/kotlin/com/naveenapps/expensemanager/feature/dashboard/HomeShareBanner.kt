package com.naveenapps.expensemanager.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme

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
    val colors = shareBannerColors(MaterialTheme.colorScheme)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
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
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_share_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.content,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.home_share_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.content.copy(alpha = 0.88f),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onShare,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.buttonContainer,
                        contentColor = colors.buttonContent,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_share_button),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colors.decoration.copy(alpha = colors.decoration.alpha * 1.8f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.VolunteerActivism,
                    contentDescription = null,
                    tint = colors.content,
                    modifier = Modifier.size(34.dp),
                )
            }
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.getting_started_hide),
                tint = colors.content.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Immutable
private data class ShareBannerColors(
    val gradient: List<Color>,
    val content: Color,
    val buttonContainer: Color,
    val buttonContent: Color,
    val decoration: Color,
)

/**
 * Light theme: a vivid primary → tertiary gradient with light text and a surface-coloured button.
 * Dark theme: the deeper primary → tertiary *container* colours with their matching text and a
 * primary button, so the card stays rich without glaring and every pair keeps its contrast.
 * Decided from the theme's own surface, so it follows the app's theme setting, not just the
 * system one.
 */
private fun shareBannerColors(scheme: ColorScheme): ShareBannerColors {
    val isDark = scheme.surface.luminance() < 0.5f
    return if (isDark) {
        ShareBannerColors(
            gradient = listOf(scheme.primaryContainer, scheme.tertiaryContainer),
            content = scheme.onPrimaryContainer,
            buttonContainer = scheme.primary,
            buttonContent = scheme.onPrimary,
            decoration = scheme.onPrimaryContainer.copy(alpha = 0.06f),
        )
    } else {
        ShareBannerColors(
            gradient = listOf(scheme.primary, scheme.tertiary),
            content = scheme.onPrimary,
            buttonContainer = scheme.surface,
            buttonContent = scheme.primary,
            decoration = scheme.onPrimary.copy(alpha = 0.10f),
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun HomeShareBannerPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        HomeShareBanner(onShare = {}, onDismiss = {})
    }
}
