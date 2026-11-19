package com.naveenapps.expensemanager.feature.reminder

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import java.util.Locale

/**
 * "Is my reminder actually arriving?": when it last ran, plus a fix for each known reason it
 * might not show (notifications off, battery optimisation, phone makers' battery savers).
 */
@Composable
internal fun ReminderDeliverySection(
    state: ReminderState,
    onAction: (ReminderAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.md)) {
        EmCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(EmTheme.spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
            ) {
                EmIcon(
                    icon = Icons.Rounded.History,
                    contentDescription = null,
                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                )
                EmText(
                    text = state.lastReminderRunAt?.let { at ->
                        stringResource(R.string.reminder_last_run, context.relativeTime(at))
                    } ?: stringResource(R.string.reminder_not_run_yet),
                    color = EmTextColor.Supporting,
                )
            }
        }

        if (state.deliveryBlock != null) {
            DeliveryWarning(
                icon = Icons.Rounded.NotificationsOff,
                title = stringResource(R.string.reminder_blocked_title),
                body = stringResource(R.string.reminder_blocked_body),
                action = stringResource(R.string.reminder_turn_on),
                onAction = {
                    onAction(ReminderAction.DeliveryFixOpened(REASON_NOTIFICATIONS))
                    context.openNotificationSettings()
                },
            )
        }

        if (state.isBatteryOptimised) {
            val brand = PhoneBrand.current()
            DeliveryWarning(
                icon = Icons.Rounded.BatteryAlert,
                title = stringResource(R.string.reminder_battery_title),
                body = stringResource(R.string.reminder_battery_body) +
                    (brand?.let { "\n\n" + stringResource(R.string.reminder_battery_brand, it.displayName) } ?: ""),
                action = stringResource(R.string.reminder_battery_allow),
                onAction = {
                    onAction(ReminderAction.DeliveryFixOpened(REASON_BATTERY))
                    context.openBatterySettings()
                },
                secondaryAction = brand?.let { stringResource(R.string.reminder_more_help) },
                onSecondaryAction = {
                    brand?.let { context.openUrl("https://dontkillmyapp.com/${it.slug}") }
                },
            )
        }
    }
}

@Composable
private fun DeliveryWarning(
    icon: ImageVector,
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
    secondaryAction: String? = null,
    onSecondaryAction: () -> Unit = {},
) {
    EmCard(modifier = Modifier.fillMaxWidth(), variant = EmCardVariant.Error) {
        Column(
            modifier = Modifier.padding(EmTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                EmIcon(icon = icon, contentDescription = null, size = EmTheme.icons.md)
                EmText(text = title, role = EmTextRole.SectionTitle)
            }
            EmText(text = body, role = EmTextRole.Supporting)
            Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
                EmButton(text = action, onClick = onAction, variant = EmButtonVariant.Secondary)
                if (secondaryAction != null) {
                    EmButton(
                        text = secondaryAction,
                        onClick = onSecondaryAction,
                        variant = EmButtonVariant.Tertiary,
                    )
                }
            }
        }
    }
}

/** Phone makers whose battery savers often stop background work (dontkillmyapp.com pages). */
internal enum class PhoneBrand(val displayName: String, val slug: String) {
    XIAOMI("Xiaomi, Redmi and POCO", "xiaomi"),
    SAMSUNG("Samsung", "samsung"),
    ONEPLUS("OnePlus", "oneplus"),
    OPPO("OPPO", "oppo"),
    REALME("realme", "realme"),
    VIVO("vivo and iQOO", "vivo"),
    HUAWEI("Huawei and Honor", "huawei"),
    ASUS("ASUS", "asus"),
    MEIZU("Meizu", "meizu"),
    NOKIA("Nokia", "nokia"),
    SONY("Sony", "sony"),
    LENOVO("Lenovo and Motorola", "lenovo"),
    ;

    companion object {
        fun current(): PhoneBrand? = when (Build.MANUFACTURER.lowercase(Locale.ROOT)) {
            "xiaomi", "redmi", "poco" -> XIAOMI
            "samsung" -> SAMSUNG
            "oneplus" -> ONEPLUS
            "oppo" -> OPPO
            "realme" -> REALME
            "vivo", "iqoo" -> VIVO
            "huawei", "honor" -> HUAWEI
            "asus" -> ASUS
            "meizu" -> MEIZU
            "hmd global", "nokia" -> NOKIA
            "sony" -> SONY
            "lenovo", "motorola" -> LENOVO
            else -> null
        }
    }
}

private const val REASON_NOTIFICATIONS = "notifications_off"
private const val REASON_BATTERY = "battery"

private fun Context.relativeTime(at: Long): String = DateUtils.getRelativeDateTimeString(
    this,
    at,
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.WEEK_IN_MILLIS,
    0,
).toString()

private fun Context.openNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    startSafely(intent) { appDetails() }
}

// The list of apps under battery optimisation: no special permission (unlike asking directly,
// which Play restricts), and the user picks Expense Manager there.
private fun Context.openBatterySettings() {
    startSafely(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) { appDetails() }
}

private fun Context.appDetails() = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    .setData(Uri.fromParts("package", packageName, null))

private fun Context.openUrl(url: String) {
    startSafely(Intent(Intent.ACTION_VIEW, Uri.parse(url))) { null }
}

private fun Context.startSafely(intent: Intent, fallback: () -> Intent?) {
    try {
        startActivity(intent)
    } catch (error: ActivityNotFoundException) {
        fallback()?.let { runCatching { startActivity(it) } }
    }
}
