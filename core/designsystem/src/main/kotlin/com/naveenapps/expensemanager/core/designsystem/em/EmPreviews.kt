package com.naveenapps.expensemanager.core.designsystem.em

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Light + dark preview of a composable. Use with ExpenseManagerPreviewTheme. Replaces the
 * naveenapps library's AppPreviewsLightAndDarkMode so features don't import that library.
 */
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
annotation class EmPreviews
