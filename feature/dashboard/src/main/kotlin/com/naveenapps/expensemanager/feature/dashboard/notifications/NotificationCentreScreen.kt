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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
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
    EmScaffold(
        topBar = {
            EmTopBar(
                title = stringResource(R.string.notification_centre_title),
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = onBack,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = EmTheme.spacing.lg,
                end = EmTheme.spacing.lg,
                top = EmTheme.spacing.sm,
                bottom = EmTheme.spacing.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
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
    EmCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EmTheme.spacing.lg),
            verticalAlignment = Alignment.Top,
        ) {
            KindIcon(icon = insight.kind.icon)
            Spacer(modifier = Modifier.width(EmTheme.spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                TitleRow(title = insight.title, isNew = item.isNew)
                Spacer(modifier = Modifier.height(EmTheme.spacing.xxs))
                EmText(
                    text = insight.message,
                    color = EmTextColor.Supporting,
                )
                Spacer(modifier = Modifier.height(EmTheme.spacing.sm))
                EmText(
                    text = formatDateTime(insight.postedAt),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
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

    EmCard {
        Column(modifier = Modifier.animateContentSize()) {
            // Header band
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                EmTheme.colors.scheme.primary.tint(),
                                EmTheme.colors.scheme.tertiary.tint(),
                            ),
                        ),
                    )
                    .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(EmTheme.shapes.md)
                        .background(EmTheme.colors.scheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    EmIcon(
                        icon = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = EmTheme.colors.scheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(EmTheme.spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    TitleRow(
                        title = stringResource(R.string.whats_new_version, item.release.version),
                        isNew = item.isNew,
                    )
                    EmText(
                        text = formatDate(item.time),
                        role = EmTextRole.Supporting,
                        color = EmTextColor.Supporting,
                    )
                }
            }

            // Highlights
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = EmTheme.spacing.lg,
                        end = EmTheme.spacing.lg,
                        top = EmTheme.spacing.md,
                        bottom = if (canExpand) EmTheme.spacing.xs else EmTheme.spacing.lg,
                    ),
                verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
            ) {
                shown.forEach { line -> HighlightRow(text = line) }
            }

            if (canExpand) {
                EmButton(
                    text = if (expanded) {
                        stringResource(R.string.notification_see_less)
                    } else {
                        stringResource(R.string.notification_see_more, lines.size - COLLAPSED_ITEMS)
                    },
                    onClick = { expanded = !expanded },
                    modifier = Modifier.padding(start = EmTheme.spacing.xs, bottom = EmTheme.spacing.xs),
                    variant = EmButtonVariant.Tertiary,
                    leadingIcon = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                )
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
                .clip(EmTheme.shapes.pill)
                .background(EmTheme.colors.scheme.primary.tint()),
            contentAlignment = Alignment.Center,
        ) {
            EmIcon(
                icon = Icons.Rounded.Check,
                contentDescription = null,
                size = EmTheme.icons.sm,
                tint = EmTheme.colors.scheme.primary,
            )
        }
        Spacer(modifier = Modifier.width(EmTheme.spacing.md))
        EmText(
            text = text,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Highlights shown before "See more". */
private const val COLLAPSED_ITEMS = 3

@Composable
private fun TitleRow(title: String, isNew: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        EmText(
            text = title,
            modifier = Modifier.weight(1f, fill = false),
            role = EmTextRole.ItemTitle,
        )
        if (isNew) {
            Spacer(modifier = Modifier.width(EmTheme.spacing.sm))
            EmText(
                text = stringResource(R.string.notification_new),
                modifier = Modifier
                    .clip(EmTheme.shapes.pill)
                    .background(EmTheme.colors.scheme.primaryContainer)
                    .padding(horizontal = EmTheme.spacing.sm, vertical = EmTheme.spacing.xxs),
                role = EmTextRole.Supporting,
                color = EmTheme.colors.scheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun KindIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(EmTheme.shapes.md)
            .background(EmTheme.colors.scheme.primary.tint()),
        contentAlignment = Alignment.Center,
    ) {
        EmIcon(
            icon = icon,
            contentDescription = null,
            tint = EmTheme.colors.scheme.primary,
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

@EmPreviews
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
