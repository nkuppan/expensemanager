package com.naveenapps.expensemanager.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.naveenapps.expensemanager.core.designsystem.em.EmSelectionSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingTrailing
import com.naveenapps.expensemanager.core.model.LandingPage

/**
 * A settings row showing the current choice; tapping it opens a sheet to pick another
 * (Preferences: default account and categories, landing tab).
 */
@Composable
internal fun SelectionSettingItem(
    label: String,
    selectedValue: String,
    icon: ImageVector,
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }

    EmSettingItem(
        icon = icon,
        title = label,
        trailing = EmSettingTrailing.Value(selectedValue),
        onClick = { showSheet = true },
        modifier = modifier,
    )

    if (showSheet) {
        EmSelectionSheet(
            title = label,
            options = items,
            selectedIndex = items.indexOf(selectedValue),
            onSelect = onItemSelected,
            onDismissRequest = { showSheet = false },
        )
    }
}

internal val LandingPage.labelResId: Int
    get() = when (this) {
        LandingPage.HOME -> R.string.landing_home
        LandingPage.ANALYSIS -> R.string.landing_analysis
        LandingPage.TRANSACTIONS -> R.string.landing_transactions
        LandingPage.CATEGORIES -> R.string.landing_categories
    }
