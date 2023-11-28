package com.naveenapps.expensemanager.core.designsystem

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices.NEXUS_5X
import androidx.compose.ui.tooling.preview.Preview


@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class AppPreviewsLightAndDarkMode

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = NEXUS_5X)
annotation class AppPreviewsWithNexus5x