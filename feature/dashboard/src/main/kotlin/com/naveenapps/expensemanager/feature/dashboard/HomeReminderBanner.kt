package com.naveenapps.expensemanager.feature.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView

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
    HomeReminderBanner(
        modifier = modifier,
        onTurnOn = {
            when {
                activity.canAskForNotifications(state.notificationPermissionAsks) -> {
                    onAction(DashboardAction.ReminderBannerTurnOn(DashboardViewModel.REMINDER_METHOD_DIALOG))
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    onAction(DashboardAction.ReminderBannerTurnOn(DashboardViewModel.REMINDER_METHOD_SETTINGS))
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
) {
    AppCardView(
        modifier = modifier.fillMaxWidth(),
        cornerSize = 16.dp,
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.reminder_banner_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.reminder_banner_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onNotNow) {
                    Text(text = stringResource(R.string.reminder_banner_not_now))
                }
                FilledTonalButton(onClick = onTurnOn) {
                    Text(text = stringResource(R.string.reminder_banner_turn_on), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** True when the app may post notifications (always below Android 13). */
private fun Context.canPostNotifications(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
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

@AppPreviewsLightAndDarkMode
@Composable
private fun HomeReminderBannerPreview() {
    ExpenseManagerPreviewTheme(padding = 16.dp) {
        HomeReminderBanner(onTurnOn = {}, onNotNow = {})
    }
}
