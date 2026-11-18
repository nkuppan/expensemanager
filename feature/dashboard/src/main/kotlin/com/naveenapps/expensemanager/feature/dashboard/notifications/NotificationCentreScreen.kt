package com.naveenapps.expensemanager.feature.dashboard.notifications

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.model.Insight
import com.naveenapps.expensemanager.core.model.InsightKind
import com.naveenapps.expensemanager.feature.dashboard.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.koin.compose.viewmodel.koinViewModel

/** Notification centre from Home's bell: release notes and pushed insights, newest first. */
@Composable
fun NotificationCentreScreen(
    viewModel: NotificationCentreViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NotificationCentreScaffold(items = state.items, onBack = viewModel::closePage)
}

@Composable
private fun NotificationCentreScaffold(
    items: List<NotificationItem>,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(R.string.notification_centre_title),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = onBack,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = items,
                key = { item ->
                    when (item) {
                        is NotificationItem.Release -> "release-${item.release.version}"
                        is NotificationItem.InsightItem -> "insight-${item.insight.postedAt}-${item.insight.title}"
                    }
                },
            ) { item ->
                when (item) {
                    is NotificationItem.Release -> ReleaseCard(item)
                    is NotificationItem.InsightItem -> InsightCard(item)
                }
            }
        }
    }
}

/** A pushed insight: icon, title, message and when it was posted. */
@Composable
private fun InsightCard(item: NotificationItem.InsightItem) {
    val insight = item.insight
    AppCardView(cornerSize = 16.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            KindIcon(icon = insight.kind.icon)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                TitleRow(title = insight.title, isNew = item.isNew)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatDateTime(insight.postedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

/**
 * A release's notes. A soft brand-tinted header carries the sparkle, "Version x" as the title,
 * the "New" tag and the release date; below it the highlights, the first [COLLAPSED_ITEMS]
 * shown until "See more" expands the rest (and "See less" folds them back).
 */
@Composable
private fun ReleaseCard(item: NotificationItem.Release) {
    val lines = stringArrayResource(item.release.items)
    val canExpand = lines.size > COLLAPSED_ITEMS
    var expanded by rememberSaveable(item.release.version) { mutableStateOf(false) }
    val shown = if (expanded || !canExpand) lines.toList() else lines.take(COLLAPSED_ITEMS)

    AppCardView(cornerSize = 16.dp) {
        Column(modifier = Modifier.animateContentSize()) {
            // Header band
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.06f),
                            ),
                        ),
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    TitleRow(
                        title = stringResource(R.string.whats_new_version, item.release.version),
                        isNew = item.isNew,
                    )
                    Text(
                        text = formatDate(item.time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Highlights
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = if (canExpand) 4.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                shown.forEach { line -> HighlightRow(text = line) }
            }

            if (canExpand) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.padding(start = 6.dp, bottom = 4.dp),
                ) {
                    Text(
                        text = if (expanded) {
                            stringResource(R.string.notification_see_less)
                        } else {
                            stringResource(R.string.notification_see_more, lines.size - COLLAPSED_ITEMS)
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/** One highlight: a small tinted check badge and the text. */
@Composable
private fun HighlightRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 1.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Highlights shown before "See more". */
private const val COLLAPSED_ITEMS = 3

@Composable
private fun TitleRow(title: String, isNew: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (isNew) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.notification_new),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun KindIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
    }
}

private val InsightKind.icon: ImageVector
    get() = when (this) {
        InsightKind.WEEKLY_SUMMARY -> Icons.Outlined.Insights
        InsightKind.BUDGET_ALERT -> Icons.Outlined.Savings
    }

/** "7 Nov 2026, 7:00 pm" in the app's language. */
@Composable
private fun formatDateTime(millis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(millis, locale) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
    }
}

/** "7 Nov 2026" in the app's language. */
@Composable
private fun formatDate(millis: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    return remember(millis, locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun NotificationCentrePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        NotificationCentreScaffold(
            items = notificationItems(
                releases = WHATS_NEW_RELEASES,
                insights = listOf(
                    Insight(
                        kind = InsightKind.BUDGET_ALERT,
                        title = "Groceries budget at 80%",
                        message = "₹8,000 of ₹10,000 spent this month.",
                        postedAt = System.currentTimeMillis(),
                    ),
                ),
                openedBefore = 0L,
                seenRelease = null,
            ),
            onBack = {},
        )
    }
}
