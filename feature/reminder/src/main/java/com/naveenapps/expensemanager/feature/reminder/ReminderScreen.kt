package com.naveenapps.expensemanager.feature.reminder

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
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

    val state by viewModel.state.collectAsState()

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
            onAction = viewModel::processAction
        )
    }

    ReminderScreenContent(
        state = state,
        onAction = viewModel::processAction
    )
}
@Composable
private fun ReminderScreenContent(
    state: ReminderState,
    onAction: (ReminderAction) -> Unit,
) {
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
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
                .padding(horizontal = 20.dp),
        ) {
            if (state.showPermissionMessage.not()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Reminder toggle card
                AppCardView {
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

                Spacer(modifier = Modifier.height(16.dp))

                // Time picker card
                AnimatedVisibility(
                    visible = state.reminderStatus,
                    enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                    exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.schedule).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                        )

                        AppCardView {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAction.invoke(ReminderAction.ShowReminderDialog)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(44.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.AccessTime,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(id = R.string.notification_time),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = state.reminderTime,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            } else {
                // Permission required state
                Spacer(modifier = Modifier.height(48.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        modifier = Modifier.size(80.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(56.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.NotificationsOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.permission_required_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val textToShow = if (state.shouldShowRationale) {
                        stringResource(R.string.notification_permission_message)
                    } else {
                        stringResource(R.string.permission_disabled)
                    }

                    Text(
                        text = textToShow,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val isRationale = state.shouldShowRationale
                    Button(
                        onClick = {
                            if (isRationale) {
                                onAction.invoke(ReminderAction.RequestPermission)
                            } else {
                                onAction.invoke(ReminderAction.OpenSettings)
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    ) {
                        Icon(
                            imageVector = if (isRationale)
                                Icons.Rounded.Notifications
                            else
                                Icons.Rounded.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                if (isRationale) R.string.request_permission
                                else R.string.open_settings,
                            ),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (checked)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun ReminderScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        ReminderScreenContent(
            state = ReminderState(
                reminderStatus = true
            ),
            onAction = {},
        )
    }
}