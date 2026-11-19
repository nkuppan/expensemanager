package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmNoSearchResults
import com.naveenapps.expensemanager.core.designsystem.em.EmSearchField
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/**
 * The shared layout of the multi-select bottom sheets (categories, accounts): a title with a
 * close button, a search box, "N selected" with a "Select all" checkbox, the list, and a
 * floating Done button.
 *
 * @param selectAllState for the items currently listed (so it respects the search).
 * @param items the list rows; the sheet adds bottom room so Done never covers the last one.
 */
@Composable
fun MultiSelectionSheet(
    title: String,
    searchPlaceholder: String,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedText: String,
    selectAllState: ToggleableState,
    onSelectAllClick: () -> Unit,
    showNoResults: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    items: LazyListScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = EmTheme.spacing.xl, end = EmTheme.spacing.md, top = EmTheme.spacing.xs, bottom = EmTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmText(
                    text = title,
                    modifier = Modifier.weight(1f),
                    role = EmTextRole.ScreenTitle,
                )
                if (onClose != null) {
                    EmIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.a11y_close),
                        onClick = onClose,
                        variant = EmIconButtonVariant.Tonal,
                    )
                }
            }

            EmSearchField(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = searchPlaceholder,
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = EmTheme.spacing.xl, end = EmTheme.spacing.sm, top = EmTheme.spacing.xs, bottom = EmTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmText(
                    text = selectedText,
                    modifier = Modifier.weight(1f),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
                Row(
                    modifier = Modifier
                        .clip(EmTheme.shapes.pill)
                        .clickable(role = Role.Checkbox, onClick = onSelectAllClick)
                        .padding(end = EmTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // The row handles the click (and is the accessible checkbox).
                    TriStateCheckbox(state = selectAllState, onClick = null, modifier = Modifier.padding(EmTheme.spacing.md))
                    EmText(
                        text = stringResource(R.string.selection_select_all),
                        role = EmTextRole.Label,
                    )
                }
            }

            if (showNoResults) {
                EmNoSearchResults(modifier = Modifier.padding(horizontal = EmTheme.spacing.xl, vertical = EmTheme.spacing.xl))
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(start = EmTheme.spacing.lg, end = EmTheme.spacing.lg, top = EmTheme.spacing.xs, bottom = DONE_CLEARANCE),
                verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
                content = items,
            )
        }

        EmButton(
            text = stringResource(R.string.selection_done),
            onClick = onDone,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(EmTheme.spacing.lg),
        )
    }
}

private val DONE_CLEARANCE = 80.dp
