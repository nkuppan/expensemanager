package com.naveenapps.expensemanager.feature.reminder

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconTile
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingTrailing
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.repository.ShareRepository
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ReminderScreen(
    shareRepository: ShareRepository,
    viewModel: ReminderViewModel = koinViewModel(),
) {
    val notificationPermission: PermissionState? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission =
                rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
            val permissionResult = notificationPermission.status
            if (permissionResult.isGranted.not()) {
                if (permissionResult.shouldShowRationale) {
                    viewModel.processAction(ReminderAction.ShowPermissionRationale)
                } else {
                    LaunchedEffect(key1 = "permission") {
                        viewModel.processAction(ReminderAction.RequestPermission)
                    }
                }
            } else {
                viewModel.processAction(ReminderAction.PermissionGranted)
            }
            notificationPermission
        } else {
            null
        }

    val state by viewModel.state.collectAsStateWithLifecycle()

    // Re-check delivery whenever the screen shows, e.g. back from notification or battery settings.
    LifecycleResumeEffect(Unit) {
        viewModel.processAction(ReminderAction.RefreshDelivery)
        onPauseOrDispose { }
    }

    ObserveAsEvents(viewModel.event) {
        when (it) {
            ReminderEvent.RequestPermission -> {
                notificationPermission?.launchPermissionRequest()
            }

            ReminderEvent.OpenSettings -> {
                shareRepository.openAppSettings()
            }
        }
    }

    if (state.showTimePickerDialog) {
        ReminderTimePickerView(
            reminderTimeState = state.reminderTimeState,
            onAction = viewModel::processAction,
        )
    }

    ReminderScreenContent(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun ReminderScreenContent(
    state: ReminderState,
    onAction: (ReminderAction) -> Unit,
) {
    EmScaffold(
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
                    onAction.invoke(ReminderAction.ClosePage)
                },
                title = stringResource(R.string.reminder),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = EmTheme.spacing.xl),
        ) {
            if (state.showPermissionMessage.not()) {
                Spacer(modifier = Modifier.height(EmTheme.spacing.md))

                // Reminder toggle card
                EmCard {
                    SwitchSettingsItem(
                        icon = Icons.Rounded.NotificationsActive,
                        title = stringResource(id = R.string.reminder_notification),
                        description = stringResource(id = R.string.reminder_notification_message),
                        checked = state.reminderStatus,
                        onCheckedChange = { status ->
                            onAction.invoke(ReminderAction.ChangeReminderStatus(status))
                        },
                    )
                }

                Spacer(modifier = Modifier.height(EmTheme.spacing.lg))

                // Time picker card
                AnimatedVisibility(
                    visible = state.reminderStatus,
                    enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                    exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
                ) {
                    Column {
                        EmText(
                            text = stringResource(id = R.string.schedule),
                            modifier = Modifier.padding(bottom = EmTheme.spacing.sm, start = EmTheme.spacing.xs),
                            role = EmTextRole.Overline,
                            color = EmTextColor.Supporting,
                        )

                        EmCard(modifier = Modifier.padding(bottom = EmTheme.spacing.lg)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAction.invoke(ReminderAction.ShowReminderDialog)
                                    }
                                    .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.lg),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
                            ) {
                                EmIconTile(icon = Icons.Rounded.AccessTime)
                                Column(modifier = Modifier.weight(1f)) {
                                    EmText(
                                        text = stringResource(id = R.string.notification_time),
                                        role = EmTextRole.Supporting,
                                        color = EmTextColor.Supporting,
                                    )
                                    Spacer(modifier = Modifier.height(EmTheme.spacing.xxs))
                                    EmText(
                                        text = state.reminderTime,
                                        role = EmTextRole.SectionTitle,
                                    )
                                }
                                EmIcon(
                                    icon = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    size = EmTheme.icons.md,
                                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                                )
                            }
                        }

                        ReminderDeliverySection(
                            state = state,
                            onAction = onAction,
                            modifier = Modifier.padding(bottom = EmTheme.spacing.lg),
                        )
                    }
                }

                // Heads-up before recurring payments: its own switch, independent of the reminder.
                EmCard {
                    SwitchSettingsItem(
                        icon = Icons.Rounded.EventRepeat,
                        title = stringResource(id = R.string.upcoming_alerts_title),
                        description = stringResource(id = R.string.upcoming_alerts_description),
                        checked = state.upcomingAlertsOn,
                        onCheckedChange = { on ->
                            onAction.invoke(ReminderAction.ChangeUpcomingAlerts(on))
                        },
                    )
                }
            } else {
                // Permission required state
                Spacer(modifier = Modifier.height(EmTheme.spacing.xxxl))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(EmTheme.icons.hero)
                            .clip(EmTheme.shapes.pill)
                            .background(EmTheme.colors.scheme.errorContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmIcon(
                            icon = Icons.Rounded.NotificationsOff,
                            contentDescription = null,
                            tint = EmTheme.colors.scheme.onErrorContainer,
                        )
                    }

                    Spacer(modifier = Modifier.height(EmTheme.spacing.xl))

                    EmText(
                        text = stringResource(R.string.permission_required_title),
                        role = EmTextRole.SectionTitle,
                    )

                    Spacer(modifier = Modifier.height(EmTheme.spacing.sm))

                    val textToShow = if (state.shouldShowRationale) {
                        stringResource(R.string.notification_permission_message)
                    } else {
                        stringResource(R.string.permission_disabled)
                    }

                    EmText(
                        text = textToShow,
                        color = EmTextColor.Supporting,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = EmTheme.spacing.xl),
                    )

                    Spacer(modifier = Modifier.height(EmTheme.spacing.xl))

                    val isRationale = state.shouldShowRationale
                    EmButton(
                        text = stringResource(
                                if (isRationale) {
                                    R.string.request_permission
                                } else {
                                    R.string.open_settings
                                },
                            ),
                        onClick = {
                            if (isRationale) {
                                onAction.invoke(ReminderAction.RequestPermission)
                            } else {
                                onAction.invoke(ReminderAction.OpenSettings)
                            }
                        },
                        leadingIcon = if (isRationale) {
                                Icons.Rounded.Notifications
                            } else {
                                Icons.Rounded.Settings
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchSettingsItem(
    title: String,
    description: String,
    checked: Boolean,
    icon: ImageVector,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    EmSettingItem(
        icon = icon,
        title = title,
        supporting = description,
        trailing = EmSettingTrailing.Toggle(checked, onCheckedChange),
        modifier = modifier,
    )
}

@EmPreviews
@Composable
fun ReminderScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        ReminderScreenContent(
            state = ReminderState(
                reminderStatus = true,
            ),
            onAction = {},
        )
    }
}
