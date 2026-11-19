package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/** A row with a radio button: one choice of several. The whole row is the touch target. */
@Composable
fun EmSelectableItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
) {
    EmListItem(
        title = title,
        supporting = supporting,
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        trailing = { EmRadio(selected = selected, onClick = null) },
    )
}

/**
 * Pick one of a few values in a bottom sheet (landing tab, default account, …). Selecting closes
 * the sheet. The settings row that opens it shows the current value with EmSettingTrailing.Value.
 */
@Composable
fun EmSelectionSheet(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    EmBottomSheet(onDismissRequest = onDismissRequest, title = title) {
        Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs)) {
            options.forEachIndexed { index, option ->
                EmSelectableItem(
                    title = option,
                    selected = index == selectedIndex,
                    onClick = {
                        onSelect(index)
                        onDismissRequest()
                    },
                )
            }
        }
    }
}
