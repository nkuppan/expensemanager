package com.naveenapps.expensemanager.feature.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.foundation.withTime
import kotlin.time.Clock

/**
 * "Daily reminders are off" on Home: a second, gentle chance for users who skipped or denied the
 * reminder ask after their first expense. Shown while notifications are blocked (and the state
 * allows it, see [DashboardState.showReminderBanner]).
 *
 * "Turn on" shows the system dialog while Android still allows it, and opens the app's
 * notification settings once it doesn't (after a denial, with no rationale to show). Allowing
 * notifications either way switches the daily reminder on.
 */
@Composable
internal fun ReminderBannerItem(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var canNotify by remember { mutableStateOf(context.canPostNotifications()) }

    // Re-check on every return to the app, e.g. after allowing notifications in Settings.
    LifecycleResumeEffect(Unit) {
        val allowedNow = context.canPostNotifications()
        if (allowedNow && !canNotify) onAction(DashboardAction.NotificationsAllowed)
        canNotify = allowedNow
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        canNotify = granted
        onAction(DashboardAction.ReminderPermissionResult(granted))
    }

    if (!state.showReminderBanner(canNotify)) return

    LaunchedEffect(Unit) { onAction(DashboardAction.ReminderBannerShown) }
    val suggestedTime = state.suggestedReminderTime?.let { context.formatReminderTime(it.hour, it.minute) }
    HomeReminderBanner(
        modifier = modifier,
        suggestedTime = suggestedTime,
        onTurnOn = {
            val is24Hour = DateFormat.is24HourFormat(context)
            when {
                activity.canAskForNotifications(state.notificationPermissionAsks) -> {
                    onAction(
                        DashboardAction.ReminderBannerTurnOn(DashboardViewModel.REMINDER_METHOD_DIALOG, is24Hour),
                    )
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    onAction(
                        DashboardAction.ReminderBannerTurnOn(DashboardViewModel.REMINDER_METHOD_SETTINGS, is24Hour),
                    )
                    context.openAppNotificationSettings()
                }
            }
        },
        onNotNow = { onAction(DashboardAction.DismissReminderBanner) },
    )
}

@Composable
private fun HomeReminderBanner(
    onTurnOn: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    suggestedTime: String? = null,
) {
    EmCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(
                start = EmTheme.spacing.lg,
                end = EmTheme.spacing.sm,
                top = EmTheme.spacing.lg,
                bottom = EmTheme.spacing.sm,
            ),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(EmTheme.shapes.md)
                        .background(EmTheme.colors.scheme.primary.tint()),
                    contentAlignment = Alignment.Center,
                ) {
                    EmIcon(
                        icon = Icons.Outlined.NotificationsOff,
                        contentDescription = null,
                        tint = EmTheme.colors.scheme.primary,
                    )
                }
                Spacer(modifier = Modifier.width(EmTheme.spacing.md))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = EmTheme.spacing.sm),
                ) {
                    EmText(
                        text = if (suggestedTime != null) {
                            stringResource(R.string.reminder_banner_title_with_time, suggestedTime)
                        } else {
                            stringResource(R.string.reminder_banner_title)
                        },
                        role = EmTextRole.SectionTitle,
                    )
                    EmText(
                        text = if (suggestedTime != null) {
                            stringResource(R.string.reminder_banner_body_with_time)
                        } else {
                            stringResource(R.string.reminder_banner_body)
                        },
                        role = EmTextRole.Supporting,
                        color = EmTextColor.Supporting,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = EmTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmButton(
                    text = stringResource(R.string.reminder_banner_not_now),
                    onClick = onNotNow,
                    variant = EmButtonVariant.Tertiary,
                )
                EmButton(
                    text = if (suggestedTime != null) {
                        stringResource(R.string.reminder_banner_turn_on_at, suggestedTime)
                    } else {
                        stringResource(R.string.reminder_banner_turn_on)
                    },
                    onClick = onTurnOn,
                    variant = EmButtonVariant.Secondary,
                )
            }
        }
    }
}

/** "9:00 PM" or "21:00", following the phone's 12/24-hour setting. */
private fun Context.formatReminderTime(hour: Int, minute: Int): String {
    val todayAt = Clock.System.now().withTime(hour, minute)
    return DateFormat.getTimeFormat(this).format(todayAt.toEpochMilliseconds())
}

/** True when the app may post notifications (always below Android 13). */
private fun Context.canPostNotifications(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
    PackageManager.PERMISSION_GRANTED

/**
 * Whether the system dialog will still appear: never asked yet, or Android wants a rationale (one
 * denial so far). After a second denial it shows nothing, so the app opens Settings instead.
 */
private fun Activity?.canAskForNotifications(asksSoFar: Int): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    if (asksSoFar == 0) return true
    return this?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true
}

/** This app's notification settings page (minSdk 26, so always available). */
private fun Context.openAppNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}

@EmPreviews
@Composable
private fun HomeReminderBannerPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        HomeReminderBanner(onTurnOn = {}, onNotNow = {})
    }
}

@EmPreviews
@Composable
private fun HomeReminderBannerWithTimePreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        HomeReminderBanner(onTurnOn = {}, onNotNow = {}, suggestedTime = "9:00 PM")
    }
}
