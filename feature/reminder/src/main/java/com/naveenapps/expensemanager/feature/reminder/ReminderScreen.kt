package com.naveenapps.expensemanager.feature.reminder

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.naveenapps.expensemanager.core.designsystem.ui.components.PrimaryButton
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
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
    onAction: (ReminderAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopNavigationBar(
                onClick = {
                    onAction.invoke(ReminderAction.ClosePage)
                },
                title = stringResource(R.string.reminder),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
        ) {
            if (state.showPermissionMessage.not()) {
                SwitchSettingsItem(
                    modifier = Modifier
                        .clickable {
                            onAction.invoke(
                                ReminderAction.ChangeReminderStatus(
                                    state.reminderStatus.not()
                                )
                            )
                        }
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    icon = Icons.Default.NotificationsActive,
                    title = stringResource(id = R.string.reminder_notification),
                    description = stringResource(id = R.string.reminder_notification_message),
                    checked = state.reminderStatus,
                    checkChange = { status ->
                        onAction.invoke(ReminderAction.ChangeReminderStatus(status))
                    },
                )
                SettingsItem(
                    modifier = Modifier
                        .clickable {
                            onAction.invoke(ReminderAction.ShowReminderDialog)
                        }
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    title = stringResource(id = R.string.notification_time),
                    description = state.reminderTime,
                    icon = Icons.Default.AccessTime,
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    val textToShow = if (state.shouldShowRationale) {
                        stringResource(R.string.notification_permission_message)
                    } else {
                        stringResource(R.string.permission_disabled)
                    }

                    Text(textToShow)
                    Spacer(modifier = Modifier.height(8.dp))
                    PrimaryButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            if (state.shouldShowRationale) {
                                onAction.invoke(ReminderAction.RequestPermission)
                            } else {
                                onAction.invoke(ReminderAction.OpenSettings)
                            }
                        },
                    ) {
                        val actionString = if (state.shouldShowRationale) {
                            R.string.request_permission
                        } else {
                            R.string.open_settings
                        }
                        Text(stringResource(actionString))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            imageVector = icon,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
        ) {
            Text(text = title)
            Text(text = description, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SwitchSettingsItem(
    title: String,
    description: String,
    checked: Boolean,
    icon: ImageVector,
    checkChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            imageVector = icon,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterVertically),
        ) {
            Text(text = title)
            Text(text = description, style = MaterialTheme.typography.labelMedium)
        }
        Switch(
            modifier = Modifier
                .wrapContentSize()
                .padding(end = 16.dp)
                .align(Alignment.CenterVertically),
            checked = checked,
            onCheckedChange = checkChange,
        )
    }
}

@Preview
@Composable
fun ReminderScreenPreview() {
    ExpenseManagerTheme {
        ReminderScreenContent(
            state = ReminderState(),
            onAction = {},
        )
    }
}
