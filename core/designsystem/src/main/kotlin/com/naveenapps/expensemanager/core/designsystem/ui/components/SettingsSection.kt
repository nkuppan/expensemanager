package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.R


@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
        )
        content()
    }
}


@Composable
@AppPreviewsLightAndDarkMode
fun SettingsSectionWithSampleValueAndSubtitlePreview() {
    NaveenAppsPreviewTheme {
        SettingsSection(title = "Sample Settings Header") {
            AppCardView {
                SettingRow(
                    icon = Icons.Default.ImageSearch,
                    title = "Sample Title",
                    subtitle = "Sample description about the option. Which is too long to show the content so it's truncating",
                    value = "Value",
                    onClick = {},
                    showDivider = true
                )
                SettingRow(
                    icon = Icons.Default.ImageSearch,
                    title = "Sample Title",
                    subtitle = "Sample description about the option. Which is too long to show the content so it's truncating",
                    value = "Value",
                    onClick = {}
                )
            }
        }

        SettingsSection(title = "Sample Settings Header") {
            AppCardView(modifier = Modifier) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StringTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = "Sample text",
                        isError = false,
                        onValueChange = { },
                        label = R.string.icon,
                    )
                }
            }
        }
    }
}