package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R

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
                    .padding(start = 20.dp, end = 12.dp, top = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (onClose != null) {
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.a11y_close),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            SheetSearchField(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = searchPlaceholder,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Checkbox, onClick = onSelectAllClick)
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // The row handles the click (and is the accessible checkbox).
                    TriStateCheckbox(state = selectAllState, onClick = null, modifier = Modifier.padding(12.dp))
                    Text(
                        text = stringResource(R.string.selection_select_all),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (showNoResults) {
                NoSearchResults(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = DONE_CLEARANCE),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = items,
            )
        }

        Button(
            onClick = onDone,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Text(text = stringResource(R.string.selection_done), fontWeight = FontWeight.SemiBold)
        }
    }
}

private val DONE_CLEARANCE = 80.dp
