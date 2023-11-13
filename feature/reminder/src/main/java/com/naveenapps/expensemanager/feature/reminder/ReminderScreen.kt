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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    var showMessageLayout by remember { mutableIntStateOf(0) }

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        val notificationPermission =
            rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
        val permissionResult = notificationPermission.status
        if (permissionResult.isGranted.not()) {
            if (permissionResult.shouldShowRationale) {
                showMessageLayout = 2
            } else {
                LaunchedEffect(key1 = "permission") {
                    notificationPermission.launchPermissionRequest()
                    if (showMessageLayout == 0) {
                        showMessageLayout = 1
                    } else {
                        showMessageLayout = 3
                    }
                }
            }
        }
        notificationPermission
    } else {
        null
    }


    var showTimePicker by remember { mutableStateOf(false) }
    if (showTimePicker) {
        ReminderTimePickerView {
            showTimePicker = false
            if (it) {
                viewModel.saveReminderStatus(true)
            }
        }
    }

    val reminderStatus by viewModel.reminderOn.collectAsState()
    val reminderTime by viewModel.reminderTime.collectAsState()

    Scaffold(topBar = {
        TopNavigationBar(
            onClick = {
                viewModel.closePage()
            }, title = stringResource(R.string.reminder)
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            if (notificationPermission == null || notificationPermission.status.isGranted) {
                SwitchSettingsItem(
                    modifier = Modifier
                        .clickable {
                            viewModel.saveReminderStatus(reminderStatus.not())
                        }
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    icon = Icons.Default.NotificationsActive,
                    title = stringResource(id = R.string.reminder_notification),
                    description = stringResource(id = R.string.reminder_notification_message),
                    checked = reminderStatus,
                    checkChange = {
                        viewModel.saveReminderStatus(it)
                    }
                )
                SettingsItem(modifier = Modifier
                    .clickable {
                        if (reminderStatus)
                            showTimePicker = true
                    }
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                    title = stringResource(id = R.string.notification_time),
                    description = reminderTime,
                    icon = Icons.Default.AccessTime
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    val shouldShowRationale = notificationPermission.status.shouldShowRationale
                    val textToShow = if (shouldShowRationale) {
                        stringResource(R.string.notification_permission_message)
                    } else {
                        stringResource(R.string.permission_disabled)
                    }

                    Text(textToShow)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            notificationPermission.launchPermissionRequest()
                        }) {
                        Text("Request permission")
                    }
                }
            }
        }
    }
}


@Composable
private fun SettingsItem(
    title: String, description: String, icon: ImageVector, modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            imageVector = icon,
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
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
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            imageVector = icon,
            contentDescription = null
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterVertically)
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
            onCheckedChange = checkChange
        )
    }
}


@Preview
@Composable
fun ReminderScreenPreview() {
    ExpenseManagerTheme {
        ReminderScreen()
    }
}